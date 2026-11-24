package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Repositorio implements Persistencia {
    private String caminhoArquivo;
    private Map<Integer, Participante> participantes = new LinkedHashMap<>();
    private Map<Integer, Planejamento> planejamentos = new LinkedHashMap<>();

    public Repositorio() {
        this("dados.dat");
    }

    public Repositorio(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
    }

    @Override
    public void addParticipante(Participante participante) {
        if (participante == null) {
            throw new IllegalArgumentException("Participante inválido");
        }
        participantes.put(participante.getCodigo_unico_participante(), participante);
        salvar();
    }

    @Override
    public Map<Integer, Participante> getParticipantes() {
        return Collections.unmodifiableMap(participantes);
    }

    @Override
    public Participante buscarParticipante(int codigo_unico_participante) {
        return participantes.get(codigo_unico_participante);
    }

    @Override
    public boolean removerParticipante(int codigo_unico_participante) {
        if (!participantes.containsKey(codigo_unico_participante)) {
            return false;
        }
        if (participanteAssociadoADespesa(codigo_unico_participante)) {
            throw new IllegalArgumentException(
                    "Não é possível remover o participante pois ele está associado a uma ou mais despesas."
            );
        }
        boolean removido = participantes.remove(codigo_unico_participante) != null;
        if (removido) {
            salvar();
        }
        return removido;
    }

    private boolean participanteAssociadoADespesa(int codigoParticipante) {
        for (Planejamento planejamento : planejamentos.values()) {
            for (Despesa despesa : planejamento.getDespesas()) {
                for (Participante p : despesa.getParticipantes()) {
                    if (p != null && p.getCodigo_unico_participante() == codigoParticipante) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addPlanejamento(Planejamento planejamento) {
        if (planejamento == null) {
            throw new IllegalArgumentException("Planejamento inválido");
        }
        planejamentos.put(planejamento.getCodigo_unico_planejamento(), planejamento);
        salvar();
    }

    @Override
    public Map<Integer, Planejamento> getPlanejamentos() {
        return Collections.unmodifiableMap(planejamentos);
    }

    @Override
    public Planejamento buscarPlanejamento(int codigo_unico_planejamento) {
        return planejamentos.get(codigo_unico_planejamento);
    }

    @Override
    public boolean removerPlanejamento(int codigo_unico_planejamento) {
        boolean removido = planejamentos.remove(codigo_unico_planejamento) != null;
        if (removido) {
            salvar();
        }
        return removido;
    }

    @Override
    public void iniciarVazio() {
        this.participantes = new LinkedHashMap<>();
        this.planejamentos = new LinkedHashMap<>();
    }

    @Override
    public void iniciarNovoArquivo(File novoArquivo) throws IOException {
        if (novoArquivo == null) {
            throw new IOException("Arquivo de destino inválido.");
        }
        Map<Integer, Participante> antigosParticipantes = this.participantes;
        Map<Integer, Planejamento> antigosPlanejamentos = this.planejamentos;
        String antigoCaminho = this.caminhoArquivo;

        try {
            this.participantes = new LinkedHashMap<>();
            this.planejamentos = new LinkedHashMap<>();
            salvar(novoArquivo);
        } catch (IOException e) {
            this.participantes = antigosParticipantes;
            this.planejamentos = antigosPlanejamentos;
            this.caminhoArquivo = antigoCaminho;
            throw e;
        }
    }

    @Override
    public String getCaminhoArquivo() {
        return caminhoArquivo;
    }

    @Override
    public void salvar() {
        try {
            salvar(new File(caminhoArquivo));
        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar dados no arquivo: " + caminhoArquivo, e);
        }
    }

    @Override
    public void salvar(File arquivo) throws IOException {
        if (arquivo == null) {
            throw new IOException("Arquivo de destino inválido.");
        }
        try (FileOutputStream fos = new FileOutputStream(arquivo);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {
            out.writeObject(participantes);
            out.writeObject(planejamentos);
            this.caminhoArquivo = arquivo.getPath();
        }
    }

    @Override
    public void carregar() throws IOException {
        carregar(new File(caminhoArquivo));
    }

    @Override
    public void carregar(File arquivo) throws IOException {
        if (arquivo == null || !arquivo.exists() || !arquivo.isFile()) {
            String nome = (arquivo == null) ? "nulo" : arquivo.getPath();
            throw new FileNotFoundException("Arquivo de dados não encontrado: " + nome);
        }

        Map<Integer, Participante> novosParticipantes;
        Map<Integer, Planejamento> novosPlanejamentos;

        try (FileInputStream fis = new FileInputStream(arquivo);
             ObjectInputStream in = new ObjectInputStream(fis)) {
            Object objParticipantes = in.readObject();
            Object objPlanejamentos = in.readObject();

            novosParticipantes = validarMapaParticipantes(objParticipantes);
            novosPlanejamentos = validarMapaPlanejamentos(objPlanejamentos);
        } catch (IOException e) {
            throw new IOException("Arquivo de dados inválido ou corrompido: " + arquivo.getPath(), e);
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Formato incompatível no arquivo: " + arquivo.getPath(), e);
        }

        // Só substitui os dados em uso depois de validar todo o carregamento com sucesso
        this.participantes = novosParticipantes;
        this.planejamentos = novosPlanejamentos;
        this.caminhoArquivo = arquivo.getPath();
        sincronizarContadores();
    }

    private Map<Integer, Participante> validarMapaParticipantes(Object obj) throws IOException {
        if (!(obj instanceof Map<?, ?> bruto)) {
            throw new IOException("Estrutura de participantes inválida.");
        }
        Map<Integer, Participante> validado = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : bruto.entrySet()) {
            if (!(entry.getKey() instanceof Integer chave) || !(entry.getValue() instanceof Participante valor)) {
                throw new IOException("Registro de participante inválido.");
            }
            validado.put(chave, valor);
        }
        return validado;
    }

    private Map<Integer, Planejamento> validarMapaPlanejamentos(Object obj) throws IOException {
        if (!(obj instanceof Map<?, ?> bruto)) {
            throw new IOException("Estrutura de planejamentos inválida.");
        }
        Map<Integer, Planejamento> validado = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : bruto.entrySet()) {
            if (!(entry.getKey() instanceof Integer chave) || !(entry.getValue() instanceof Planejamento valor)) {
                throw new IOException("Registro de planejamento inválido.");
            }
            validado.put(chave, valor);
        }
        return validado;
    }

    private void sincronizarContadores() {
        int maiorParticipante = 0;
        List<Participante> listaParticipantes = new ArrayList<>(participantes.values());
        for (int i = 0; i < listaParticipantes.size(); i++) {
            int codigo = listaParticipantes.get(i).getCodigo_unico_participante();
            if (codigo > maiorParticipante) {
                maiorParticipante = codigo;
            }
        }

        int maiorPlanejamento = 0;
        int maiorDespesa = 0;
        List<Planejamento> listaPlanejamentos = new ArrayList<>(planejamentos.values());
        for (int i = 0; i < listaPlanejamentos.size(); i++) {
            Planejamento planejamento = listaPlanejamentos.get(i);
            if (planejamento.getCodigo_unico_planejamento() > maiorPlanejamento) {
                maiorPlanejamento = planejamento.getCodigo_unico_planejamento();
            }

            List<Despesa> despesas = planejamento.getDespesas();
            for (int j = 0; j < despesas.size(); j++) {
                Despesa despesa = despesas.get(j);
                if (despesa.getCodigo_unico_despesa() > maiorDespesa) {
                    maiorDespesa = despesa.getCodigo_unico_despesa();
                }

                Participante[] participantesDespesa = despesa.getParticipantes();
                for (int k = 0; k < participantesDespesa.length; k++) {
                    int codigo = participantesDespesa[k].getCodigo_unico_participante();
                    if (codigo > maiorParticipante) {
                        maiorParticipante = codigo;
                    }
                }
            }
        }

        Participante.atualizarContador(maiorParticipante);
        Planejamento.atualizarContador(maiorPlanejamento);
        Despesa.atualizarContador(maiorDespesa);
    }
}
