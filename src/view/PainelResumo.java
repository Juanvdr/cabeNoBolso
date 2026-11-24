package view;

import controller.PlanejadorController;
import model.Despesa;
import model.Participante;
import model.Planejamento;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PainelResumo extends JPanel {
    private final PlanejadorController controller;
    private final NumberFormat formatoMoeda;
    private final NumberFormat formatoNumero;

    private final JComboBox<String> comboPlanejamentos;
    private final List<Planejamento> planejamentosCarregados = new ArrayList<>();
    private boolean atualizandoCombo = false;

    private final DefaultTableModel modeloTabelaDespesas;
    private final DefaultTableModel modeloTabelaParticipantes;
    private final JLabel labelTotalGeral;

    public PainelResumo(PlanejadorController controller) {
        this.controller = controller;
        Locale ptBr = Locale.of("pt", "BR");
        this.formatoMoeda = NumberFormat.getCurrencyInstance(ptBr);
        this.formatoNumero = NumberFormat.getNumberInstance(ptBr);
        this.formatoNumero.setMinimumFractionDigits(2);
        this.formatoNumero.setMaximumFractionDigits(2);

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Topo: seletor de planejamento
        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        comboPlanejamentos = new JComboBox<>();
        comboPlanejamentos.setPreferredSize(new Dimension(280, 26));
        painelTopo.add(new JLabel("Planejamento:"));
        painelTopo.add(comboPlanejamentos);

        // Centro: duas tabelas empilhadas (Despesas + Valores esperados por participante)
        JPanel painelTabelas = new JPanel();
        painelTabelas.setLayout(new BoxLayout(painelTabelas, BoxLayout.Y_AXIS));

        // Tabela 1: Despesas
        String[] colunasDespesas = {"Despesa", "Tipo", "Valor informado", "Participantes"};
        modeloTabelaDespesas = new DefaultTableModel(colunasDespesas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabelaDespesas = new JTable(modeloTabelaDespesas);
        tabelaDespesas.getTableHeader().setReorderingAllowed(false);

        JPanel secaoDespesas = new JPanel(new BorderLayout(5, 5));
        secaoDespesas.setBorder(BorderFactory.createTitledBorder("Despesas do Planejamento"));
        secaoDespesas.add(new JScrollPane(tabelaDespesas), BorderLayout.CENTER);
        secaoDespesas.setPreferredSize(new Dimension(0, 180));

        // Tabela 2: Valores esperados por participante
        JPanel secaoParticipantes = new JPanel(new BorderLayout(5, 5));
        secaoParticipantes.setBorder(BorderFactory.createTitledBorder("Valores por participante (em R$)"));

        modeloTabelaParticipantes = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable tabelaParticipantes = new JTable(modeloTabelaParticipantes);
        tabelaParticipantes.getTableHeader().setReorderingAllowed(false);

        secaoParticipantes.add(new JScrollPane(tabelaParticipantes), BorderLayout.CENTER);

        painelTabelas.add(secaoDespesas);
        painelTabelas.add(Box.createVerticalStrut(10));
        painelTabelas.add(secaoParticipantes);

        // Rodapé: Total geral
        JPanel painelRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        labelTotalGeral = new JLabel("Total geral do planejamento: R$ 0,00");
        labelTotalGeral.setFont(labelTotalGeral.getFont().deriveFont(Font.BOLD, 14f));
        painelRodape.add(labelTotalGeral);

        add(painelTopo, BorderLayout.NORTH);
        add(painelTabelas, BorderLayout.CENTER);
        add(painelRodape, BorderLayout.SOUTH);

        comboPlanejamentos.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!atualizandoCombo) {
                    exibirResumoSelecionado();
                }
            }
        });

        atualizarTudo();
    }

    public void atualizarTudo() {
        Integer codigoAnterior = null;
        int indiceAnterior = comboPlanejamentos.getSelectedIndex();
        if (indiceAnterior >= 0 && indiceAnterior < planejamentosCarregados.size()) {
            codigoAnterior = planejamentosCarregados.get(indiceAnterior).getCodigo_unico_planejamento();
        }

        atualizandoCombo = true;
        comboPlanejamentos.removeAllItems();
        planejamentosCarregados.clear();

        List<Planejamento> planejamentos = controller.listarPlanejamentos();
        int indiceParaSelecionar = -1;

        for (int i = 0; i < planejamentos.size(); i++) {
            Planejamento p = planejamentos.get(i);
            planejamentosCarregados.add(p);
            comboPlanejamentos.addItem(p.getCodigo_unico_planejamento() + " - " + p.getTitulo());

            if (codigoAnterior != null && p.getCodigo_unico_planejamento() == codigoAnterior) {
                indiceParaSelecionar = i;
            }
        }

        if (indiceParaSelecionar >= 0) {
            comboPlanejamentos.setSelectedIndex(indiceParaSelecionar);
        } else if (!planejamentosCarregados.isEmpty()) {
            comboPlanejamentos.setSelectedIndex(0);
        }
        atualizandoCombo = false;

        exibirResumoSelecionado();
    }

    @SuppressWarnings("unchecked")
    private void exibirResumoSelecionado() {
        modeloTabelaDespesas.setRowCount(0);
        modeloTabelaParticipantes.setRowCount(0);
        modeloTabelaParticipantes.setColumnCount(0);
        labelTotalGeral.setText("Total geral do planejamento: " + formatoMoeda.format(BigDecimal.ZERO));

        int indice = comboPlanejamentos.getSelectedIndex();
        if (indice < 0 || indice >= planejamentosCarregados.size()) {
            return;
        }

        int codigoPlanejamento = planejamentosCarregados.get(indice).getCodigo_unico_planejamento();
        Map<String, Object> resumo = controller.gerarResumo(codigoPlanejamento);
        if (resumo == null) {
            return;
        }

        List<Despesa> despesas = (List<Despesa>) resumo.get("despesas");
        List<Map<String, Object>> valoresPorParticipante = (List<Map<String, Object>>) resumo.get("valoresPorParticipante");
        BigDecimal totalGeral = (BigDecimal) resumo.get("totalGeral");

        // Preenche Tabela 1: Despesas
        for (int i = 0; i < despesas.size(); i++) {
            Despesa d = despesas.get(i);
            String tipoLegivel = "porPessoa".equals(d.getTipo()) ? "Por pessoa" : "Fixa";
            modeloTabelaDespesas.addRow(new Object[]{
                    d.getDescricao(),
                    tipoLegivel,
                    formatoMoeda.format(d.getValorInformado()),
                    formatarNomesParticipantes(d.getParticipantes())
            });
        }

        // Monta colunas dinâmicas da Tabela 2
        modeloTabelaParticipantes.addColumn("Pessoa");
        for (int i = 0; i < despesas.size(); i++) {
            modeloTabelaParticipantes.addColumn(despesas.get(i).getDescricao());
        }
        modeloTabelaParticipantes.addColumn("Total");
        modeloTabelaParticipantes.addColumn("Orçamento");

        // Preenche linhas da Tabela 2
        for (int i = 0; i < valoresPorParticipante.size(); i++) {
            Map<String, Object> linha = valoresPorParticipante.get(i);
            String nome = (String) linha.get("nome");
            Map<Integer, BigDecimal> valoresPorDespesa = (Map<Integer, BigDecimal>) linha.get("valoresPorDespesa");
            BigDecimal totalPessoa = (BigDecimal) linha.get("total");
            boolean orcamentoOk = (boolean) linha.get("orcamentoOk");
            BigDecimal valorExcedido = (BigDecimal) linha.get("valorExcedido");

            Object[] dadosLinha = new Object[despesas.size() + 3];
            dadosLinha[0] = nome;

            for (int j = 0; j < despesas.size(); j++) {
                int codigoDespesa = despesas.get(j).getCodigo_unico_despesa();
                BigDecimal valor = valoresPorDespesa.get(codigoDespesa);
                dadosLinha[j + 1] = formatoMoeda.format(valor);
            }

            dadosLinha[despesas.size() + 1] = formatoMoeda.format(totalPessoa);
            dadosLinha[despesas.size() + 2] = orcamentoOk
                    ? "OK"
                    : "Excedido em " + formatoMoeda.format(valorExcedido);

            modeloTabelaParticipantes.addRow(dadosLinha);
        }

        labelTotalGeral.setText("Total geral do planejamento: " + formatoMoeda.format(totalGeral));
    }

    private String formatarNomesParticipantes(Participante[] participantes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < participantes.length; i++) {
            if (i > 0) {
                if (i == participantes.length - 1) {
                    sb.append(" e ");
                } else {
                    sb.append(", ");
                }
            }
            sb.append(participantes[i].getNome());
        }
        return sb.toString();
    }
}
