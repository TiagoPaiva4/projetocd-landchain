package rwa;

import blockchain.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.rmi.RemoteException;
import javax.swing.*;
import utils.RMI;
import utils.Utils;

public class RWAWalletGui extends JFrame implements Nodelistener, MinerListener {

    private RemoteNodeObject myNode; // O nosso Nó P2P
    private JTextArea txtLog;
    private String usuario;
    
    // Componentes de Rede
    private JTextField txtPorta, txtVizinho;
    private JButton btnStart, btnConnect;

    public RWAWalletGui() {
        // 1. Login
        usuario = JOptionPane.showInputDialog("Nome da Carteira:", "User1");
        if (usuario == null || usuario.trim().isEmpty()) System.exit(0);
        
        configurarJanela();
    }

    private void configurarJanela() {
        setTitle("RWA P2P Wallet - " + usuario);
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- PAINEL TOPO: REDE ---
        JPanel pnlRede = new JPanel(new GridLayout(1, 4, 5, 5));
        pnlRede.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        txtPorta = new JTextField("10010"); 
        txtPorta.setBorder(BorderFactory.createTitledBorder("Minha Porta"));
        
        btnStart = new JButton("1. Iniciar Nó");
        btnStart.addActionListener(e -> iniciarServidor());
        
        txtVizinho = new JTextField("127.0.0.1:10010"); 
        txtVizinho.setBorder(BorderFactory.createTitledBorder("Vizinho (IP:Porta)"));
        
        btnConnect = new JButton("2. Conectar");
        btnConnect.addActionListener(e -> conectarRede());
        
        pnlRede.add(txtPorta); pnlRede.add(btnStart);
        pnlRede.add(txtVizinho); pnlRede.add(btnConnect);
        add(pnlRede, BorderLayout.NORTH);

        // --- PAINEL CENTRO: LOG ---
        txtLog = new JTextArea(); 
        txtLog.setEditable(false);
        txtLog.setFont(new java.awt.Font("Monospaced", 0, 12));
        add(new JScrollPane(txtLog), BorderLayout.CENTER);

        // --- PAINEL ESQUERDA: MENU RWA ---
        JPanel pnlMenu = new JPanel(new GridLayout(0, 1, 5, 5));
        pnlMenu.setBorder(BorderFactory.createTitledBorder("Menu RWA"));
        
        // Registo
        JButton btnReg = new JButton("0 - REGISTAR ID");
        btnReg.setBackground(new Color(200, 255, 200));
        btnReg.addActionListener(e -> enviarTransacao(TransacaoRWA.Tipo.REGISTAR_CARTEIRA, null));
        pnlMenu.add(btnReg);
        
        adicionarBotao(pnlMenu, "1 - Novo Imóvel", e -> acaoNovoRWA());
        adicionarBotao(pnlMenu, "2 - Listar Imóveis", e -> listarRWA());
        adicionarBotao(pnlMenu, "3 - Validar Imóvel", e -> acaoValidar());
        adicionarBotao(pnlMenu, "4 - Transferir Tokens", e -> acaoTransferir());
        adicionarBotao(pnlMenu, "5 - Meus Saldos", e -> verSaldos());
        adicionarBotao(pnlMenu, "6 - Registar Renda", e -> acaoRenda());
        adicionarBotao(pnlMenu, "7 - Ver Blockchain", e -> verChain());
        
        pnlMenu.add(new JSeparator());
        
        // Mercado
        adicionarBotao(pnlMenu, "8 - [MKT] Vender", e -> acaoVender());
        adicionarBotao(pnlMenu, "9 - [MKT] Comprar", e -> acaoComprar());
        adicionarBotao(pnlMenu, "10 - [MKT] Confirmar", e -> acaoConfirmarVenda());
        
        pnlMenu.add(new JSeparator());
        
        // Mineração
        JButton btnMine = new JButton("⛏️ MINERAR BLOCO");
        btnMine.setBackground(Color.ORANGE);
        btnMine.addActionListener(e -> acaoMinerar());
        pnlMenu.add(btnMine);
        
        add(new JScrollPane(pnlMenu), BorderLayout.WEST);
    }
    
    // =======================================================
    // LÓGICA DE REDE (P2P)
    // =======================================================
    
    private void iniciarServidor() {
        try {
            int port = Integer.parseInt(txtPorta.getText());
            // Cria o nó e passa 'this' como listener para receber eventos
            // NOTA: O RemoteNodeObject deve ter o construtor adaptado para aceitar (int port, String wallet, Listener)
            // Se não tiver, usa o construtor padrão e define o RWAService manualmente depois.
            myNode = new RemoteNodeObject(port, usuario, this); 
            
            // Inicia RMI
            RMI.startRemoteObject(myNode, port, "remoteNode");
            
            btnStart.setEnabled(false);
            log(">>> Servidor P2P iniciado na porta " + port);
            log(">>> Estado RWA carregado.");
        } catch (Exception e) { 
            log("ERRO AO INICIAR: " + e.getMessage()); 
            e.printStackTrace();
        }
    }
    
