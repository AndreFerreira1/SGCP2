
// CONFIGURAÇÃO E SERVIÇOS CENTRAIS


const API_BASE_URL = 'http://localhost:8080/api';

const Auth = {
    saveToken(token) {
        localStorage.setItem('authToken', token);
    },
    getToken() {
        return localStorage.getItem('authToken');
    },
    isLoggedIn() {
        return !!this.getToken();
    },
    logout() {
        localStorage.removeItem('authToken');
        window.location.href = 'index.html';
    },
    getUserProfile() {
        const token = this.getToken();
        if (!token) return null;
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            return {
                username: payload.sub,
                perfil: payload.perfil
            };
        } catch (e) {
            console.error("Erro ao decodificar o token:", e);
            this.logout();
            return null;
        }
    }
};

async function fetchData(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (Auth.isLoggedIn()) {
        headers['Authorization'] = `Bearer ${Auth.getToken()}`;
    }


    // Adiciona a opção para desativar o cache, garantindo que os dados sejam sempre novos.
    const fetchOptions = {
        ...options,
        headers,
        cache: 'no-cache'
    };

    try {
        const response = await fetch(url, fetchOptions);

        if (response.status === 401 || response.status === 403) {
            alert("Sua sessão expirou ou você não tem permissão. Por favor, faça o login novamente.");
            Auth.logout();
            throw new Error('Não autorizado');
        }

        const responseText = await response.text();

        if (!response.ok) {
            console.error(`Erro na API (${response.status}):`, responseText);
            try {
                const errorJson = JSON.parse(responseText);
                alert(`Erro: ${errorJson.error || responseText}`);
            } catch (e) {
                alert(`Erro: ${responseText || response.statusText}`);
            }
            throw new Error(`HTTP error! status: ${response.status}, message: ${responseText}`);
        }

        if (response.headers.get("content-type")?.includes("application/json")) {
            return responseText ? JSON.parse(responseText) : {};
        } else {
            return responseText;
        }
    } catch (error) {
        if (error.message !== 'Não autorizado') {
             console.error('Falha na comunicação com a API:', error);
        }
        throw error;
    }
}


// LÓGICA DAS PÁGINAS DE AUTENTICAÇÃO (LOGIN/CADASTRO)


function handleAuthPages() {
    const formLogin = document.getElementById('loginForm');
    const formCadastro = document.getElementById('cadastroForm');

    if (formLogin) {
        formLogin.addEventListener('submit', async (event) => {
            event.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetchData(`/login`, {
                    method: 'POST',
                    body: JSON.stringify({ username: email, password: password })
                });

                if (response.token) {
                    Auth.saveToken(response.token);
                    window.location.href = 'dashboard.html';
                }
            } catch (error) {
                console.error('Falha no login.');
            }
        });
    }

    if (formCadastro) {
        const passwordInput = document.getElementById('password');
        const strengthBar = document.getElementById('strength-bar');
        const strengthText = document.getElementById('strength-text');

        passwordInput.addEventListener('input', () => {
            const password = passwordInput.value;
            const result = checkPasswordStrength(password);
            let width = '0%';
            let color = '#ddd';
            let text = '';

            if (password.length > 0) {
                switch (result.score) {
                    case 0:
                    case 1:
                        width = '25%'; color = '#dc3545'; text = 'Muito Fraca';
                        break;
                    case 2:
                        width = '50%'; color = '#ffc107'; text = 'Média';
                        break;
                    case 3:
                        width = '75%'; color = '#007bff'; text = 'Forte';
                        break;
                    case 4:
                        width = '100%'; color = '#28a745'; text = 'Muito Forte';
                        break;
                }
            }
            strengthBar.style.width = width;
            strengthBar.style.backgroundColor = color;
            strengthText.textContent = text;
        });

        formCadastro.addEventListener('submit', async (event) => {
            event.preventDefault();
            const nomeCompleto = document.getElementById('name').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm-password').value;

            if (password !== confirmPassword) {
                alert('As senhas não coincidem.');
                return;
            }

            try {
                await fetchData(`/register`, {
                    method: 'POST',
                    body: JSON.stringify({ nomeCompleto, email, senha: password })
                });
                alert('Cadastro realizado com sucesso! Você será redirecionado para a tela de login.');
                window.location.href = 'index.html';
            } catch (error) {
                console.error('Falha no cadastro.');
            }
        });
    }
}

