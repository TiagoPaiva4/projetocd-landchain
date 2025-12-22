package rwa;

import blockchain.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.rmi.RemoteException;
import javax.swing.*;
import utils.RMI;

public class RWAWalletGui extends JFrame implements Nodelistener { // Nodelistener do TEU pacote blockchain

    private RemoteNodeObject myNode; // O nó original
    private RWAService rwaService;   // O nosso serviço RWA (local)
    
    private JTextArea txtLog;
    private String usuario;
    
    // GUI
    private JTextField txtPorta, txtVizinho;
    private JButton btnStart, btnConnect;

    public RWAWalletGui() {
        usuario = JOptionPane.showInputDialog("Nome da Carteira:", "User1");
        if (usuario == null || usuario.trim().isEmpty()) System.exit(0);
        
        // Inicializa o serviço RWA localmente
        rwaService = new RWAService(usuario);
        
        configurarJanela();
    }

    private void configurarJanela() {
        setTitle("RWA P2P Wallet - " + usuario);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- PAINEL REDE ---
        JPanel pnlRede = new JPanel(new GridLayout(1, 4, 5, 5));
        txtPorta = new JTextField("10010"); txtPorta.setBorder(BorderFactory.createTitledBorder("Porta Local"));
        btnStart = new JButton("1. Iniciar Nó");
        btnStart.addActionListener(e -> iniciarServidor());
        
        txtVizinho = new JTextField("127.0.0.1:10010"); txtVizinho.setBorder(BorderFactory.createTitledBorder("Vizinho"));
        btnConnect = new JButton("2. Conectar");
        btnConnect.addActionListener(e -> conectarRede());
        
        pnlRede.add(txtPorta); pnlRede.add(btnStart);
        pnlRede.add(txtVizinho); pnlRede.add(btnConnect);
        add(pnlRede, BorderLayout.NORTH);

        // --- LOG ---
        txtLog = new JTextArea(); txtLog.setEditable(false);
        add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // --- MENU RWA ---
        JPanel pnlMenu = new JPanel(new GridLayout(0, 1, 5, 5));
        
        JButton btnReg = new JButton("0 - REGISTAR ID");
        btnReg.setBackground(new Color(200, 255, 200));
        btnReg.addActionListener(e -> enviarTransacao(TransacaoRWA.Tipo.REGISTAR_CARTEIRA, null));
        pnlMenu.add(btnReg);
        
        adicionarBotao(pnlMenu, "1 - Novo Imóvel", e -> acaoNovoRWA());
        adicionarBotao(pnlMenu, "2 - Listar Imóveis", e -> listarRWA());
        adicionarBotao(pnlMenu, "3 - Transferir", e -> acaoTransferir());
        adicionarBotao(pnlMenu, "4 - Ver Saldos", e -> verSaldos());
        adicionarBotao(pnlMenu, "5 - Mercado (Listar)", e -> listarMercado());
        
        pnlMenu.add(new JSeparator());
        
        JButton btnMine = new JButton("⛏️ MINERAR");
        btnMine.setBackground(Color.ORANGE);
        btnMine.addActionListener(e -> acaoMinerar());
        pnlMenu.add(btnMine);
        
        add(new JScrollPane(pnlMenu), BorderLayout.WEST);
    }
    
    // ================= REDE =================
    
    private void iniciarServidor() {
        try {
            int port = Integer.parseInt(txtPorta.getText());
            
            // CORREÇÃO: Usar o construtor original (int, Nodelistener)
            // 'this' refere-se à GUI, que implementa Nodelistener
            myNode = new RemoteNodeObject(port, this); 
            
            // Inicia RMI (código padrão do teu projeto)
            RMI.startRemoteObject(myNode, port, "remoteNode");
            
            // Tenta carregar estado inicial se existir blockchain no disco
            if(myNode.getBlockchain() != null) {
                rwaService.atualizarEstado(myNode.getBlockchain());
            }
            
            btnStart.setEnabled(false);
            log("Nó P2P iniciado na porta " + port);
        } catch (Exception e) { 
            log("Erro Server: " + e.getMessage()); 
            e.printStackTrace();
        }
    }
    
