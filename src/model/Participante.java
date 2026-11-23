package model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Participante implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contador = 0;
    private int codigo_unico_participante;
    private String nome;
    private BigDecimal orcamento = BigDecimal.ZERO;

    public Participante(String nome, BigDecimal orcamento) {
        setNome(nome);
        setOrcamento(orcamento);

        this.codigo_unico_participante = ++contador;
    }

    public static void atualizarContador(int ultimoCodigo) {
        if (ultimoCodigo > contador) {
            contador = ultimoCodigo;
        }
    }

    public boolean orcamentoOk(BigDecimal totalDespesas) {
        return orcamento.compareTo(totalDespesas) >= 0;
    }

    public int getCodigo_unico_participante() {
        return codigo_unico_participante;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do participante");
        }
        this.nome = nome.trim();
    }

    public BigDecimal getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(BigDecimal novoOrcamento) {
        if (novoOrcamento == null || novoOrcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de orçamento inválido");
        }
        this.orcamento = novoOrcamento;
    }
}