function checkPasswordStrength(password) {
    let score = 0;
    if (password.length > 8) score++;
    if (password.match(/[a-z]/) && password.match(/[A-Z]/)) score++;
    if (password.match(/[0-9]/)) score++;
    if (password.match(/[^a-zA-Z0-9]/)) score++;
    return { score };
}


// LÓGICA DA PÁGINA DO DASHBOARD


function handleDashboardPage() {
    if (!Auth.isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    const userProfile = Auth.getUserProfile();
    const welcomeMessage = document.getElementById('welcomeMessage');
    if(welcomeMessage) {
        welcomeMessage.textContent = `Bem-vindo, ${userProfile.username}!`;
    }

    async function carregarDadosDashboard() {
        try {
            const data = await fetchData('/dashboard');

            document.getElementById('kpi-faturamento').textContent = `R$ ${parseFloat(data.kpis.faturamentoMes).toFixed(2)}`;
            document.getElementById('kpi-novos-clientes').textContent = data.kpis.novosClientes;
            document.getElementById('kpi-pedidos-pendentes').textContent = data.kpis.pedidosPendentes;

            renderizarGrafico('graficoStatus', 'doughnut', data.graficoStatus.labels, 'Status dos Pedidos', data.graficoStatus.valores, ['#ffc107', '#28a745', '#dc3545']);
            renderizarGrafico('graficoFaturamento', 'bar', data.graficoFaturamento.labels, 'Faturamento Mensal', data.graficoFaturamento.valores, '#007bff');
        } catch (error) {
            console.error("Falha ao carregar dados do dashboard", error);
        }
    }

    function renderizarGrafico(canvasId, type, labels, label, data, backgroundColor) {
        const ctx = document.getElementById(canvasId)?.getContext('2d');
        if (ctx) {
            new Chart(ctx, {
                type: type,
                data: {
                    labels: labels,
                    datasets: [{ label: label, data: data, backgroundColor: backgroundColor }]
                },
                options: { responsive: true, maintainAspectRatio: false }
            });
        }
    }

    carregarDadosDashboard();
}


// LÓGICA DAS PÁGINAS DE CRUD (CLIENTES, PEDIDOS)


function handleClientesPage() {
    if (!Auth.isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    const form = document.getElementById('formCliente');
    const tableBody = document.querySelector('#tabelaClientes tbody');
    const idInput = document.getElementById('clienteId');
    const cancelBtn = document.getElementById('cancelEditCliente');
    const userProfile = Auth.getUserProfile();

    async function carregar() {
        try {
            const clientes = await fetchData('/clientes');
            tableBody.innerHTML = '';
            clientes.forEach(cliente => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${cliente.id}</td>
                    <td>${cliente.nome}</td>
                    <td>${cliente.email || ''}</td>
                    <td>${cliente.telefone || ''}</td>
                    <td>
                        <button class="edit-btn" data-id="${cliente.id}">Editar</button>
                        ${userProfile.perfil === 'ADMIN' ? `<button class="delete-btn" data-id="${cliente.id}">Deletar</button>` : ''}
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } catch (error) {
            console.error('Falha ao carregar clientes.', error);
        }
    }

    async function salvar(event) {
        event.preventDefault();
        const id = idInput.value;
        const cliente = {
            nome: document.getElementById('nome').value,
            email: document.getElementById('email').value,
            telefone: document.getElementById('telefone').value,
        };
        const endpoint = id ? `/clientes/${id}` : '/clientes';
        const method = id ? 'PUT' : 'POST';

        try {
            await fetchData(endpoint, { method, body: JSON.stringify(cliente) });
            alert(`Cliente ${id ? 'atualizado' : 'adicionado'} com sucesso!`);
            resetarFormulario();
            carregar();
        } catch (error) {
            console.error('Falha ao salvar cliente.', error);
        }
    }

    async function preencherFormularioParaEdicao(id) {
        try {
            const cliente = await fetchData(`/clientes/${id}`);
            idInput.value = cliente.id;
            document.getElementById('nome').value = cliente.nome;
            document.getElementById('email').value = cliente.email || '';
            document.getElementById('telefone').value = cliente.telefone || '';
            cancelBtn.style.display = 'inline-block';
            window.scrollTo(0, 0);
        } catch (error) {
            console.error('Falha ao carregar cliente para edição.', error);
        }
    }

    async function deletar(id) {
        if (confirm('Tem certeza que deseja deletar este cliente? Os pedidos associados também serão removidos.')) {
            try {
                await fetchData(`/clientes/${id}`, { method: 'DELETE' });
                alert('Cliente deletado com sucesso!');
                carregar();
            } catch (error) {
                console.error('Falha ao deletar cliente.', error);
            }
        }
    }

    function resetarFormulario() {
        form.reset();
        idInput.value = '';
        cancelBtn.style.display = 'none';
    }

    tableBody.addEventListener('click', (event) => {
        const id = event.target.dataset.id;
        if (event.target.classList.contains('edit-btn')) {
            preencherFormularioParaEdicao(id);
        } else if (event.target.classList.contains('delete-btn')) {
            deletar(id);
        }
    });

    form.addEventListener('submit', salvar);
    cancelBtn.addEventListener('click', resetarFormulario);
    carregar();
}

function handlePedidosPage() {
    if (!Auth.isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    const form = document.getElementById('formPedido');
    const tableBody = document.querySelector('#tabelaPedidos tbody');
    const idInput = document.getElementById('pedidoId');
    const cancelBtn = document.getElementById('cancelEditPedido');
    const clienteSelect = document.getElementById('clienteIdPedido');
    const userProfile = Auth.getUserProfile();

    async function popularClientesSelect() {
        if (!clienteSelect) return;
        try {
            const clientes = await fetchData(`/clientes`);
            clienteSelect.innerHTML = '<option value="">Selecione um Cliente</option>';
            clientes.forEach(cliente => {
                const option = document.createElement('option');
                option.value = cliente.id;
                option.textContent = cliente.nome;
                clienteSelect.appendChild(option);
            });
        } catch (error) {
            console.error("Falha ao popular select de clientes", error);
        }
    }

    async function carregar() {
        try {
            const pedidos = await fetchData('/pedidos');
            tableBody.innerHTML = '';
            pedidos.forEach(pedido => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${pedido.id}</td>
                    <td>${pedido.clienteId}</td>
                    <td>${new Date(pedido.dataPedido + 'T00:00:00').toLocaleDateString()}</td>
                    <td>R$ ${parseFloat(pedido.valorTotal).toFixed(2)}</td>
                    <td>${pedido.status}</td>
                    <td>
                        <button class="edit-btn" data-id="${pedido.id}">Editar</button>
                        ${userProfile.perfil === 'ADMIN' ? `<button class="delete-btn" data-id="${pedido.id}">Deletar</button>` : ''}
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } catch(error) {
            console.error("Falha ao carregar pedidos", error);
        }
    }

    async function salvar(event) {
        event.preventDefault();
        const id = idInput.value;
        const pedido = {
            clienteId: parseInt(clienteSelect.value),
            dataPedido: document.getElementById('dataPedido').value,
            valorTotal: parseFloat(document.getElementById('valorTotal').value),
            status: document.getElementById('statusPedido').value
        };

        if (!pedido.clienteId) {
            alert("Por favor, selecione um cliente.");
            return;
        }

        const endpoint = id ? `/pedidos/${id}` : '/pedidos';
        const method = id ? 'PUT' : 'POST';

        try {
            await fetchData(endpoint, { method, body: JSON.stringify(pedido) });
            alert(`Pedido ${id ? 'atualizado' : 'adicionado'} com sucesso!`);
            resetarFormulario();
            carregar();
        } catch (error) {
            console.error("Falha ao salvar pedido", error);
        }
    }

    async function preencherFormularioParaEdicao(id) {
        try {
            const pedido = await fetchData(`/pedidos/${id}`);
            idInput.value = pedido.id;
            clienteSelect.value = pedido.clienteId;
            document.getElementById('dataPedido').value = pedido.dataPedido;
            document.getElementById('valorTotal').value = pedido.valorTotal;
            document.getElementById('statusPedido').value = pedido.status;
            cancelBtn.style.display = 'inline-block';
            window.scrollTo(0, 0);
        } catch (error) {
            console.error("Falha ao carregar pedido para edição", error);
        }
    }

    async function deletar(id) {
        if (confirm('Tem certeza que deseja deletar este pedido?')) {
            try {
                await fetchData(`/pedidos/${id}`, { method: 'DELETE' });
                alert('Pedido deletado com sucesso!');
                carregar();
            } catch (error) {
                console.error("Falha ao deletar pedido", error);
            }
        }
    }

    function resetarFormulario() {
        form.reset();
        idInput.value = '';
        cancelBtn.style.display = 'none';
    }

    tableBody.addEventListener('click', (event) => {
        const id = event.target.dataset.id;
        if (event.target.classList.contains('edit-btn')) {
            preencherFormularioParaEdicao(id);
        } else if (event.target.classList.contains('delete-btn')) {
            deletar(id);
        }
    });

    form.addEventListener('submit', salvar);
    cancelBtn.addEventListener('click', resetarFormulario);

    popularClientesSelect();
    carregar();
}

function handleRelatorioPage() {
    if (!Auth.isLoggedIn()) {
        window.location.href = 'index.html';
        return;
    }

    const tableBody = document.querySelector('#tabelaRelatorioPedidosClientes tbody');

    async function carregarRelatorio() {
        if (!tableBody) return;
        try {
            const relatorio = await fetchData(`/pedidos/com-clientes`);
            tableBody.innerHTML = '';
            relatorio.forEach(item => {
                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${item.id}</td>
                    <td>${item.nomeCliente}</td>
                    <td>${new Date(item.dataPedido + 'T00:00:00').toLocaleDateString()}</td>
                    <td>R$ ${parseFloat(item.valorTotal).toFixed(2)}</td>
                    <td>${item.status}</td>
                `;
                tableBody.appendChild(tr);
            });
        } catch (error) {
            console.error("Falha ao carregar relatório", error);
        }
    }

    carregarRelatorio();
}



// INICIALIZAÇÃO DA APLICAÇÃO


document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname.split("/").pop() || 'index.html';

    const nav = document.querySelector('nav');
    if (Auth.isLoggedIn() && nav) {
        const userProfile = Auth.getUserProfile();
        const userInfo = document.createElement('span');
        userInfo.className = 'user-info';
        if (userProfile) {
            userInfo.textContent = `Olá, ${userProfile.username} (${userProfile.perfil})`;
            nav.prepend(userInfo);
        }

        const logoutLink = document.createElement('a');
        logoutLink.href = '#';
        logoutLink.textContent = 'Sair';
        logoutLink.id = 'logout-link';
        logoutLink.onclick = (e) => {
            e.preventDefault();
            Auth.logout();
        };
        nav.appendChild(logoutLink);
    }

    switch(path) {
        case 'index.html':
        case 'cadastro.html':
            handleAuthPages();
            break;
        case 'dashboard.html':
            handleDashboardPage();
            break;
        case 'clientes.html':
            handleClientesPage();
            break;
        case 'pedidos.html':
            handlePedidosPage();
            break;
        case 'relatorio.html':
        case 'relatorio_pedidos_clientes.html':
            handleRelatorioPage();
            break;
    }
});
