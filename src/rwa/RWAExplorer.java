package rwa;

import core.Block;
import core.BlockChain;
import core.Transaction;
import events.RentDistributionEvent;
import network.Message;
import network.P2PNode;
import token.TokenRegistry;
import token.Wallet;
import escrow.EscrowManager;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class RWAExplorer {

    private final BlockChain blockchain;
    private final Oracle oracle;
    private final RWAValidator validator;
    private final Scanner sc;

    // Dependências externas
    private P2PNode node;
    private TokenRegistry registry;
    private EscrowManager escrowManager; // Gestor do Mercado

    // Carteira do utilizador atual
    private Wallet myWallet;

    public RWAExplorer(BlockChain bc, Oracle oracle, RWAValidator validator) {
        this.blockchain = bc;
        this.oracle = oracle;
        this.validator = validator;
        this.sc = new Scanner(System.in);
        
        // Inicializar o gestor de Escrow
        this.escrowManager = new EscrowManager(bc);

        // Iniciar sistema de Login
        loginMenu();
    }

    // ================================
    // SISTEMA DE LOGIN / CARTEIRA
    // ================================
    private void loginMenu() {
        System.out.println("\n==================================");
        System.out.println("      BEM-VINDO A LANDCHAIN       ");
        System.out.println("==================================");
        System.out.println("1. Criar Nova Carteira");
        System.out.println("2. Carregar Carteira Existente");
        System.out.println("3. Entrar como Convidado (Temp)");
        System.out.print("Escolha: ");

        String op = sc.nextLine();

        try {
            if (op.equals("1")) {
                criarCarteira();
            } else if (op.equals("2")) {
                carregarCarteira();
            } else {
                System.out.println("A entrar com carteira temporária...");
                this.myWallet = new Wallet("Guest User");
            }
        } catch (Exception e) {
            System.out.println("❌ Erro no login: " + e.getMessage());
            try { this.myWallet = new Wallet("Fallback User"); } catch (Exception ex) {}
        }
    }

    private void criarCarteira() throws Exception {
        System.out.print("\nNome do Titular: ");
        String nome = sc.nextLine();

        System.out.print("Defina uma Password: ");
        String pass = sc.nextLine();

        this.myWallet = new Wallet(nome);
        String filename = nome.replaceAll("\\s+", "");
        this.myWallet.save(filename, pass);

        System.out.println("✔ Carteira criada e guardada como '" + filename + ".wallet'");
        System.out.println("Endereço: " + myWallet.getAddress());
    }

    private void carregarCarteira() throws Exception {
        System.out.println("\n--- Ficheiros Disponíveis ---");
        File folder = new File(".");
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".wallet"));
        if (files != null) {
            for (File f : files) System.out.println(" > " + f.getName());
        }

        System.out.print("\nNome do ficheiro (ex: Tiago): ");
        String filename = sc.nextLine();

        System.out.print("Password: ");
        String pass = sc.nextLine();

        this.myWallet = Wallet.load(filename, pass);
        System.out.println("✔ Login efetuado! Bem-vindo " + myWallet.getName());
    }

    // Setters
    public void setNode(P2PNode node) { this.node = node; }
    public void setRegistry(TokenRegistry registry) { this.registry = registry; }

    // ================================
    //            MENU PRINCIPAL
    // ================================
    public void start() {
        while (true) {
            System.out.println("\n===== MENU RWA =====");
            System.out.println("Carteira Atual: " + (myWallet != null ? myWallet.getName() : "Sem Carteira"));
            System.out.println("1 - Registar novo RWA");
            System.out.println("2 - Listar RWAs");
            System.out.println("3 - Validar RWA");
            System.out.println("4 - Mostrar Blockchain");
            System.out.println("5 - Registar Renda");
            System.out.println("6 - Listar Rendas");
            System.out.println("7 - Ver Meus Saldos");
            System.out.println("8 - Transferir Tokens (Direto)");
            System.out.println("-----------------------------");
            System.out.println("9  - [MERCADO] Criar Ordem de Venda");
            System.out.println("10 - [MERCADO] Comprar Tokens");
            System.out.println("11 - [MERCADO] Confirmar Venda (Vendedor)");
            System.out.println("12 - Ver Extrato de Movimentos"); // Novo
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            try {
                String input = sc.nextLine();
                if (input.trim().isEmpty()) continue;

                int op = Integer.parseInt(input);
                switch (op) {
                    case 1 -> registarRWA();
                    case 2 -> listarRWAs();
                    case 3 -> validarRWA();
                    case 4 -> mostrarBlockchain();
                    case 5 -> registarRenda();
                    case 6 -> listarRendas();
                    case 7 -> verMeusSaldos();
                    case 8 -> transferirTokens();
                    case 9 -> criarOrdemVenda();
                    case 10 -> comprarTokens();
                    case 11 -> confirmarVenda();
                    case 12 -> verExtrato();
                    case 0 -> {
                        System.out.println("A terminar...");
                        return;
                    }
                    default -> System.out.println("Opção inválida!");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, insira um número válido.");
            }
        }
    }

    // ================================
    // 1 — REGISTAR RWA
    // ================================
    private void registarRWA() {
        try {
            if (myWallet == null) {
                System.out.println("ERRO: Nenhuma carteira configurada.");
                return;
            }

            System.out.print("ID do ativo: ");
            String id = sc.nextLine();
            System.out.print("Tipo do ativo: ");
            String tipo = sc.nextLine();
            System.out.print("Caminho do ficheiro: ");
            String path = sc.nextLine();

            oracle.registarRWA(id, tipo, path, myWallet);
            System.out.println("✔ RWA registado com sucesso!");
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro ao registar RWA: " + e.getMessage());
        }
    }

    // ================================
    // 2 — LISTAR RWA
    // ================================
    private void listarRWAs() {
        System.out.println("\n===== LISTA DE RWA's =====\n");
        for (Block b : blockchain.getBlocks()) {
            List<Object> dados = b.getData().getElements();
            if (dados.isEmpty()) continue;
            for (Object obj : dados) {
                if (obj instanceof RWARecord) {
                    printRWA((RWARecord) obj);
                }
            }
        }
    }

    private void printRWA(RWARecord r) {
        System.out.println("---------------------------------");
        System.out.println("Asset ID: " + r.getAssetID());
        System.out.println("Tipo: " + r.getAssetType());
        System.out.println("Hash Doc: " + java.util.Base64.getEncoder().encodeToString(r.getHashDocumento()));
        System.out.println("---------------------------------");
    }

    // ================================
    // 3 — VALIDAR RWA
    // ================================
    private void validarRWA() {
        try {
            System.out.print("ID do RWA: ");
            String id = sc.nextLine();
            RWARecord alvo = null;
            for (Block b : blockchain.getBlocks()) {
                for (Object obj : b.getData().getElements()) {
                    if (obj instanceof RWARecord && ((RWARecord) obj).getAssetID().equals(id)) {
                        alvo = (RWARecord) obj;
                        break;
                    }
                }
                if (alvo != null) break;
            }

            if (alvo == null) {
                System.out.println("❌ RWA não encontrado!");
                return;
            }
            System.out.print("Caminho do ficheiro REAL: ");
            String path = sc.nextLine();
            boolean ok = validator.validar(alvo, path);
            System.out.println("Resultado: " + (ok ? "✔ VÁLIDO" : "❌ INVÁLIDO"));
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ================================
    // 4 — MOSTRAR BLOCKCHAIN
    // ================================
    private void mostrarBlockchain() {
        System.out.println("\n===== BLOCKCHAIN =====\n");
        for (Block b : blockchain.getBlocks()) {
            System.out.println("------ BLOCO " + b.getID() + " ------");
            System.out.println(b.toStringHeader());
            System.out.println("Dados: " + b.getData().getElements());
            System.out.println();
        }
    }

    // ================================
    // 5 & 6 — RENDAS
    // ================================
    private void registarRenda() {
        try {
            System.out.print("ID do ativo: ");
            String id = sc.nextLine();
            System.out.print("Valor da renda (€): ");
            double valor = Double.parseDouble(sc.nextLine());
            oracle.registarRenda(id, valor);
            System.out.println("✔ Renda registada com sucesso!");
            propagarUltimoBloco();
        } catch (Exception e) {
            System.out.println("Erro ao registar renda: " + e.getMessage());
        }
    }

    private void listarRendas() {
        System.out.println("\n===== RENDAS NA BLOCKCHAIN =====\n");
        for (Block b : blockchain.getBlocks()) {
            for (Object obj : b.getData().getElements()) {
                if (obj instanceof RentDistributionEvent) {
                    RentDistributionEvent e = (RentDistributionEvent) obj;
                    System.out.println("Ativo: " + e.getAssetID() + " | " + e.getAmount() + "€ | " + new java.util.Date(e.getTimestamp()));
                }
            }
        }
    }

    // ================================
    // 7 — VER MEUS SALDOS
    // ================================
    private void verMeusSaldos() {
        if (registry == null) {
            System.out.println("Erro: TokenRegistry não configurado.");
            return;
        }
        System.out.println("\n===== MEUS SALDOS =====");
        System.out.println("Wallet Addr: " + myWallet.getAddress());
        registry.sync(blockchain);
        registry.printWalletBalance(myWallet.getAddress());
        System.out.println("=======================");
    }

    // ================================
    // 8 — TRANSFERIR TOKENS
    // ================================
    private void transferirTokens() {
        try {
            if (myWallet == null) return;
            System.out.println("\n--- TRANSFERENCIA DIRETA P2P ---");
            System.out.print("ID do Ativo: ");
            String assetId = sc.nextLine();

            // Verificar Saldo
            if (registry != null) {
                registry.sync(blockchain);
                int saldo = registry.getBalance(myWallet.getAddress(), assetId);
                System.out.println("Saldo Atual: " + saldo);
                if (saldo <= 0) {
                    System.out.println("❌ Saldo insuficiente.");
                    return;
                }
            }

            System.out.println("Destinatário (Public Key): ");
            String receiverAddress = sc.nextLine();
            if (receiverAddress.equals(myWallet.getAddress())) {
                System.out.println("❌ Erro: Destino igual a origem.");
                return;
            }

            System.out.print("Quantidade: ");
            int amount = Integer.parseInt(sc.nextLine());

            // Criar Transação (Preço 0.0 pois é transferencia direta)
            Transaction tx = new Transaction(
                    Transaction.Type.TRANSFER_TOKEN,
                    myWallet.getAddress(),
                    receiverAddress,
                    assetId,
                    amount,
                    0.0,
                    "Transferencia P2P"
            );
            tx.sign(myWallet.getPrivateKey());

            if (!tx.isValid()) {
                System.out.println("❌ Falha na assinatura digital.");
                return;
            }

            blockchain.add(java.util.List.of(tx));
            System.out.println("✔ Transferência realizada!");
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    
    // ================================
    // 9 - MERCADO: CRIAR VENDA
    // ================================
    private void criarOrdemVenda() {
        try {
            System.out.println("\n--- CRIAR VENDA NO ESCROW ---");
            System.out.print("ID do Ativo: ");
            String assetId = sc.nextLine();
            
            System.out.print("Quantidade a vender: ");
            int qtd = Integer.parseInt(sc.nextLine());
            
            System.out.print("Preço Total (€): ");
            double preco = Double.parseDouble(sc.nextLine());

            // Venda: Sender -> "ESCROW" (TokenRegistry gere isto)
            Transaction tx = new Transaction(
                    Transaction.Type.CREATE_SALE,
                    myWallet.getAddress(),
                    "ESCROW",
                    assetId,
                    qtd,
                    preco,
                    "Venda Marketplace"
            );
            tx.sign(myWallet.getPrivateKey());

            blockchain.add(java.util.List.of(tx));
            System.out.println("✔ Oferta de venda criada! Tokens bloqueados no Escrow.");
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    // ================================
    // 10 - MERCADO: COMPRAR (RESERVAR)
    // ================================
    private void comprarTokens() {
        System.out.println("\n--- MERCADO (ORDENS ABERTAS) ---");
        List<Transaction> orders = escrowManager.getOpenOrders();
        
        if (orders.isEmpty()) {
            System.out.println("Nenhuma venda ativa.");
            return;
        }

        for (int i = 0; i < orders.size(); i++) {
            Transaction t = orders.get(i);
            String sellerShort = t.getSender().substring(0, 10);
            System.out.printf("[%d] %s | Qtd: %d | Preço: %.2f € | Vendedor: %s...\n", 
                    i, t.getAssetID(), t.getAmount(), t.getPrice(), sellerShort);
        }

        System.out.print("\nQual número queres comprar? (-1 sair): ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            if (idx < 0 || idx >= orders.size()) return;

            Transaction offer = orders.get(idx);
            if (offer.getSender().equals(myWallet.getAddress())) {
                System.out.println("❌ Não podes comprar a tua própria venda!");
                return;
            }

            System.out.println("A reservar compra...");
            
            Transaction buyTx = new Transaction(
                    Transaction.Type.BUY_SALE,
                    myWallet.getAddress(),
                    myWallet.getAddress(),
                    offer.getAssetID(),
                    offer.getAmount(),
                    offer.getPrice(),
                    "Reserva de Compra"
            );
            
            // Ligar à venda original
            buyTx.setTransactionRef(offer.getTransactionID());
            buyTx.sign(myWallet.getPrivateKey());

            blockchain.add(java.util.List.of(buyTx));
            System.out.println("✔ Reserva efetuada! Aguarde a confirmação do vendedor.");
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
    
    // ================================
    // 11 - MERCADO: CONFIRMAR (LIBERTAR)
    // ================================
    private void confirmarVenda() {
        System.out.println("\n--- VENDAS PENDENTES DE APROVAÇÃO ---");
        List<Transaction> requests = escrowManager.getPendingApprovals(myWallet.getAddress());
        
        if (requests.isEmpty()) {
            System.out.println("Não tens vendas com compradores à espera.");
            return;
        }

        for (int i = 0; i < requests.size(); i++) {
            Transaction buy = requests.get(i);
            Transaction originalSale = escrowManager.getSaleByRef(buy.getTransactionRef());
            System.out.printf("[%d] Comprador: %s... | Ativo: %s | Valor: %.2f €\n", 
                    i, buy.getSender().substring(0, 10), originalSale.getAssetID(), originalSale.getPrice());
        }

        System.out.print("Confirmar recebimento e libertar tokens? (nº ou -1): ");
        try {
            int idx = Integer.parseInt(sc.nextLine());
            if (idx >= 0 && idx < requests.size()) {
                Transaction buyTx = requests.get(idx);
                Transaction saleTx = escrowManager.getSaleByRef(buyTx.getTransactionRef());
                
                // Transação final: ESCROW -> COMPRADOR
                Transaction confirmTx = new Transaction(
                        Transaction.Type.CONFIRM_SALE,
                        myWallet.getAddress(), // Vendedor assina
                        buyTx.getSender(),     // Destino = Comprador
                        saleTx.getAssetID(),
                        saleTx.getAmount(),
                        saleTx.getPrice(),
                        "Libertação Escrow"
                );
                
                confirmTx.setTransactionRef(saleTx.getTransactionID());
                confirmTx.sign(myWallet.getPrivateKey());
                
                blockchain.add(java.util.List.of(confirmTx));
                System.out.println("✔ Tokens libertados do Escrow com sucesso!");
                propagarUltimoBloco();
            }
        } catch (Exception e) {
             System.out.println("Erro: " + e.getMessage());
        }
    }

    // ================================
    // 12 - EXTRATO DE MOVIMENTOS
    // ================================
    private void verExtrato() {
        if (myWallet == null) return;
        
        System.out.println("\n=== EXTRATO DE MOVIMENTOS: " + myWallet.getName() + " ===");
        String myAddr = myWallet.getAddress();
        boolean encontrou = false;

        // Percorrer a Blockchain do início ao fim
        for (Block b : blockchain.getBlocks()) {
            for (Object obj : b.getData().getElements()) {
                
                if (obj instanceof Transaction) {
                    Transaction tx = (Transaction) obj;
                    
                    // Verificar se sou o Remetente OU o Destinatário
                    boolean souRemetente = tx.getSender().equals(myAddr);
                    boolean souDestinatario = tx.getReceiver().equals(myAddr);

                    if (souRemetente || souDestinatario) {
                        encontrou = true;
                        String tipo = "";
                        String detalhes = "";
                        
                        // Formatar a mensagem dependendo do tipo
                        if (tx.getType() == Transaction.Type.MINT_TOKEN) {
                            tipo = "[+] MINT";
                            detalhes = "Recebidos 1000 tokens (Criação)";
                        } 
                        else if (tx.getType() == Transaction.Type.TRANSFER_TOKEN) {
                            if (souRemetente) {
                                tipo = "[-] ENVIO";
                                detalhes = "Enviaste " + tx.getAmount() + " tokens (" + tx.getAssetID() + ")";
                            } else {
                                tipo = "[+] RECEBIDO";
                                detalhes = "Recebeste " + tx.getAmount() + " tokens (" + tx.getAssetID() + ")";
                            }
                        }
                        else if (tx.getType() == Transaction.Type.CREATE_SALE) {
                            tipo = "[-] ESCROW";
                            detalhes = "Colocaste à venda " + tx.getAmount() + " tokens por " + tx.getPrice() + "€";
                        }
                        else if (tx.getType() == Transaction.Type.BUY_SALE) {
                            tipo = "[!] RESERVA";
                            detalhes = "Reservaste compra de " + tx.getAssetID();
                        }
                        else if (tx.getType() == Transaction.Type.CONFIRM_SALE) {
                            if (souRemetente) { // Vendedor
                                tipo = "[OK] VENDA";
                                detalhes = "Confirmaste a venda. Tokens libertados.";
                            } else { // Comprador
                                tipo = "[+] COMPRA";
                                detalhes = "Compra finalizada! Recebeste " + tx.getAmount() + " tokens.";
                            }
                        }

                        // Imprimir linha bonita
                        System.out.printf("%s | Data: %s | %s\n", 
                                tipo, new java.util.Date(tx.getTimestamp()).toString(), detalhes);
                    }
                }
            }
        }
        
        if (!encontrou) {
            System.out.println("(Nenhum movimento encontrado)");
        }
        System.out.println("==============================================");
    }
    
    // ================================
    // AUXILIAR DE REDE
    // ================================
    private void propagarUltimoBloco() {
        if (node == null) return;
        try {
            Block lastBlock = blockchain.getLastBlock();
            System.out.println(">> A propagar bloco " + lastBlock.getID() + " para a rede...");
            node.broadcast(new Message(Message.Type.NEW_BLOCK, lastBlock));
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível propagar o bloco.");
        }
    }
}