    private void conectarRede() {
        if (myNode == null) { log("ERRO: Inicie o servidor local primeiro!"); return; }
        try {
            String url = "//" + txtVizinho.getText() + "/remoteNode";
            RemoteNodeInterface node = (RemoteNodeInterface) RMI.getRemote(url);
            myNode.addNode(node);
            log(">>> Conexão solicitada a " + url);
        } catch (Exception e) { 
            log("ERRO AO CONECTAR: " + e.getMessage()); 
        }
    }

    // =======================================================
    // AÇÕES RWA (Cria TX -> Base64 -> Envia para Rede)
    // =======================================================
    
    private void enviarTransacao(TransacaoRWA.Tipo tipo, Object dados) {
        if (myNode == null) { log("ERRO: Inicie o servidor P2P."); return; }
        
        try {
            // 1. Criar Transação
            TransacaoRWA tx = new TransacaoRWA(usuario, tipo);
            
            // 2. Preencher Dados
            if (tipo == TransacaoRWA.Tipo.REGISTAR_CARTEIRA) tx.dadosCarteira = new Carteira(usuario);
            if (dados instanceof ImovelRWA) tx.dadosImovel = (ImovelRWA) dados;
            if (dados instanceof OrdemVenda) tx.dadosOrdem = (OrdemVenda) dados;
            
            // Para casos específicos que usam campos soltos (transferência, validação, etc)
            if (tipo == TransacaoRWA.Tipo.VALIDAR_RWA) tx.idRwaAlvo = (String) dados;
            
            // 3. Serializar para Texto (Base64)
            String txBase64 = UtilsRWA.transacaoParaTexto(tx);
            
            if (txBase64 != null) {
                // 4. Enviar para a Mempool P2P
                myNode.addTransaction(txBase64);
                log(">>> Transação enviada para a rede (Aguardando mineração...)");
            } else {
                log("ERRO: Falha na serialização.");
            }
            
        } catch (Exception e) { log("Erro TX: " + e.getMessage()); }
    }
    
    // --- Wrappers para as ações ---
    
    private void acaoNovoRWA() {
        String id = JOptionPane.showInputDialog("ID Imóvel:");
        String nome = JOptionPane.showInputDialog("Nome:");
        if (id != null) enviarTransacao(TransacaoRWA.Tipo.REGISTAR_RWA, new ImovelRWA(id, nome, usuario));
    }
    
    private void acaoValidar() {
        String id = JOptionPane.showInputDialog("ID do Imóvel a Validar:");
        if (id != null) enviarTransacao(TransacaoRWA.Tipo.VALIDAR_RWA, id);
    }
    
    private void acaoTransferir() {
        // Como o método genérico 'enviarTransacao' simplificado acima não cobre todos os campos,
        // fazemos a construção manual aqui para transferências:
        if (myNode == null) return;
        try {
            String id = JOptionPane.showInputDialog("ID Imóvel:");
            String dest = JOptionPane.showInputDialog("Destinatário:");
            int qtd = Integer.parseInt(JOptionPane.showInputDialog("Quantidade:"));
            
            TransacaoRWA tx = new TransacaoRWA(usuario, TransacaoRWA.Tipo.TRANSFERIR_TOKENS);
            tx.idRwaAlvo = id;
            tx.destinatario = dest;
            tx.quantidade = qtd;
            
            myNode.addTransaction(UtilsRWA.transacaoParaTexto(tx));
            log(">>> Pedido de transferência enviado.");
        } catch(Exception e) { log("Erro inputs: " + e.getMessage()); }
    }
    
    private void acaoRenda() {
        if (myNode == null) return;
        try {
            String id = JOptionPane.showInputDialog("ID Imóvel:");
            double valor = Double.parseDouble(JOptionPane.showInputDialog("Valor Renda:"));
            
            TransacaoRWA tx = new TransacaoRWA(usuario, TransacaoRWA.Tipo.REGISTAR_RENDA);
            tx.idRwaAlvo = id;
            tx.valorFinanceiro = valor;
            
            myNode.addTransaction(UtilsRWA.transacaoParaTexto(tx));
            log(">>> Registo de renda enviado.");
        } catch(Exception e) { log("Erro inputs: " + e.getMessage()); }
    }
    
    private void acaoVender() {
        // Criar Ordem
        try {
            String idRwa = JOptionPane.showInputDialog("ID Imóvel:");
            int qtd = Integer.parseInt(JOptionPane.showInputDialog("Qtd Tokens:"));
            double preco = Double.parseDouble(JOptionPane.showInputDialog("Preço Total:"));
            
            OrdemVenda ordem = new OrdemVenda("ORD-" + System.currentTimeMillis(), usuario, idRwa, qtd, preco);
            enviarTransacao(TransacaoRWA.Tipo.MERCADO_CRIAR_ORDEM, ordem);
        } catch(Exception e) { log("Erro: " + e.getMessage()); }
    }
    
