package controller;

import model.Participante;
import model.Planejamento;
import model.Repositorio;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlanejadorController {
    private final Repositorio repositorio;

    public PlanejadorController(Repositorio repositorio) {
        this.repositorio = repositorio;
    }

    public void carregarArquivoPadrao() throws IOException {
        repositorio.carregar();
    }

    public void carregarArquivo(File arquivo) throws IOException {
        repositorio.carregar(arquivo);
    }

    public void salvarArquivoComo(File arquivo) throws IOException {
        repositorio.salvar(arquivo);
    }

    public void iniciarPlanejamentoVazio() {
        repositorio.iniciarVazio();
    }

    public void iniciarNovoArquivoVazio(File novoArquivo) throws IOException {
        repositorio.iniciarNovoArquivo(novoArquivo);
    }

    public String getCaminhoArquivoAtual() {
        return repositorio.getCaminhoArquivo();
    }

    public Participante cadastrarParticipante(String nome, BigDecimal orcamento) {
        Participante participante = new Participante(nome, orcamento);
        repositorio.addParticipante(participante);
        return participante;
    }

    public List<Participante> listarParticipantes() {
        return new ArrayList<>(repositorio.getParticipantes().values());
    }

    public Participante buscarParticipante(int codigoParticipante) {
        return repositorio.buscarParticipante(codigoParticipante);
    }

    public void editarParticipante(int codigoParticipante, String novoNome, BigDecimal novoOrcamento) {
        if (novoNome == null || novoNome.trim().isEmpty()) {
            throw new IllegalArgumentException("Informe o nome do participante");
        }
        if (novoOrcamento == null || novoOrcamento.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor de orçamento inválido");
        }

        Participante participante = repositorio.buscarParticipante(codigoParticipante);
        if (participante == null) {
            throw new IllegalArgumentException("Participante não encontrado");
        }

        participante.setNome(novoNome);
        participante.setOrcamento(novoOrcamento);
        repositorio.salvar();
    }

    public boolean removerParticipante(int codigoParticipante) {
        return repositorio.removerParticipante(codigoParticipante);
    }

    public Planejamento criarPlanejamento(String titulo) {
        Planejamento planejamento = new Planejamento(titulo);
        repositorio.addPlanejamento(planejamento);
        return planejamento;
    }

    public List<Planejamento> listarPlanejamentos() {
        return new ArrayList<>(repositorio.getPlanejamentos().values());
    }

    public Planejamento buscarPlanejamento(int codigoPlanejamento) {
        return repositorio.buscarPlanejamento(codigoPlanejamento);
    }

    public boolean removerPlanejamento(int codigoPlanejamento) {
        return repositorio.removerPlanejamento(codigoPlanejamento);
    }

    public void adicionarDespesa(int codigoPlanejamento, String tipo, String descricao,
                                 BigDecimal valor, List<Integer> codigosParticipantes) {
        Planejamento planejamento = repositorio.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }

        Participante[] participantes = resolverParticipantes(codigosParticipantes);
        planejamento.addDespesa(tipo, descricao, valor, participantes);
        repositorio.salvar();
    }

    public void editarDespesa(int codigoPlanejamento, int codigoDespesa, String novaDescricao,
                              BigDecimal novoValor, List<Integer> codigosParticipantes) {
        Planejamento planejamento = repositorio.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }

        Participante[] participantes = resolverParticipantes(codigosParticipantes);
        planejamento.editarDespesa(codigoDespesa, novaDescricao, novoValor, participantes);
        repositorio.salvar();
    }

    public void removerDespesa(int codigoPlanejamento, int codigoDespesa) {
        Planejamento planejamento = repositorio.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            throw new IllegalArgumentException("Selecione um planejamento válido");
        }
        planejamento.removerDespesa(codigoDespesa);
        repositorio.salvar();
    }

    public Map<String, Object> gerarResumo(int codigoPlanejamento) {
        Planejamento planejamento = repositorio.buscarPlanejamento(codigoPlanejamento);
        if (planejamento == null) {
            return null;
        }
        return planejamento.getResumo();
    }

    private Participante[] resolverParticipantes(List<Integer> codigosParticipantes) {
        Participante[] participantes = new Participante[codigosParticipantes.size()];
        for (int i = 0; i < codigosParticipantes.size(); i++) {
            Participante p = repositorio.buscarParticipante(codigosParticipantes.get(i));
            if (p == null) {
                throw new IllegalArgumentException("Participante inválido selecionado");
            }
            participantes[i] = p;
        }
        return participantes;
    }

    public Repositorio getRepositorio() {
        return repositorio;
    }
}
