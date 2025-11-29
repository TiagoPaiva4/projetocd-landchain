package rwa;

import core.Block;
import core.BlockChain;
import events.RentDistributionEvent;
import network.Message;
import network.P2PNode;
import token.TokenRegistry;
import token.Wallet;

import java.util.List;
import java.util.Scanner;

public class RWAExplorer {

    private final BlockChain blockchain;
    private final Oracle oracle;
    private final RWAValidator validator;
    private final Scanner sc;
    
    // Dependências externas (injetadas via setters)
    private P2PNode node;
    private TokenRegistry registry;
    
    // Carteira do utilizador atual (simulação)
    private Wallet myWallet; 

    public RWAExplorer(BlockChain bc, Oracle oracle, RWAValidator validator) {
        this.blockchain = bc;
        this.oracle = oracle;
        this.validator = validator;
        this.sc = new Scanner(System.in);
        
        // Inicializar uma carteira de teste
        try {
            this.myWallet = new Wallet("User Admin");
            System.out.println("Carteira carregada: " + myWallet.getAddress());
        } catch (Exception e) {
            System.err.println("Erro ao criar carteira: " + e.getMessage());
        }
    }

    // Setter para a Rede P2P
    public void setNode(P2PNode node) {
        this.node = node;
    }

    // Setter para o Registo de Tokens (para ver saldos)
    public void setRegistry(TokenRegistry registry) {
        this.registry = registry;
    }

    // ================================
    //            MENU
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
            System.out.println("7 - Ver Meus Saldos"); // <--- NOVA OPÇÃO
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            try {
                String input = sc.nextLine();
                // Proteção contra enter vazio
                if(input.trim().isEmpty()) continue;
                
                int op = Integer.parseInt(input);
                switch (op) {
                    case 1 -> registarRWA();
                    case 2 -> listarRWAs();
                    case 3 -> validarRWA();
                    case 4 -> mostrarBlockchain();
                    case 5 -> registarRenda();
                    case 6 -> listarRendas();
                    case 7 -> verMeusSaldos();
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

            // Passamos a wallet para receber os tokens (MINT)
            oracle.registarRWA(id, tipo, path, myWallet);

            System.out.println("✔ RWA registado com sucesso!");

            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro ao registar RWA: " + e.getMessage());
            // e.printStackTrace(); // Descomentar para debug
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
            
            // Procura dentro do bloco se existe um RWARecord
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
        System.out.println("Timestamp: " + r.getTimestamp());
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
            
            // Procura o registo na blockchain
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
    // 7 — VER MEUS SALDOS (NOVO)
    // ================================
    private void verMeusSaldos() {
        if (registry == null) {
            System.out.println("Erro: TokenRegistry não configurado no Main.");
            return;
        }
        
        System.out.println("\n===== MEUS SALDOS =====");
        System.out.println("Wallet Addr: " + myWallet.getAddress());
        
        // 1. Forçar sincronização do registry com a blockchain atual
        registry.sync(blockchain);
        
        // 2. Imprimir todos os saldos (Para debug, imprime tudo, depois podes filtrar)
        registry.printBalances();
        
        System.out.println("=======================");
    }

    // ================================
    // MÉTODO AUXILIAR DE REDE
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