    private void acaoComprar() {
        listarMercado();
        String idOrdem = JOptionPane.showInputDialog("ID da Ordem para Comprar:");
        if (idOrdem == null) return;
        
        try {
            TransacaoRWA tx = new TransacaoRWA(usuario, TransacaoRWA.Tipo.MERCADO_COMPRAR);
            // Criamos um objeto dummy apenas com o ID para identificar
            OrdemVenda dummy = new OrdemVenda(idOrdem, "", "", 0, 0);
            tx.dadosOrdem = dummy; 
            
            myNode.addTransaction(UtilsRWA.transacaoParaTexto(tx));
            log(">>> Intenção de compra enviada.");
        } catch(Exception e) { log("Erro: " + e.getMessage()); }
    }
    
    private void acaoConfirmarVenda() {
        String idOrdem = JOptionPane.showInputDialog("ID da Ordem para Confirmar (Vendedor):");
        if (idOrdem == null) return;
         try {
            TransacaoRWA tx = new TransacaoRWA(usuario, TransacaoRWA.Tipo.MERCADO_CONFIRMAR);
            OrdemVenda dummy = new OrdemVenda(idOrdem, "", "", 0, 0);
            tx.dadosOrdem = dummy; 
            
            myNode.addTransaction(UtilsRWA.transacaoParaTexto(tx));
            log(">>> Confirmação enviada.");
        } catch(Exception e) { log("Erro: " + e.getMessage()); }
    }

    // --- LEITURAS DE ESTADO (Local) ---
    
    private void listarRWA() {
        if (myNode == null) return;
        log("=== IMÓVEIS (Estado Local) ===");
        for (ImovelRWA r : myNode.rwaService.getListaImoveis()) {
            log(r.toString() + " | Dist: " + r.distribuicaoTokens);
        }
    }
    
    private void verSaldos() {
        if (myNode == null) return;
        log("=== MEUS SALDOS ===");
        log(myNode.rwaService.getMeusSaldos());
    }
    
    private void listarMercado() {
        if (myNode == null) return;
        log("=== MERCADO ===");
        for (OrdemVenda ov : myNode.rwaService.getMercado()) {
            log(ov.toString());
        }
    }
    
    private void verChain() {
        if (myNode == null) return;
        try {
            log("=== BLOCKCHAIN ===");
            log("Altura: " + myNode.getBlockchainSize());
            Block last = myNode.getlastBlock();
            if(last != null) log("Último Hash: " + last.getCurrentHash());
        } catch (RemoteException ex) { }
    }
    
    private void acaoMinerar() {
        if (myNode == null) return;
        try {
            log("Iniciando mineração distribuída (Dificuldade 3)...");
            myNode.startMiner("RWA-Block", 3);
        } catch (RemoteException ex) { log("Erro Minerar: " + ex.getMessage()); }
    }

    // =======================================================
    // LISTENERS (Eventos vindos do Nó)
    // =======================================================

    @Override public void onStart(String msg) { log("[NÓ] " + msg); }
    @Override public void onConect(String end) { log("[REDE] Conectado a " + end); }
    @Override public void onException(Exception e, String title) { log("[ERRO] " + title + ": " + e.getMessage()); }
    
    @Override 
    public void onTransaction(String transaction) { 
        // Apenas notificação visual
        // log("Nova Transação na Pool...");
    }
    
    @Override 
    public void onBlockchain(BlockChain b) { 
        SwingUtilities.invokeLater(() -> {
            log(">>> BLOCKCHAIN ATUALIZADA! Nova altura: " + b.getSize());
            log(">>> Estado RWA recalculado.");
        }); 
    }

    // Eventos do Minerador
    @Override public void onStartMining(String msg, int dif) { log(">>> A MINERAR... (Dif: " + dif + ")"); }
    @Override public void onStopMining(int nonce) { log(">>> Mineração parada."); }
    @Override 
    public void onNonceFound(int nonce) { 
        SwingUtilities.invokeLater(() -> {
            log("🏆 SUCESSO! Nonce encontrado: " + nonce);
            log("O bloco será propagado e o estado atualizado.");
            
            // Importante: No RemoteNodeObject, onNonceFound já trata de parar e adicionar o bloco.
            // Aqui apenas mostramos a festa.
        });
    }

    // Auxiliares
    private void log(String m) { 
        txtLog.append(m + "\n"); 
        txtLog.setCaretPosition(txtLog.getDocument().getLength()); 
    }
    
    private void adicionarBotao(JPanel p, String l, java.awt.event.ActionListener a) { 
        JButton b = new JButton(l); 
        b.addActionListener(a); 
        p.add(b); 
    }
    
    public static void main(String[] args) { 
        SwingUtilities.invokeLater(() -> new RWAWalletGui().setVisible(true)); 
    }
}