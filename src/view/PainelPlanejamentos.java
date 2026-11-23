package view;

import controller.PlanejadorController;
import model.Despesa;
import model.Participante;
import model.Planejamento;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PainelPlanejamentos extends JPanel {
    private final PlanejadorController controller;
    private final NumberFormat formatoMoeda;

    // Lado esquerdo: planejamentos
    private final JTextField campoTitulo;
    private final DefaultTableModel modeloPlanejamentos;
    private final JTable tabelaPlanejamentos;

    // Lado direito: despesas do planejamento selecionado
    private final JLabel labelPlanejamentoSelecionado;
    private final JTextField campoDescricao;
    private final JComboBox<String> comboTipo;
    private final JTextField campoValor;
    private final JPanel painelCheckboxes;
    private final List<JCheckBox> checkboxesParticipantes = new ArrayList<>();
    private final List<Participante> participantesCarregados = new ArrayList<>();
    private final DefaultTableModel modeloDespesas;
    private final JTable tabelaDespesas;

    private Integer codigoPlanejamentoSelecionado = null;

    public PainelPlanejamentos(PlanejadorController controller) {
        this.controller = controller;
        this.formatoMoeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // --- Painel Esquerdo: Planejamentos ---
        JPanel painelEsquerdo = new JPanel(new BorderLayout(8, 8));
        painelEsquerdo.setBorder(BorderFactory.createTitledBorder("Planejamentos"));
        painelEsquerdo.setPreferredSize(new Dimension(280, 0));

        JPanel formPlanejamento = new JPanel(new BorderLayout(5, 5));
        campoTitulo = new JTextField();
        JButton botaoCriarPlanejamento = new JButton("Criar");
        formPlanejamento.add(new JLabel("Título:"), BorderLayout.WEST);
        formPlanejamento.add(campoTitulo, BorderLayout.CENTER);
        formPlanejamento.add(botaoCriarPlanejamento, BorderLayout.EAST);

        String[] colunasPlan = {"Código", "Título"};
        modeloPlanejamentos = new DefaultTableModel(colunasPlan, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaPlanejamentos = new JTable(modeloPlanejamentos);
        tabelaPlanejamentos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPlanejamentos.getTableHeader().setReorderingAllowed(false);

        JButton botaoRemoverPlanejamento = new JButton("Remover planejamento");
        JPanel rodapePlan = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodapePlan.add(botaoRemoverPlanejamento);

        painelEsquerdo.add(formPlanejamento, BorderLayout.NORTH);
        painelEsquerdo.add(new JScrollPane(tabelaPlanejamentos), BorderLayout.CENTER);
        painelEsquerdo.add(rodapePlan, BorderLayout.SOUTH);

        // --- Painel Direito: Despesas ---
        JPanel painelDireito = new JPanel(new BorderLayout(8, 8));
        painelDireito.setBorder(BorderFactory.createTitledBorder("Despesas"));

        JPanel topoDespesas = new JPanel(new BorderLayout(5, 8));
        labelPlanejamentoSelecionado = new JLabel("Selecione um planejamento à esquerda");
        topoDespesas.add(labelPlanejamentoSelecionado, BorderLayout.NORTH);

        JPanel formDespesa = new JPanel(new GridBagLayout());
        formDespesa.setBorder(BorderFactory.createTitledBorder("Nova Despesa"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        campoDescricao = new JTextField(16);
        comboTipo = new JComboBox<>(new String[]{"Fixa", "Por pessoa"});
        campoValor = new JTextField(10);
        painelCheckboxes = new JPanel();
        painelCheckboxes.setLayout(new BoxLayout(painelCheckboxes, BoxLayout.Y_AXIS));
        JScrollPane scrollCheckboxes = new JScrollPane(painelCheckboxes);
        scrollCheckboxes.setPreferredSize(new Dimension(200, 80));
        JButton botaoAdicionarDespesa = new JButton("Adicionar despesa");

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formDespesa.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formDespesa.add(campoDescricao, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        formDespesa.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 3; gbc.weightx = 0.5;
        formDespesa.add(comboTipo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formDespesa.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formDespesa.add(campoValor, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formDespesa.add(new JLabel("Participantes:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        formDespesa.add(scrollCheckboxes, gbc);

        gbc.gridx = 3; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        formDespesa.add(botaoAdicionarDespesa, gbc);

        topoDespesas.add(formDespesa, BorderLayout.CENTER);

        String[] colunasDesp = {"Código", "Descrição", "Tipo", "Valor informado", "Total", "Participantes"};
        modeloDespesas = new DefaultTableModel(colunasDesp, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaDespesas = new JTable(modeloDespesas);
        tabelaDespesas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaDespesas.getTableHeader().setReorderingAllowed(false);

        JButton botaoEditarDespesa = new JButton("Editar despesa selecionada");
        JButton botaoRemoverDespesa = new JButton("Remover despesa selecionada");
        JPanel rodapeDesp = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodapeDesp.add(botaoEditarDespesa);
        rodapeDesp.add(botaoRemoverDespesa);

        painelDireito.add(topoDespesas, BorderLayout.NORTH);
        painelDireito.add(new JScrollPane(tabelaDespesas), BorderLayout.CENTER);
        painelDireito.add(rodapeDesp, BorderLayout.SOUTH);

        JSplitPane divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, painelDireito);
        divisor.setDividerLocation(290);
        add(divisor, BorderLayout.CENTER);

        // Eventos
        botaoCriarPlanejamento.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarPlanejamento();
            }
        });

        botaoRemoverPlanejamento.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerPlanejamento();
            }
        });

        tabelaPlanejamentos.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    aoSelecionarPlanejamento();
                }
            }
        });

        botaoAdicionarDespesa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adicionarDespesa();
            }
        });

        botaoEditarDespesa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarDespesa();
            }
        });

        botaoRemoverDespesa.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerDespesa();
            }
        });

        atualizarTudo();
    }

    private void criarPlanejamento() {
        try {
            Planejamento criado = controller.criarPlanejamento(campoTitulo.getText());
            campoTitulo.setText("");
            atualizarTabelaPlanejamentos();
            selecionarPlanejamentoPorCodigo(criado.getCodigo_unico_planejamento());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerPlanejamento() {
        if (codigoPlanejamentoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um planejamento para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja remover o planejamento selecionado e todas as suas despesas?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmacao == JOptionPane.YES_OPTION) {
            controller.removerPlanejamento(codigoPlanejamentoSelecionado);
            codigoPlanejamentoSelecionado = null;
            atualizarTudo();
        }
    }

    private void aoSelecionarPlanejamento() {
        int linha = tabelaPlanejamentos.getSelectedRow();
        if (linha == -1) {
            codigoPlanejamentoSelecionado = null;
            labelPlanejamentoSelecionado.setText("Selecione um planejamento à esquerda");
        } else {
            codigoPlanejamentoSelecionado = (int) modeloPlanejamentos.getValueAt(linha, 0);
            String titulo = (String) modeloPlanejamentos.getValueAt(linha, 1);
            labelPlanejamentoSelecionado.setText("Planejamento selecionado: " + codigoPlanejamentoSelecionado + " - " + titulo);
        }
        atualizarTabelaDespesas();
    }

    private void adicionarDespesa() {
        if (codigoPlanejamentoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione ou crie um planejamento primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String textoValor = campoValor.getText().trim().replace(",", ".");
        try {
            if (textoValor.isEmpty()) {
                throw new IllegalArgumentException("Informe o valor da despesa");
            }
            BigDecimal valor = new BigDecimal(textoValor);
            String tipoSelecionado = comboTipo.getSelectedIndex() == 1 ? "porPessoa" : "fixa";

            List<Integer> codigosSelecionados = new ArrayList<>();
            for (int i = 0; i < checkboxesParticipantes.size(); i++) {
                if (checkboxesParticipantes.get(i).isSelected()) {
                    codigosSelecionados.add(participantesCarregados.get(i).getCodigo_unico_participante());
                }
            }

            controller.adicionarDespesa(
                    codigoPlanejamentoSelecionado,
                    tipoSelecionado,
                    campoDescricao.getText(),
                    valor,
                    codigosSelecionados
            );

            campoDescricao.setText("");
            campoValor.setText("");
            for (int i = 0; i < checkboxesParticipantes.size(); i++) {
                checkboxesParticipantes.get(i).setSelected(false);
            }
            campoDescricao.requestFocus();
            atualizarTabelaDespesas();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Digite um número (ex: 90,00).", "Erro de validação", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarDespesa() {
        if (codigoPlanejamentoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um planejamento primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int linha = tabelaDespesas.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Planejamento plan = controller.buscarPlanejamento(codigoPlanejamentoSelecionado);
        if (plan == null) {
            return;
        }

        int codigoDespesa = (int) modeloDespesas.getValueAt(linha, 0);
        Despesa despesa = plan.buscarDespesa(codigoDespesa);
        if (despesa == null) {
            return;
        }

        JTextField editDescricao = new JTextField(despesa.getDescricao(), 18);
        String tipoTexto = "porPessoa".equals(despesa.getTipo()) ? "Por pessoa" : "Fixa";
        JTextField editValor = new JTextField(despesa.getValorInformado().toPlainString().replace(".", ","), 10);

        JPanel editCheckboxesPanel = new JPanel();
        editCheckboxesPanel.setLayout(new BoxLayout(editCheckboxesPanel, BoxLayout.Y_AXIS));
        List<Participante> todosParticipantes = controller.listarParticipantes();
        List<JCheckBox> editCheckboxes = new ArrayList<>();

        for (int i = 0; i < todosParticipantes.size(); i++) {
            Participante p = todosParticipantes.get(i);
            JCheckBox check = new JCheckBox(p.getCodigo_unico_participante() + " - " + p.getNome());
            check.setSelected(participanteEstaNaDespesa(p, despesa.getParticipantes()));
            editCheckboxes.add(check);
            editCheckboxesPanel.add(check);
        }

        JScrollPane scrollEditCheckboxes = new JScrollPane(editCheckboxesPanel);
        scrollEditCheckboxes.setPreferredSize(new Dimension(220, 100));

        JPanel painelEdicao = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        painelEdicao.add(new JLabel("Tipo:"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(new JLabel(tipoTexto), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelEdicao.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(editDescricao, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        painelEdicao.add(new JLabel("Valor (R$):"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(editValor, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.anchor = GridBagConstraints.NORTHWEST;
        painelEdicao.add(new JLabel("Participantes:"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(scrollEditCheckboxes, gbc);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                painelEdicao,
                "Editar Despesa #" + codigoDespesa,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String textoValor = editValor.getText().trim().replace(",", ".");
        try {
            if (textoValor.isEmpty()) {
                throw new IllegalArgumentException("Informe o valor da despesa");
            }
            BigDecimal novoValor = new BigDecimal(textoValor);

            List<Integer> codigosSelecionados = new ArrayList<>();
            for (int i = 0; i < editCheckboxes.size(); i++) {
                if (editCheckboxes.get(i).isSelected()) {
                    codigosSelecionados.add(todosParticipantes.get(i).getCodigo_unico_participante());
                }
            }

            controller.editarDespesa(
                    codigoPlanejamentoSelecionado,
                    codigoDespesa,
                    editDescricao.getText(),
                    novoValor,
                    codigosSelecionados
            );
            atualizarTabelaDespesas();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido. Digite um número (ex: 90,00).", "Erro de validação", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean participanteEstaNaDespesa(Participante participante, Participante[] participantesDaDespesa) {
        for (int i = 0; i < participantesDaDespesa.length; i++) {
            if (participantesDaDespesa[i].getCodigo_unico_participante() == participante.getCodigo_unico_participante()) {
                return true;
            }
        }
        return false;
    }

    private void removerDespesa() {
        if (codigoPlanejamentoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um planejamento primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int linha = tabelaDespesas.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma despesa na tabela para remover.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int codigoDespesa = (int) modeloDespesas.getValueAt(linha, 0);
        String descricao = (String) modeloDespesas.getValueAt(linha, 1);

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja remover a despesa \"" + descricao + "\"?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION
        );
        if (confirmacao == JOptionPane.YES_OPTION) {
            controller.removerDespesa(codigoPlanejamentoSelecionado, codigoDespesa);
            atualizarTabelaDespesas();
        }
    }

    public void atualizarTudo() {
        Integer codigoAnterior = codigoPlanejamentoSelecionado;
        atualizarParticipantesDisponiveis();
        atualizarTabelaPlanejamentos();
        if (codigoAnterior != null) {
            selecionarPlanejamentoPorCodigo(codigoAnterior);
        } else {
            atualizarTabelaDespesas();
        }
    }

    public void atualizarParticipantesDisponiveis() {
        checkboxesParticipantes.clear();
        participantesCarregados.clear();
        painelCheckboxes.removeAll();

        List<Participante> participantes = controller.listarParticipantes();
        for (int i = 0; i < participantes.size(); i++) {
            Participante p = participantes.get(i);
            JCheckBox check = new JCheckBox(p.getCodigo_unico_participante() + " - " + p.getNome());
            checkboxesParticipantes.add(check);
            participantesCarregados.add(p);
            painelCheckboxes.add(check);
        }

        painelCheckboxes.revalidate();
        painelCheckboxes.repaint();
    }

    private void atualizarTabelaPlanejamentos() {
        modeloPlanejamentos.setRowCount(0);
        List<Planejamento> planejamentos = controller.listarPlanejamentos();
        for (int i = 0; i < planejamentos.size(); i++) {
            Planejamento plan = planejamentos.get(i);
            modeloPlanejamentos.addRow(new Object[]{
                    plan.getCodigo_unico_planejamento(),
                    plan.getTitulo()
            });
        }
    }

    public void selecionarPlanejamentoPorCodigo(int codigo) {
        for (int i = 0; i < modeloPlanejamentos.getRowCount(); i++) {
            int codigoLinha = (int) modeloPlanejamentos.getValueAt(i, 0);
            if (codigoLinha == codigo) {
                tabelaPlanejamentos.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void atualizarTabelaDespesas() {
        modeloDespesas.setRowCount(0);
        if (codigoPlanejamentoSelecionado == null) {
            return;
        }

        Planejamento plan = controller.buscarPlanejamento(codigoPlanejamentoSelecionado);
        if (plan == null) {
            return;
        }

        List<Despesa> despesas = plan.getDespesas();
        for (int i = 0; i < despesas.size(); i++) {
            Despesa d = despesas.get(i);
            String tipoLegivel = "porPessoa".equals(d.getTipo()) ? "Por pessoa" : "Fixa";
            String nomesParticipantes = formatarNomesParticipantes(d.getParticipantes());

            modeloDespesas.addRow(new Object[]{
                    d.getCodigo_unico_despesa(),
                    d.getDescricao(),
                    tipoLegivel,
                    formatoMoeda.format(d.getValorInformado()),
                    formatoMoeda.format(d.getValor()),
                    nomesParticipantes
            });
        }
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
