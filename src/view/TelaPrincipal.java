package view;

import controller.PlanejadorController;
import model.Planejamento;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;

public class TelaPrincipal extends JFrame {
    private final PlanejadorController controller;
    private final JTabbedPane abas;
    private PainelParticipantes abaParticipantes;
    private PainelPlanejamentos abaPlanejamentos;
    private PainelResumo abaResumo;

    public TelaPrincipal(PlanejadorController controller) {
        this.controller = controller;
        this.abas = new JTabbedPane();

        configurarJanela();
        configurarMenu();
        configurarAbas();
    }

    private void configurarJanela() {
        setTitle("Cabe no Bolso - Planejador de Despesas Compartilhadas");
        setSize(950, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void configurarMenu() {
        JMenuBar barraMenu = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");

        JMenuItem itemNovoParticipante = new JMenuItem("Novo participante...");
        JMenuItem itemNovoPlanejamento = new JMenuItem("Novo planejamento vazio...");
        JMenuItem itemNovoArquivo = new JMenuItem("Novo arquivo de dados...");
        JMenuItem itemCarregar = new JMenuItem("Carregar arquivo...");
        JMenuItem itemSalvarComo = new JMenuItem("Salvar arquivo como...");

        itemNovoParticipante.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoParticipantePeloMenu();
            }
        });

