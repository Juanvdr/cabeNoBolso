package view;

import controller.PlanejadorController;
import model.Participante;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PainelParticipantes extends JPanel {
    private final PlanejadorController controller;
    private final JTextField campoNome;
    private final JTextField campoOrcamento;
    private final DefaultTableModel modeloTabela;
    private final JTable tabela;
    private final NumberFormat formatoMoeda;

    public PainelParticipantes(PlanejadorController controller) {
        this.controller = controller;
        this.formatoMoeda = NumberFormat.getCurrencyInstance(Locale.of("pt", "BR"));

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Formulário no topo
        JPanel painelFormulario = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Novo Participante"));

        campoNome = new JTextField(18);
        campoOrcamento = new JTextField(10);
        JButton botaoCadastrar = new JButton("Cadastrar");

        painelFormulario.add(new JLabel("Nome:"));
        painelFormulario.add(campoNome);
        painelFormulario.add(new JLabel("Orçamento (R$):"));
        painelFormulario.add(campoOrcamento);
        painelFormulario.add(botaoCadastrar);

        // Tabela no centro
        String[] colunas = {"Código", "Nome", "Orçamento"};
        modeloTabela = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollTabela = new JScrollPane(tabela);

        // Botões embaixo
        JPanel painelAcoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton botaoEditar = new JButton("Editar participante selecionado");
        JButton botaoRemover = new JButton("Remover participante selecionado");
        painelAcoes.add(botaoEditar);
        painelAcoes.add(botaoRemover);

        add(painelFormulario, BorderLayout.NORTH);
        add(scrollTabela, BorderLayout.CENTER);
        add(painelAcoes, BorderLayout.SOUTH);

        // Eventos dos botões
        botaoCadastrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cadastrar();
            }
        });

        botaoEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editarSelecionado();
            }
        });

        botaoRemover.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removerSelecionado();
            }
        });

        atualizarTabela();
    }

    private void cadastrar() {
        String nome = campoNome.getText();
        String textoOrcamento = campoOrcamento.getText().trim().replace(",", ".");

        try {
            if (textoOrcamento.isEmpty()) {
                throw new IllegalArgumentException("Informe o orçamento do participante");
            }
            BigDecimal orcamento = new BigDecimal(textoOrcamento);
            controller.cadastrarParticipante(nome, orcamento);

            campoNome.setText("");
            campoOrcamento.setText("");
            campoNome.requestFocus();
            atualizarTabela();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Orçamento inválido. Digite um número (ex: 150,00).",
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void editarSelecionado() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um participante na tabela para editar.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int codigo = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
        Participante participante = controller.buscarParticipante(codigo);
        if (participante == null) {
            return;
        }

        JTextField editNome = new JTextField(participante.getNome(), 18);
        JTextField editOrcamento = new JTextField(
                participante.getOrcamento().toPlainString().replace(".", ","),
                10
        );

        JPanel painelEdicao = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        painelEdicao.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(editNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painelEdicao.add(new JLabel("Orçamento (R$):"), gbc);
        gbc.gridx = 1;
        painelEdicao.add(editOrcamento, gbc);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                painelEdicao,
                "Editar Participante #" + codigo,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String textoOrcamento = editOrcamento.getText().trim().replace(",", ".");
        try {
            if (textoOrcamento.isEmpty()) {
                throw new IllegalArgumentException("Informe o orçamento do participante");
            }
            BigDecimal novoOrcamento = new BigDecimal(textoOrcamento);
            controller.editarParticipante(codigo, editNome.getText(), novoOrcamento);
            atualizarTabela();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Orçamento inválido. Digite um número (ex: 150,00).",
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Erro de validação",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void removerSelecionado() {
        int linhaSelecionada = tabela.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione um participante na tabela para remover.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int codigo = (int) modeloTabela.getValueAt(linhaSelecionada, 0);
        String nome = (String) modeloTabela.getValueAt(linhaSelecionada, 1);

        int confirmacao = JOptionPane.showConfirmDialog(
                this,
                "Deseja remover o participante \"" + nome + "\"?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION
        );

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                controller.removerParticipante(codigo);
                atualizarTabela();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Remoção bloqueada",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }
    }

    public void atualizarTabela() {
        modeloTabela.setRowCount(0);
        List<Participante> participantes = controller.listarParticipantes();
        for (int i = 0; i < participantes.size(); i++) {
            Participante p = participantes.get(i);
            Object[] linha = {
                    p.getCodigo_unico_participante(),
                    p.getNome(),
                    formatoMoeda.format(p.getOrcamento())
            };
            modeloTabela.addRow(linha);
        }
    }
}
