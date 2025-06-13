const API_BASE_URL = 'http://localhost:8080/api';
const API_AUTH_URL = `${API_BASE_URL}/auth`;

async function fetchData(url, options = {}) {
    try {
        const response = await fetch(url, options);
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
        console.error('Falha na comunicação com a API:', error);
        throw error;
    }
}

function handleAuthPages() {
    const formLogin = document.getElementById('loginForm');
    const formCadastro = document.getElementById('cadastroForm');

    if (formLogin) {
        formLogin.addEventListener('submit', async (event) => {
            event.preventDefault();
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            try {
                const response = await fetchData(`${API_AUTH_URL}/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username: email, password: password })
                });
                alert(response.message || 'Login bem-sucedido!');
                window.location.href = 'clientes.html';
            } catch (error) {
                console.error('Falha no login.');
            }
        });
    }

    if (formCadastro) {
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
                await fetchData(`${API_AUTH_URL}/cadastro`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
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

function handleClientesPage() {
    const form = document.getElementById('formCliente');
    const tableBody = document.querySelector('#tabelaClientes tbody');
    const idInput = document.getElementById('clienteId');
    const cancelBtn = document.getElementById('cancelEditCliente');
    const clientesApiUrl = `${API_BASE_URL}/clientes/`;

    async function carregar() {
        try {
            const clientes = await fetchData(clientesApiUrl);
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
                        <button class="delete-btn" data-id="${cliente.id}">Deletar</button>
                    </td>
                `;
                tableBody.appendChild(tr);
            });
        } catch (error) {
            console.error('Falha ao carregar clientes.', error);
            alert('Não foi possível carregar a lista de clientes.');
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

        const url = id ? `${clientesApiUrl}${id}` : clientesApiUrl;
        const method = id ? 'PUT' : 'POST';

        try {
            await fetchData(url, {
                method,
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(cliente)
            });
            alert(`Cliente ${id ? 'atualizado' : 'adicionado'} com sucesso!`);
            resetarFormulario();
            carregar();
        } catch (error) {
            console.error('Falha ao salvar cliente.', error);
        }
    }

    async function preencherFormularioParaEdicao(id) {
        try {
            const cliente = await fetchData(`${clientesApiUrl}${id}`);
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
                await fetchData(`${clientesApiUrl}${id}`, { method: 'DELETE' });
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
        }
        if (event.target.classList.contains('delete-btn')) {
            deletar(id);
        }
    });

    form.addEventListener('submit', salvar);
    cancelBtn.addEventListener('click', resetarFormulario);
    carregar();
}

async function popularClientesSelect() {
    const select = document.getElementById('clienteIdPedido');
    if (!select) return;

    try {
        const clientes = await fetchData(`${API_BASE_URL}/clientes/`);
        clientes.forEach(cliente => {
            const option = document.createElement('option');
            option.value = cliente.id;
            option.textContent = cliente.nome;
            select.appendChild(option);
        });
    } catch (error) {
        console.error("Falha ao popular select de clientes", error);
    }
}

async function carregarPedidos() {
    const tableBody = document.querySelector('#tabelaPedidos tbody');
    if (!tableBody) return;

    try {
        const pedidos = await fetchData(`${API_BASE_URL}/pedidos/`);
        tableBody.innerHTML = '';
        pedidos.forEach(pedido => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${pedido.id}</td>
                <td>${pedido.clienteId}</td>
                <td>${new Date(pedido.dataPedido).toLocaleDateString()}</td>
                <td>R$ ${parseFloat(pedido.valorTotal).toFixed(2)}</td>
                <td>${pedido.status}</td>
                <td>
                    <button onclick="preencherFormularioPedidoParaEdicao(${pedido.id})">Editar</button>
                    <button onclick="deletarPedido(${pedido.id})">Deletar</button>
                </td>
            `;
            tableBody.appendChild(tr);
        });
    } catch (error) {
        console.error("Falha ao carregar pedidos", error);
    }
}

function handlePedidosPage() {
    const form = document.getElementById('formPedido');
    const cancelBtn = document.getElementById('cancelEditPedido');
    const pedidoIdInput = document.getElementById('pedidoId');
    const pedidosApiUrl = `${API_BASE_URL}/pedidos/`;

    popularClientesSelect();
    carregarPedidos();

    if (form) {
        form.addEventListener('submit', async (event) => {
            event.preventDefault();
            const id = pedidoIdInput.value;
            const pedido = {
                clienteId: parseInt(document.getElementById('clienteIdPedido').value),
                dataPedido: document.getElementById('dataPedido').value,
                valorTotal: parseFloat(document.getElementById('valorTotal').value),
                status: document.getElementById('statusPedido').value
            };

            if (!pedido.clienteId) {
                alert("Por favor, selecione um cliente.");
                return;
            }

            const url = id ? `${pedidosApiUrl}${id}` : pedidosApiUrl;
            const method = id ? 'PUT' : 'POST';

            try {
                await fetchData(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(pedido)
                });
                alert(`Pedido ${id ? 'atualizado' : 'adicionado'} com sucesso!`);
                form.reset();
                pedidoIdInput.value = '';
                if (cancelBtn) cancelBtn.style.display = 'none';
                carregarPedidos();
            } catch (error) {
                console.error('Falha ao salvar pedido:', error);
            }
        });
    }

    if (cancelBtn) {
        cancelBtn.addEventListener('click', () => {
            form.reset();
            pedidoIdInput.value = '';
            cancelBtn.style.display = 'none';
        });
    }
}

async function preencherFormularioPedidoParaEdicao(id) {
    try {
        const pedido = await fetchData(`${API_BASE_URL}/pedidos/${id}`);
        document.getElementById('pedidoId').value = pedido.id;
        document.getElementById('clienteIdPedido').value = pedido.clienteId;
        document.getElementById('dataPedido').value = pedido.dataPedido;
        document.getElementById('valorTotal').value = pedido.valorTotal;
        document.getElementById('statusPedido').value = pedido.status;
        document.getElementById('cancelEditPedido').style.display = 'inline-block';
        window.scrollTo(0, 0);
    } catch (error) {
        console.error("Falha ao buscar pedido para edição", error);
    }
}

async function deletarPedido(id) {
    if (confirm('Tem certeza que deseja deletar este pedido?')) {
        try {
            await fetchData(`${API_BASE_URL}/pedidos/${id}`, { method: 'DELETE' });
            alert('Pedido deletado com sucesso!');
            carregarPedidos();
        } catch (error) {
            console.error('Falha ao deletar pedido', error);
        }
    }
}

async function carregarRelatorioPedidosClientes() {
    const tableBody = document.querySelector('#tabelaRelatorioPedidosClientes tbody');
    if (!tableBody) return;

    try {
        const relatorio = await fetchData(`${API_BASE_URL}/pedidos/com-clientes`);
        tableBody.innerHTML = '';
        relatorio.forEach(item => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.id}</td>
                <td>${item.nomeCliente}</td>
                <td>${new Date(item.dataPedido).toLocaleDateString()}</td>
                <td>R$ ${parseFloat(item.valorTotal).toFixed(2)}</td>
                <td>${item.status}</td>
            `;
            tableBody.appendChild(tr);
        });
    } catch (error) {
        console.error("Falha ao carregar relatório", error);
    }
}


function handleRelatorioPage() {
    carregarRelatorioPedidosClientes();
}

document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('loginForm') || document.getElementById('cadastroForm')) {
        handleAuthPages();
    } else if (document.getElementById('formCliente')) {
        handleClientesPage();
    } else if (document.getElementById('formPedido')) {
        handlePedidosPage();
    } else if (document.getElementById('tabelaRelatorioPedidosClientes')) {
        handleRelatorioPage();
    }

    const currentPage = window.location.pathname.split("/").pop();
    document.querySelectorAll('nav a').forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });
});