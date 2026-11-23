package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

public abstract class Despesa implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contador = 0;
    private int codigo_unico_despesa;
    private String descricao;
    private BigDecimal valor;
    private Participante[] participantes;

    public Despesa(String descricao, BigDecimal valor, Participante[] participantes) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição da despesa");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de despesa inválido");
        }
        if (participantes == null || participantes.length == 0) {
            throw new IllegalArgumentException("Selecione ao menos um participante para a despesa");
        }

        this.codigo_unico_despesa = ++contador;
        this.descricao = descricao.trim();
        this.valor = valor;
        this.participantes = participantes;
    }

    public static void atualizarContador(int ultimoCodigo) {
        if (ultimoCodigo > contador) {
            contador = ultimoCodigo;
        }
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição da despesa");
        }
        this.descricao = descricao.trim();
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal novoValor) {
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de despesa inválido");
        }
        this.valor = novoValor;
    }

    public BigDecimal getValorInformado() {
        return getValor();
    }

    public void setValorInformado(BigDecimal novoValor) {
        setValor(novoValor);
    }

    public abstract BigDecimal getValorDoParticipante(Participante participante);

    public abstract Map<Integer, BigDecimal> calcularValoresPorParticipante();

    public Participante[] getParticipantes() {
        return participantes;
    }

    public void setParticipantes(Participante[] participantes) {
        if (participantes == null || participantes.length == 0) {
            throw new IllegalArgumentException("Selecione ao menos um participante para a despesa");
        }
        this.participantes = participantes;
    }

    public abstract String getTipo();

    public int getCodigo_unico_despesa() {
        return codigo_unico_despesa;
    }
}
