package controller;

import model.Participante;
import model.Persistencia;
import model.Planejamento;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlanejadorController {
    private final Persistencia persistencia;

    public PlanejadorController(Persistencia persistencia) {
        if (persistencia == null) {
            throw new IllegalArgumentException("Persistência inválida");
        }
        this.persistencia = persistencia;
    }

    public void carregarArquivoPadrao() throws IOException {
        persistencia.carregar();
    }

    public void carregarArquivo(File arquivo) throws IOException {
        persistencia.carregar(arquivo);
    }

    public void salvarArquivoComo(File arquivo) throws IOException {
        persistencia.salvar(arquivo);
    }

    public void iniciarPlanejamentoVazio() {
        persistencia.iniciarVazio();
    }

    public void iniciarNovoArquivoVazio(File novoArquivo) throws IOException {
        persistencia.iniciarNovoArquivo(novoArquivo);
    }

    public String getCaminhoArquivoAtual() {
        return persistencia.getCaminhoArquivo();
    }

    public Participante cadastrarParticipante(String nome, BigDecimal orcamento) {
        Participante participante = new Participante(nome, orcamento);
        persistencia.addParticipante(participante);
        return participante;
    }

    public List<Participante> listarParticipantes() {
        return new ArrayList<>(persistencia.getParticipantes().values());
    }

    public Participante buscarParticipante(int codigoParticipante) {
        return persistencia.buscarParticipante(codigoParticipante);
    }

    public void editarParticipante(int codigoParticipante, String novoNome, BigDecimal novoOrcamento) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do participante");
        }
        if (novoOrcamento == null || novoOrcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de orçamento inválido");
        }

        Participante participante = persistencia.buscarParticipante(codigoParticipante);
        if (participante == null) {
            throw new IllegalArgumentException("Participante não encontrado");
        }

        participante.setNome(novoNome);
        participante.setOrcamento(novoOrcamento);
        persistencia.salvar();
    }

    public boolean removerParticipante(int codigoParticipante) {
        return persistencia.removerParticipante(codigoParticipante);
    }

    public Planejamento criarPlanejamento(String titulo) {
        Planejamento planejamento = new Planejamento(titulo);
        persistencia.addPlanejamento(planejamento);
        return planejamento;
    }

    public List<Planejamento> listarPlanejamentos() {
        return new ArrayList<>(persistencia.getPlanejamentos().values());
    }

    public Planejamento buscarPlanejamento(int codigoPlanejamento) {
        return persistencia.buscarPlanejamento(codigoPlanejamento);
    }

    public boolean removerPlanejamento(int codigoPlanejamento) {
        return persistencia.removerPlanejamento(codigoPlanejamento);
    }

    public void adicionarDespesa(int codigoPlanejamento, String tipo, String descricao,
                                 BigDecimal valor, List<Integer> codigosParticipantes) {
        Planejamento planejamento = persistencia.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }

        Participante[] participantes = resolverParticipantes(codigosParticipantes);
        planejamento.addDespesa(tipo, descricao, valor, participantes);
        persistencia.salvar();
    }

    public void editarDespesa(int codigoPlanejamento, int codigoDespesa, String novaDescricao,
                              BigDecimal novoValor, List<Integer> codigosParticipantes) {
        Planejamento planejamento = persistencia.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }

        Participante[] participantes = resolverParticipantes(codigosParticipantes);
        planejamento.editarDespesa(codigoDespesa, novaDescricao, novoValor, participantes);
        persistencia.salvar();
    }

    public void removerDespesa(int codigoPlanejamento, int codigoDespesa) {
        Planejamento planejamento = persistencia.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }
        planejamento.removerDespesa(codigoDespesa);
        persistencia.salvar();
    }

    public Map<String, Object> gerarResumo(int codigoPlanejamento) {
        Planejamento planejamento = persistencia.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            return null;
        }
        return planejamento.getResumo();
    }

    private Participante[] resolverParticipantes(List<Integer> codigosParticipantes) {
        Participante[] participantes = new Participante[codigosParticipantes.size()];
        for (int i = 0; i < codigosParticipantes.size(); i++) {
            Participante p = persistencia.buscarParticipante(codigosParticipantes.get(i));
            if (p == null) {
                throw new IllegalArgumentException("Participante inválido selecionado");
            }
            participantes[i] = p;
        }
        return participantes;
    }

    public Persistencia getPersistencia() {
        return persistencia;
    }
}
