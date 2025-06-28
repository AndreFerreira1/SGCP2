package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Pedido {
    private int id;
    private int clienteId;
    private LocalDate dataPedido;
    private BigDecimal valorTotal;
    private String status;
    private String nomeCliente;


    public int getId(){return id;}
    public void setId(int id){this.id=id;}

    public int getClienteId(){return clienteId;}
    public void setClienteId(int clienteId){this.clienteId=clienteId;}

    public LocalDate getDataPedido(){return dataPedido;}
    public void setDataPedido(LocalDate dataPedido){this.dataPedido=dataPedido;}

    public BigDecimal getValorTotal(){return valorTotal;}
    public void setValorTotal(BigDecimal valorTotal){this.valorTotal=valorTotal;}

    public String getStatus(){return status;}
    public void setStatus(String status){this.status=status;}

    public String getNomeCliente(){return nomeCliente;}
    public void setNomeCliente(String nomeCliente){this.nomeCliente=nomeCliente;}
}
