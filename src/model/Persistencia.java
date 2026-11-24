package model;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface Persistencia {
    void addParticipante(Participante participante);

    Map<Integer, Participante> getParticipantes();

    Participante buscarParticipante(int codigo_unico_participante);

    boolean removerParticipante(int codigo_unico_participante);

    void addPlanejamento(Planejamento planejamento);

    Map<Integer, Planejamento> getPlanejamentos();

    Planejamento buscarPlanejamento(int codigo_unico_planejamento);

    boolean removerPlanejamento(int codigo_unico_planejamento);

    void iniciarVazio();

    void iniciarNovoArquivo(File novoArquivo) throws IOException;

    String getCaminhoArquivo();

    void salvar();

    void salvar(File arquivo) throws IOException;

    void carregar() throws IOException;

    void carregar(File arquivo) throws IOException;
}
