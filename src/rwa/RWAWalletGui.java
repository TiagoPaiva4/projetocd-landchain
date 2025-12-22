package rwa;

import blockchain.Block;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

public class RWAWalletGui extends JFrame {

    private RWAService service;
    private JTextArea txtLog;
    private String usuario;

    public RWAWalletGui() {
        // Login simples
        usuario = JOptionPane.showInputDialog("Nome da Carteira (Login):", "Utilizador1");
        if (usuario == null || usuario.trim().isEmpty()) System.exit(0);

        try {
            service = new RWAService(usuario);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro crítico ao iniciar: " + e.getMessage());
            System.exit(1);
        }

        configurarJanela();
        
        // Verifica se o usuário já existe na blockchain
        if (!service.isRegistada()) {
            log("!!! ATENÇÃO !!!\nA carteira '" + usuario + "' não está registada na Blockchain.\nVocê deve usar a Opção 0 para criar a sua identidade antes de fazer qualquer outra coisa.");
        } else {
            log("Bem-vindo de volta, " + usuario + ". Sistema sincronizado.");
        }
    }

    private void configurarJanela() {
        setTitle("RWA Blockchain Wallet - Logado como: " + usuario);
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel de Log (Centro)
        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new java.awt.Font("Monospaced", 0, 14));
        add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // Painel de Botões (Menu Esquerdo)
        JPanel panelMenu = new JPanel(new GridLayout(0, 1, 5, 5)); // Uma coluna

        // BOTÃO DE REGISTO (Destacado)
        JButton btnRegistar = new JButton("0 - REGISTAR CARTEIRA (Obrigatório)");
        btnRegistar.setBackground(new Color(200, 255, 200)); // Verde claro
        btnRegistar.addActionListener(e -> acaoRegistarCarteira());
        panelMenu.add(btnRegistar);
        
        panelMenu.add(new JSeparator());

        adicionarBotao(panelMenu, "1 - Registar novo RWA", e -> acaoRegistarRWA());
        adicionarBotao(panelMenu, "2 - Listar RWAs", e -> acaoListarRWAs());
        adicionarBotao(panelMenu, "3 - Validar RWA", e -> acaoValidarRWA());
        adicionarBotao(panelMenu, "4 - Mostrar Blockchain", e -> acaoMostrarBlockchain());
        adicionarBotao(panelMenu, "5 - Registar Renda", e -> acaoRegistarRenda());
        adicionarBotao(panelMenu, "6 - Listar Rendas", e -> log("=== Histórico de Rendas ===\n" + String.join("\n", service.getHistoricoRendas())));
        adicionarBotao(panelMenu, "7 - Ver Meus Saldos", e -> log("=== Meus Saldos ===\n" + service.getMeusSaldos()));
        adicionarBotao(panelMenu, "8 - Transferir Tokens (Direto)", e -> acaoTransferir());
        
        // Separador Visual
        panelMenu.add(new JSeparator());
        panelMenu.add(new JLabel("--- MERCADO SECUNDÁRIO ---", SwingConstants.CENTER));

        adicionarBotao(panelMenu, "9 - [MERCADO] Criar Ordem Venda", e -> acaoCriarOrdem());
        adicionarBotao(panelMenu, "10 - [MERCADO] Comprar Tokens", e -> acaoComprarTokens());
        adicionarBotao(panelMenu, "11 - [MERCADO] Confirmar Venda", e -> acaoConfirmarVenda());

