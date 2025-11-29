package rwa;

import core.Block;
import core.BlockChain;
import events.RentDistributionEvent;
import network.Message;  // <--- IMPORTANTE
import network.P2PNode;  // <--- IMPORTANTE

import java.util.List;
import java.util.Scanner;

public class RWAExplorer {

    private final BlockChain blockchain;
    private final Oracle oracle;
    private final RWAValidator validator;
    private final Scanner sc;
    
    // Variável para comunicar com a rede
    private P2PNode node;

    public RWAExplorer(BlockChain bc, Oracle oracle, RWAValidator validator) {
        this.blockchain = bc;
        this.oracle = oracle;
        this.validator = validator;
        this.sc = new Scanner(System.in);
    }

    /**
     * Define o nó de rede para permitir broadcast de blocos
     * @param node O nó P2P iniciado no Main
     */
    public void setNode(P2PNode node) {
        this.node = node;
    }

    // ================================
    //            MENU
    // ================================
    public void start() {

        while (true) {
            System.out.println("\n===== MENU RWA =====");
            System.out.println("1 - Registar novo RWA");
            System.out.println("2 - Listar RWAs");
            System.out.println("3 - Validar RWA");
            System.out.println("4 - Mostrar Blockchain");
            System.out.println("5 - Registar Renda");
            System.out.println("6 - Listar Rendas");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            try {
                int op = Integer.parseInt(sc.nextLine());

                switch (op) {
                    case 1 -> registarRWA();
                    case 2 -> listarRWAs();
                    case 3 -> validarRWA();
                    case 4 -> mostrarBlockchain();
                    case 5 -> registarRenda();
                    case 6 -> listarRendas();
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
            System.out.print("ID do ativo: ");
            String id = sc.nextLine();

            System.out.print("Tipo do ativo: ");
            String tipo = sc.nextLine();

            System.out.print("Caminho do ficheiro: ");
            String path = sc.nextLine();

            // 1. Registar localmente (Oracle cria o bloco e adiciona à chain)
            oracle.registarRWA(id, tipo, path);

            System.out.println("✔ RWA registado com sucesso!");

            // 2. AVISAR A REDE (Broadcast)
            propagarUltimoBloco();

        } catch (Exception e) {
            System.out.println("Erro ao registar RWA: " + e.getMessage());
            e.printStackTrace();
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

            Object obj = dados.get(0);

            if (obj instanceof RWARecord record) {
                printRWA(record);
            }
        }
    }

    private void printRWA(RWARecord r) {
        System.out.println("---------------------------------");
        System.out.println("Asset ID: " + r.getAssetID());
        System.out.println("Tipo: " + r.getAssetType());
        System.out.println("Timestamp: " + r.getTimestamp());
        System.out.println("Hash Doc: " +
                java.util.Base64.getEncoder().encodeToString(r.getHashDocumento()));
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
                if (dados.isEmpty()) continue;

                Object obj = dados.get(0);

                if (obj instanceof RWARecord record &&
                        record.getAssetID().equals(id)) {
                    alvo = record;
                    break;
                }
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
            System.out.println(b.toStringHeader()); // Usar apenas Header para não poluir
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

            // 1. Registar localmente
            oracle.registarRenda(id, valor);

            System.out.println("✔ Renda registada com sucesso!");

            // 2. AVISAR A REDE (Broadcast)
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

            Object obj = dados.get(0);

            if (obj instanceof RentDistributionEvent e) {
                printRenda(e);
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
    // MÉTODO AUXILIAR DE REDE
    // ================================
    private void propagarUltimoBloco() {
        // Se a rede não estiver ligada (ex: modo offline), ignora
        if (node == null) return;

        try {
            Block lastBlock = blockchain.getLastBlock();
            System.out.println(">> A propagar bloco " + lastBlock.getID() + " para a rede...");
            
            node.broadcast(new Message(Message.Type.NEW_BLOCK, lastBlock));
            
        } catch (Exception e) {
            System.out.println("Aviso: Não foi possível propagar o bloco. " + e.getMessage());
        }
    }
}