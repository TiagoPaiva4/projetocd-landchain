package rwa;

import core.Block;
import core.BlockChain;
import core.Transaction;
import events.RentDistributionEvent;
import network.Message;
import network.P2PNode;
import token.TokenRegistry;
import token.Wallet;

import java.io.File; // <--- Import necessário para listar ficheiros
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

    // Carteira do utilizador atual
    private Wallet myWallet;

    public RWAExplorer(BlockChain bc, Oracle oracle, RWAValidator validator) {
        this.blockchain = bc;
        this.oracle = oracle;
        this.validator = validator;
        this.sc = new Scanner(System.in);

        // --- MUDANÇA: Em vez de criar User Admin, chamamos o Login ---
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
            // Fallback para não crashar
            try { this.myWallet = new Wallet("Fallback User"); } catch (Exception ex) {}
        }
    }

    private void criarCarteira() throws Exception {
        System.out.print("\nNome do Titular: ");
        String nome = sc.nextLine();

        System.out.print("Defina uma Password: ");
        String pass = sc.nextLine();

        // 1. Criar Objeto
        this.myWallet = new Wallet(nome);

        // 2. Salvar no disco (Remove espaços do nome para o ficheiro)
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

        // Tentar carregar
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
            System.out.println("8 - Transferir Tokens");
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
                List<Object> dados = b.getData().getElements();
                for (Object obj : dados) {
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
    // 5 — REGISTAR RENDA
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

    // ================================
    // 6 — LISTAR RENDAS
    // ================================
    private void listarRendas() {
        System.out.println("\n===== RENDAS NA BLOCKCHAIN =====\n");
        for (Block b : blockchain.getBlocks()) {
            List<Object> dados = b.getData().getElements();
            if (dados.isEmpty()) continue;

            for (Object obj : dados) {
                if (obj instanceof RentDistributionEvent) {
                    printRenda((RentDistributionEvent) obj);
                }
            }
        }
    }

    private void printRenda(RentDistributionEvent e) {
        System.out.println("---------------------------------");
        System.out.println("Ativo: " + e.getAssetID());
        System.out.println("Renda: " + e.getAmount() + " €");
        System.out.println("Data: " + new java.util.Date(e.getTimestamp()));
        System.out.println("---------------------------------");
    }

    // ================================
    // 7 — VER MEUS SALDOS
    // ================================
    private void verMeusSaldos() {
        if (registry == null) {
            System.out.println("Erro: TokenRegistry não configurado.");
            return;
        }
        
        // Sincronizar para garantir dados frescos
        registry.sync(blockchain);

        // MUDANÇA AQUI: Chamamos o método específico passando o nosso endereço
        registry.printWalletBalance(myWallet.getAddress());
    }

    // ================================
    // 8 — TRANSFERIR TOKENS
    // ================================
    private void transferirTokens() {
        try {
            if (myWallet == null) {
                System.out.println("ERRO: Nenhuma carteira configurada.");
                return;
            }

            System.out.println("\n--- TRANSFERENCIA DE TOKENS ---");
            System.out.print("ID do Ativo (ex: 5): ");
            String assetId = sc.nextLine();

            // Verificar Saldo
            if (registry != null) {
                registry.sync(blockchain);
                int saldo = registry.getBalance(myWallet.getAddress(), assetId);
                System.out.println("Teu Saldo Atual: " + saldo);
                if (saldo <= 0) {
                    System.out.println("❌ Não tens tokens deste ativo para enviar.");
                    return;
                }
            }

            System.out.println("Destinatário (Cola a Chave Pública/Address): ");
            String receiverAddress = sc.nextLine();

            if (receiverAddress.equals(myWallet.getAddress())) {
                System.out.println("❌ Não podes enviar para ti próprio.");
                return;
            }

            System.out.print("Quantidade a enviar: ");
            int amount = Integer.parseInt(sc.nextLine());

            if (amount <= 0) {
                System.out.println("❌ Quantidade inválida.");
                return;
            }

            // Criar Transação
            Transaction tx = new Transaction(
                    Transaction.Type.TRANSFER_TOKEN,
                    myWallet.getAddress(),
                    receiverAddress,
                    assetId,
                    amount,
                    "Transferencia P2P"
            );

            // Assinar
            tx.sign(myWallet.getPrivateKey());

            if (!tx.isValid()) {
                System.out.println("❌ Erro critico: Falha na assinatura digital.");
                return;
            }

            // Adicionar à blockchain
            java.util.List<Object> blockData = java.util.List.of(tx);
            blockchain.add(blockData);

            System.out.println("✔ Transação realizada com sucesso!");
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro na transferencia: " + e.getMessage());
            e.printStackTrace();
        }
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