        // Adiciona o painel à esquerda dentro de um ScrollPane caso a tela seja pequena
        add(new JScrollPane(panelMenu), BorderLayout.WEST);
    }

    private void adicionarBotao(JPanel p, String label, java.awt.event.ActionListener l) {
        JButton btn = new JButton(label);
        btn.addActionListener(l);
        p.add(btn);
    }

    // ================= AÇÕES =================
    
    private void acaoRegistarCarteira() {
        if (service.isRegistada()) {
            log("ERRO: Esta carteira já está registada.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Deseja registar a identidade '" + usuario + "' na Blockchain?\nIsso irá minerar um bloco.");
        if (confirm == JOptionPane.YES_OPTION) {
            executarAsync(() -> {
                try {
                    service.registrarCarteira();
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void acaoRegistarRWA() {
        String id = JOptionPane.showInputDialog("ID único do Imóvel:");
        String nome = JOptionPane.showInputDialog("Nome/Descrição do Imóvel:");
        if (id != null && nome != null) {
            executarAsync(() -> {
                try {
                    service.registrarRWA(id, nome);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void acaoListarRWAs() {
        StringBuilder sb = new StringBuilder("=== LISTA DE IMÓVEIS ===\n");
        for (ImovelRWA r : service.getListaImoveis()) {
            sb.append(r.toString()).append("\n");
            sb.append("   -> Distribuição: ").append(r.distribuicaoTokens).append("\n");
        }
        log(sb.toString());
    }

    private void acaoValidarRWA() {
        String id = JOptionPane.showInputDialog("ID do Imóvel para Validar:");
        if (id != null) {
            executarAsync(() -> {
                try {
                    service.validarRWA(id);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void acaoMostrarBlockchain() {
        StringBuilder sb = new StringBuilder("=== BLOCKCHAIN DATA ===\n");
        for (Block b : service.getBlockchain().getBlocks()) {
            sb.append(b.toStringHeader()).append("\n");
            sb.append("DATA: ").append(b.getTransactions()).append("\n--------------------------------\n");
        }
        log(sb.toString());
    }

    private void acaoRegistarRenda() {
        String id = JOptionPane.showInputDialog("ID do Imóvel:");
        String valorStr = JOptionPane.showInputDialog("Valor Total da Renda (será distribuído):");
        try {
            double valor = Double.parseDouble(valorStr);
            executarAsync(() -> {
                try {
                    service.registrarRenda(id, valor);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception e) { log("Valor inválido."); }
    }

    private void acaoTransferir() {
        String id = JOptionPane.showInputDialog("ID do Imóvel:");
        String dest = JOptionPane.showInputDialog("Nome da Carteira de Destino:");
        String qtdStr = JOptionPane.showInputDialog("Quantidade de Tokens:");
        try {
            int qtd = Integer.parseInt(qtdStr);
            executarAsync(() -> {
                try {
                    service.transferirTokens(id, dest, qtd);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception e) { log("Dados inválidos."); }
    }

    // --- MERCADO UI ---

    private void acaoCriarOrdem() {
        String id = JOptionPane.showInputDialog("ID do Imóvel:");
        String qtdStr = JOptionPane.showInputDialog("Quantidade à venda:");
        String precoStr = JOptionPane.showInputDialog("Preço por Token:");
        try {
            int qtd = Integer.parseInt(qtdStr);
            double preco = Double.parseDouble(precoStr);
            executarAsync(() -> {
                try {
                    service.criarOrdemVenda(id, qtd, preco);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (Exception e) { log("Erro nos dados."); }
    }

    private void acaoComprarTokens() {
        listarMercado();
        String idOrdem = JOptionPane.showInputDialog("Digite o ID da Ordem para Comprar:");
        if(idOrdem != null) {
            executarAsync(() -> {
                try {
                    service.comprarTokens(idOrdem);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void acaoConfirmarVenda() {
        listarMercado();
        String idOrdem = JOptionPane.showInputDialog("VENDEDOR: Digite o ID da Ordem para confirmar:");
        if(idOrdem != null) {
            executarAsync(() -> {
                try {
                    service.confirmarVenda(idOrdem);
                } catch (Exception ex) {
                    Logger.getLogger(RWAWalletGui.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
    }

    private void listarMercado() {
        StringBuilder sb = new StringBuilder("=== MERCADO ATUAL ===\n");
        for(OrdemVenda ov : service.getMercado()) {
            sb.append(ov).append("\n");
            if(ov.compradorInteressado != null) {
                sb.append("   *** Comprador Aguardando Confirmação: ").append(ov.compradorInteressado).append("\n");
            }
        }
        log(sb.toString());
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            txtLog.setText(msg + "\n\n" + txtLog.getText()); // Adiciona no topo
            txtLog.setCaretPosition(0);
        });
    }

    // Executa operações de blockchain numa thread separada para não travar a GUI
    private void executarAsync(Runnable r) {
        new Thread(() -> {
            try {
                r.run();
                log("SUCESSO: Bloco Minerado e Transação confirmada.");
            } catch (Exception e) {
                log("ERRO NA OPERAÇÃO: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RWAWalletGui().setVisible(true));
    }
}