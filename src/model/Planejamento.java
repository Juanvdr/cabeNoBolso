package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Planejamento implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int contador = 0;
    private int codigo_unico_planejamento;
    private String titulo;
    private ArrayList<Despesa> despesas = new ArrayList<>();

    public Planejamento(String titulo) {
        setTitulo(titulo);
        this.codigo_unico_planejamento = ++contador;
    }

    public static void atualizarContador(int ultimoCodigo) {
        if (ultimoCodigo > contador) {
            contador = ultimoCodigo;
        }
    }

    public void addDespesa(String tipo, String descricao, BigDecimal valor, Participante[] participantes) {
        if (descricao == null || descricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição da despesa");
        }
        if (participantes == null || participantes.length == 0) {
            throw new IllegalArgumentException("Selecione ao menos um participante para a despesa");
        }
        String descricaoLimpa = descricao.trim();
        if ("porPessoa".equals(tipo)) {
            despesas.add(new DespesaPorPessoa(descricaoLimpa, valor, participantes));
        } else {
            despesas.add(new DespesaFixa(descricaoLimpa, valor, participantes));
        }
    }

    public Despesa buscarDespesa(int codigo_unico_despesa) {
        for (int i = 0; i < despesas.size(); i++) {
            Despesa despesa = despesas.get(i);
            if (despesa.getCodigo_unico_despesa() == codigo_unico_despesa) {
                return despesa;
            }
        }
        return null;
    }

    public void editarDespesa(int codigo_unico_despesa, String novaDescricao, BigDecimal novoValor, Participante[] novosParticipantes) {
        if (novaDescricao == null || novaDescricao.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe a descrição da despesa");
        }
        if (novoValor == null || novoValor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor de despesa inválido");
        }
        if (novosParticipantes == null || novosParticipantes.length == 0) {
            throw new IllegalArgumentException("Selecione ao menos um participante para a despesa");
        }

        Despesa despesa = buscarDespesa(codigo_unico_despesa);
        if (despesa == null) {
            throw new IllegalArgumentException("Despesa não encontrada");
        }

        despesa.setDescricao(novaDescricao);
        despesa.setParticipantes(novosParticipantes);
        despesa.setValorInformado(novoValor);
    }

    public void removerDespesa(int codigo_unico_despesa) {
        despesas.removeIf(despesa -> despesa.getCodigo_unico_despesa() == codigo_unico_despesa);
    }

    public Map<String, Object> getResumo() {
        Map<String, Object> resumo = new HashMap<>();
        resumo.put("codigo", codigo_unico_planejamento);
        resumo.put("titulo", titulo);

        Map<Integer, Participante> participantesUnicos = new TreeMap<>();
        BigDecimal totalGeral = new BigDecimal("0.00");

        for (int i = 0; i < despesas.size(); i++) {
            Despesa despesa = despesas.get(i);
            totalGeral = totalGeral.add(despesa.getValor());

            Participante[] participantesDaDespesa = despesa.getParticipantes();
            for (int j = 0; j < participantesDaDespesa.length; j++) {
                Participante p = participantesDaDespesa[j];
                participantesUnicos.putIfAbsent(p.getCodigo_unico_participante(), p);
            }
        }

        List<Participante> listaParticipantes = new ArrayList<>(participantesUnicos.values());
        List<Map<String, Object>> valoresPorParticipante = new ArrayList<>();

        for (int i = 0; i < listaParticipantes.size(); i++) {
            Participante participante = listaParticipantes.get(i);

            Map<String, Object> linhaParticipante = new LinkedHashMap<>();
            linhaParticipante.put("codigo", participante.getCodigo_unico_participante());
            linhaParticipante.put("nome", participante.getNome());

            Map<Integer, BigDecimal> valoresPorDespesa = new LinkedHashMap<>();
            BigDecimal totalParticipante = new BigDecimal("0.00");

            for (int j = 0; j < despesas.size(); j++) {
                Despesa despesa = despesas.get(j);
                BigDecimal valorNaDespesa = despesa.getValorDoParticipante(participante);

                valoresPorDespesa.put(despesa.getCodigo_unico_despesa(), valorNaDespesa);
                totalParticipante = totalParticipante.add(valorNaDespesa);
            }

            boolean orcamentoOk = participante.orcamentoOk(totalParticipante);
            BigDecimal valorExcedido = orcamentoOk
                    ? new BigDecimal("0.00")
                    : totalParticipante.subtract(participante.getOrcamento());

            linhaParticipante.put("valoresPorDespesa", valoresPorDespesa);
            linhaParticipante.put("total", totalParticipante);
            linhaParticipante.put("orcamentoOk", orcamentoOk);
            linhaParticipante.put("valorExcedido", valorExcedido);
            valoresPorParticipante.add(linhaParticipante);
        }

        resumo.put("despesas", List.copyOf(despesas));
        resumo.put("valoresPorParticipante", valoresPorParticipante);
        resumo.put("totalGeral", totalGeral);

        return resumo;
    }

    public List<Despesa> getDespesas() {
        return List.copyOf(despesas);
    }

    public int getCodigo_unico_planejamento() {
        return codigo_unico_planejamento;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o título do planejamento");
        }
        this.titulo = titulo.trim();
    }
}
