package org.example.model;

import java.math.BigDecimal;
import java.util.List;

public class DashboardData {
    private KpiData kpis;
    private ChartData graficoStatus;
    private ChartData graficoFaturamento;
    // Getters e Setters...
    public KpiData getKpis(){return kpis;}
    public void setKpis(KpiData kpis){this.kpis=kpis;}
    public ChartData getGraficoStatus(){return graficoStatus;}
    public void setGraficoStatus(ChartData graficoStatus){this.graficoStatus=graficoStatus;}
    public ChartData getGraficoFaturamento(){return graficoFaturamento;}
    public void setGraficoFaturamento(ChartData graficoFaturamento){this.graficoFaturamento=graficoFaturamento;}

    public static class KpiData {
        private BigDecimal faturamentoMes;
        private long novosClientes;
        private long pedidosPendentes;
        public KpiData(BigDecimal f, long n, long p) {
            this.faturamentoMes=f; this.novosClientes=n; this.pedidosPendentes=p;
        }
    }
    public static class ChartData {
        private List<String> labels;
        private List<?> valores;
        public ChartData(List<String> l, List<?> v) {
            this.labels=l; this.valores=v;
        }
    }
}