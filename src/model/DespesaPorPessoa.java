package model;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class DespesaPorPessoa extends Despesa {
    private static final long serialVersionUID = 1L;
    private BigDecimal valorPorPessoa;

    public DespesaPorPessoa(String descricao, BigDecimal valorPorPessoa, Participante[] participantes) {
        BigDecimal valor = valorPorPessoa.multiply(BigDecimal.valueOf(participantes.length));
        super(descricao, valor, participantes);
        this.valorPorPessoa = valorPorPessoa;
    }

    @Override
    public BigDecimal getValorDoParticipante(Participante participante) {
        Participante[] participantes = getParticipantes();
        for (int i = 0; i < participantes.length; i++) {
            if (participante != null && participantes[i].getCodigo_unico_participante() == participante.getCodigo_unico_participante()) {
                return this.valorPorPessoa;
            }
        }
        return new BigDecimal("0.00");
    }

    @Override
    public Map<Integer, BigDecimal> calcularValoresPorParticipante() {
        Map<Integer, BigDecimal> valores = new LinkedHashMap<>();
        Participante[] participantes = getParticipantes();
        for (int i = 0; i < participantes.length; i++) {
            valores.put(participantes[i].getCodigo_unico_participante(), this.valorPorPessoa);
        }
        return valores;
    }

    @Override
    public BigDecimal getValorInformado() {
        return this.valorPorPessoa;
    }

    @Override
    public void setValorInformado(BigDecimal novoValor) {
        setValorPorPessoa(novoValor);
    }

    @Override
    public String getTipo() {
        return "porPessoa";
    }

    public BigDecimal getValorPorPessoa() {
        return valorPorPessoa;
    }

    public void setValorPorPessoa(BigDecimal valorPorPessoa) {
        if (valorPorPessoa == null || valorPorPessoa.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de despesa inválido");
        }
        this.valorPorPessoa = valorPorPessoa;
        setValor(valorPorPessoa.multiply(BigDecimal.valueOf(getParticipantes().length)));
    }

    @Override
    public void setParticipantes(Participante[] participantes) {
        super.setParticipantes(participantes);
        setValor(this.valorPorPessoa.multiply(BigDecimal.valueOf(participantes.length)));
    }
}