        itemNovoPlanejamento.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoPlanejamentoPeloMenu();
            }
        });

        itemNovoArquivo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                criarNovoArquivoPeloMenu();
            }
        });

        itemCarregar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                escolherECarregarArquivo();
            }
        });

        itemSalvarComo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                escolherESalvarArquivo();
            }
        });

        menuArquivo.add(itemNovoParticipante);
        menuArquivo.add(itemNovoPlanejamento);
        menuArquivo.add(itemNovoArquivo);
        menuArquivo.addSeparator();
        menuArquivo.add(itemCarregar);
        menuArquivo.add(itemSalvarComo);
        barraMenu.add(menuArquivo);
        setJMenuBar(barraMenu);
    }

    private void configurarAbas() {
        abaParticipantes = new PainelParticipantes(controller);
        abaPlanejamentos = new PainelPlanejamentos(controller);
        abaResumo = new PainelResumo(controller);

        abas.addTab("Participantes", abaParticipantes);
        abas.addTab("Planejamentos e Despesas", abaPlanejamentos);
        abas.addTab("Resumo", abaResumo);

        abas.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int indice = abas.getSelectedIndex();
                if (indice == 0) {
                    abaParticipantes.atualizarTabela();
                } else if (indice == 1) {
                    abaPlanejamentos.atualizarTudo();
                } else if (indice == 2) {
                    abaResumo.atualizarTudo();
                }
            }
        });

        add(abas, BorderLayout.CENTER);
    }

    public void carregarDadosIniciais() {
        try {
            controller.carregarArquivoPadrao();
            atualizarTodasAsAbas();
        } catch (FileNotFoundException ex) {
            tratarFalhaInicial("Arquivo de dados não encontrado (" + controller.getCaminhoArquivoAtual() + ").");
        } catch (IOException ex) {
            tratarFalhaInicial("Não foi possível ler o arquivo de dados (" + ex.getMessage() + ").");
        }
    }

    private void tratarFalhaInicial(String motivo) {
        Object[] opcoes = {"Iniciar vazio", "Carregar outro arquivo..."};
        int escolha = JOptionPane.showOptionDialog(
                this,
                motivo + "\nDeseja iniciar um planejamento vazio ou selecionar outro arquivo?",
                "Problema no arquivo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                opcoes,
                opcoes[0]
        );

        if (escolha == 1) {
            escolherECarregarArquivo();
        } else {
            controller.iniciarPlanejamentoVazio();
            atualizarTodasAsAbas();
        }
    }

    private void criarNovoParticipantePeloMenu() {
        JTextField campoNome = new JTextField(18);
        JTextField campoOrcamento = new JTextField(10);

        JPanel painel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        painel.add(new JLabel("Nome:"), gbc);
        gbc.gridx = 1;
        painel.add(campoNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        painel.add(new JLabel("Orçamento (R$):"), gbc);
        gbc.gridx = 1;
        painel.add(campoOrcamento, gbc);

        int resultado = JOptionPane.showConfirmDialog(
                this,
                painel,
                "Novo Participante",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String textoOrcamento = campoOrcamento.getText().trim().replace(",", ".");
        try {
            if (textoOrcamento.isEmpty()) {
                throw new IllegalArgumentException("Informe o orçamento do participante");
            }
            BigDecimal orcamento = new BigDecimal(textoOrcamento);
            controller.cadastrarParticipante(campoNome.getText(), orcamento);
            atualizarTodasAsAbas();
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

    private void criarNovoPlanejamentoPeloMenu() {
        String titulo = JOptionPane.showInputDialog(
                this,
                "Título do novo planejamento:",
                "Novo planejamento vazio",
                JOptionPane.PLAIN_MESSAGE
        );

        if (titulo == null) {
            return;
        }

        try {
            Planejamento novo = controller.criarPlanejamento(titulo);
            atualizarTodasAsAbas();
            abas.setSelectedIndex(1);
            abaPlanejamentos.selecionarPlanejamentoPorCodigo(novo.getCodigo_unico_planejamento());
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de validação", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void criarNovoArquivoPeloMenu() {
        JFileChooser seletor = new JFileChooser(new File("."));
        seletor.setDialogTitle("Criar novo arquivo de dados vazio");
        seletor.setSelectedFile(new File("novo_planejamento.dat"));

        int resultado = seletor.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File novoArquivo = seletor.getSelectedFile();
        if (novoArquivo.exists()) {
            int confirmacao = JOptionPane.showConfirmDialog(
                    this,
                    "O arquivo \"" + novoArquivo.getName() + "\" já existe. Deseja substituí-lo?",
                    "Confirmar substituição",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmacao != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            controller.iniciarNovoArquivoVazio(novoArquivo);
            atualizarTodasAsAbas();
            JOptionPane.showMessageDialog(
                    this,
                    "Novo arquivo criado: " + novoArquivo.getName(),
                    "Arquivo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao criar novo arquivo: " + ex.getMessage() + "\nOs dados em uso foram preservados.",
                    "Problema no arquivo",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void escolherECarregarArquivo() {
        JFileChooser seletor = new JFileChooser(new File("."));
        seletor.setDialogTitle("Carregar arquivo de dados");

        int resultado = seletor.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivoSelecionado = seletor.getSelectedFile();
        try {
            controller.carregarArquivo(arquivoSelecionado);
            atualizarTodasAsAbas();
            JOptionPane.showMessageDialog(
                    this,
                    "Dados carregados de: " + arquivoSelecionado.getName(),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() + "\nO planejamento em uso foi preservado.",
                    "Arquivo ausente",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage() + "\nO planejamento em uso foi preservado.",
                    "Arquivo inválido",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void escolherESalvarArquivo() {
        JFileChooser seletor = new JFileChooser(new File("."));
        seletor.setDialogTitle("Salvar arquivo de dados");
        seletor.setSelectedFile(new File(controller.getCaminhoArquivoAtual()));

        int resultado = seletor.showSaveDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File arquivoDestino = seletor.getSelectedFile();
        try {
            controller.salvarArquivoComo(arquivoDestino);
            JOptionPane.showMessageDialog(
                    this,
                    "Dados salvos em: " + arquivoDestino.getName(),
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Erro ao salvar arquivo: " + ex.getMessage(),
                    "Problema no arquivo",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void atualizarTodasAsAbas() {
        abaParticipantes.atualizarTabela();
        abaPlanejamentos.atualizarTudo();
        abaResumo.atualizarTudo();
    }
}
