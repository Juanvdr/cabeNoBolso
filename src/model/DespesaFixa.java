package model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DespesaFixa extends Despesa {
    private static final long serialVersionUID = 1L;
    private Map<Integer, BigDecimal> valoresPorParticipante;

    public DespesaFixa(String descricao, BigDecimal valor, Participante[] participantes) {
        super(descricao, valor, participantes);
        recalcularDistribuicao();
    }

    private void recalcularDistribuicao() {
        Participante[] participantes = getParticipantes();
        int qtd = participantes.length;

        BigDecimal valorBase = getValor().divide(BigDecimal.valueOf(qtd), 2, RoundingMode.DOWN);
        BigDecimal totalDistribuido = valorBase.multiply(BigDecimal.valueOf(qtd));
        int centavosRestantes = getValor().subtract(totalDistribuido)
                .movePointRight(2)
                .intValueExact();

        // Ordena uma cópia em ordem crescente do código único (sem alterar o array original)
        Participante[] ordenados = Arrays.copyOf(participantes, qtd);
        Arrays.sort(ordenados, Comparator.comparingInt(Participante::getCodigo_unico_participante));

        Map<Integer, BigDecimal> valores = new LinkedHashMap<>();
        BigDecimal umCentavo = new BigDecimal("0.01");

        for (int i = 0; i < ordenados.length; i++) {
            BigDecimal valorParticipante;
            if (i < centavosRestantes) {
                valorParticipante = valorBase.add(umCentavo);
            } else {
                valorParticipante = valorBase;
            }
            valores.put(ordenados[i].getCodigo_unico_participante(), valorParticipante);
        }
        this.valoresPorParticipante = valores;
    }

    @Override
    public BigDecimal getValorDoParticipante(Participante participante) {
        if (participante == null) {
            return new BigDecimal("0.00");
        }
        return valoresPorParticipante.getOrDefault(
                participante.getCodigo_unico_participante(),
                new BigDecimal("0.00")
        );
    }

    @Override
    public Map<Integer, BigDecimal> calcularValoresPorParticipante() {
        return Map.copyOf(valoresPorParticipante);
    }

    @Override
    public void setValor(BigDecimal novoValor) {
        super.setValor(novoValor);
        recalcularDistribuicao();
    }

    @Override
    public void setParticipantes(Participante[] participantes) {
        super.setParticipantes(participantes);
        recalcularDistribuicao();
    }

    public String getTipo() {
        return "fixa";
    }
}