    private void conectarRede() {
        if (myNode == null) { log("Inicie o servidor primeiro!"); return; }
        try {
            String url = "//" + txtVizinho.getText() + "/remoteNode";
            RemoteNodeInterface node = (RemoteNodeInterface) RMI.getRemote(url);
            myNode.addNode(node);
            log("Conectado a " + url);
        } catch (Exception e) { log("Erro Connect: " + e.getMessage()); }
    }

    // ================= RWA (Via String Base64) =================
    
    private void enviarTransacao(TransacaoRWA.Tipo tipo, Object dados) {
        if (myNode == null) { log("ERRO: Inicie o servidor."); return; }
        try {
            TransacaoRWA tx = new TransacaoRWA(usuario, tipo);
            
            if (tipo == TransacaoRWA.Tipo.REGISTAR_CARTEIRA) tx.dadosCarteira = new Carteira(usuario);
            if (dados instanceof ImovelRWA) tx.dadosImovel = (ImovelRWA) dados;
            // Adaptação para outros tipos...
            
            // Converter para String e enviar
            String txBase64 = UtilsRWA.transacaoParaTexto(tx);
            if (txBase64 != null) {
                myNode.addTransaction(txBase64);
                log("Transação enviada para Pool.");
            }
        } catch (Exception e) { log("Erro TX: " + e.getMessage()); }
    }
    
    // --- Wrappers ---
    private void acaoNovoRWA() {
        String id = JOptionPane.showInputDialog("ID:");
        String nome = JOptionPane.showInputDialog("Nome:");
        if (id != null) enviarTransacao(TransacaoRWA.Tipo.REGISTAR_RWA, new ImovelRWA(id, nome, usuario));
    }
    
    private void acaoTransferir() {
        // Exemplo simplificado
        log("Use a lógica de criar TransacaoRWA manualmente aqui.");
    }

    private void acaoMinerar() {
        if (myNode == null) return;
        try {
            myNode.startMiner("RWA-Block", 3);
        } catch (RemoteException ex) { log("Erro: " + ex.getMessage()); }
    }

    // --- Leituras (Usam o rwaService local que é atualizado pelo listener) ---
    private void listarRWA() {
        log("=== IMÓVEIS ===");
        for(ImovelRWA r : rwaService.getListaImoveis()) log(r.toString());
    }
    
    private void verSaldos() {
        log(rwaService.getMeusSaldos());
    }
    
    private void listarMercado() {
        log("=== MERCADO ===");
        for(OrdemVenda ov : rwaService.getMercado()) log(ov.toString());
    }

    // ================= LISTENERS (Do RemoteNodeObject) =================
    
    @Override
    public void onBlockchain(BlockChain b) {
        // ESTA É A CHAVE: Quando o nó recebe blocos, avisamos o serviço RWA
        SwingUtilities.invokeLater(() -> {
            rwaService.atualizarEstado(b);
            
            // CORREÇÃO: Usar getBlocks().size() em vez de getSize()
            log("Blockchain atualizada! Altura: " + b.getBlocks().size());
            log("Estado RWA sincronizado.");
        });
    }

    @Override public void onStart(String msg) { log(msg); }
    @Override public void onConect(String end) { log("Conectado: " + end); }
    @Override public void onException(Exception e, String title) { log("Erro " + title + ": " + e.getMessage()); }
    @Override public void onTransaction(String t) { /* log("Nova TX..."); */ }
    
    // Helpers
    private void log(String m) { txtLog.append(m + "\n"); txtLog.setCaretPosition(txtLog.getDocument().getLength()); }
    private void adicionarBotao(JPanel p, String l, java.awt.event.ActionListener a) { JButton b = new JButton(l); b.addActionListener(a); p.add(b); }
    
    public static void main(String[] args) { 
        SwingUtilities.invokeLater(() -> new RWAWalletGui().setVisible(true)); 
    }
}