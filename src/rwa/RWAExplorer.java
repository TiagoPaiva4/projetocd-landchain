/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rwa;

import core.Block;
import core.BlockChain;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Tiago Paiva
 */
public class RWAExplorer {

    private BlockChain blockchain;
    private Oracle oracle;
    private Scanner sc;

    public RWAExplorer(BlockChain bc) throws Exception {
        this.blockchain = bc;
        this.oracle = new Oracle(bc);
        this.sc = new Scanner(System.in);
    }

    // ================================
    //       MENU PRINCIPAL
    // ================================
    public void start() {

        while (true) {
            System.out.println("\n===== MENU RWA =====");
            System.out.println("1 - Registar novo RWA");
            System.out.println("2 - Listar RWAs");
            System.out.println("3 - Validar RWA");
            System.out.println("4 - Mostrar Blockchain");
            System.out.println("0 - Sair");
            System.out.print("Opção: ");

            int op = sc.nextInt();
            sc.nextLine(); // limpar enter

            switch (op) {
                case 1 -> registarRWA();
                case 2 -> listarRWAs();
                case 3 -> validarRWA();
                case 4 -> mostrarBlockchain();
                case 0 -> {
                    System.out.println("A terminar...");
                    return;
                }
                default -> System.out.println("Opção inválida!");
            }
        }
    }

    // ================================
    //     1 — REGISTAR RWA
    // ================================
    private void registarRWA() {
        try {
            System.out.print("ID do ativo: ");
            String id = sc.nextLine();

            System.out.print("Tipo do ativo (ex: IMÓVEL, CERTIFICADO): ");
            String tipo = sc.nextLine();

            System.out.print("Caminho do ficheiro: ");
            String path = sc.nextLine();

            oracle.registarRWA(id, tipo, path);

            System.out.println("✔ RWA registado com sucesso!");

        } catch (Exception e) {
            System.out.println("Erro ao registar RWA: " + e.getMessage());
        }
    }

    // ================================
    //     2 — LISTAR RWAs
    // ================================
    private void listarRWAs() {
        System.out.println("\n===== LISTA DE RWA's NA BLOCKCHAIN =====\n");

        for (Block b : blockchain.getBlocks()) {
            if (b.getID() == 0) {
                System.out.println("(GENESIS BLOCK)");
                continue;
            }

            List<Object> dados = b.getData().getElements();

            if (dados.isEmpty()) continue;

            Object obj = dados.get(0);

            if (obj instanceof RWARecord record) {
                printRecord(record);
            }
        }
    }

    private void printRecord(RWARecord r) {
        System.out.println("---------------------------------");
        System.out.println("Asset ID: " + r.getAssetID());
        System.out.println("Tipo: " + r.getAssetType());
        System.out.println("Timestamp: " + r.getTimestamp());
        System.out.println("Hash (base64): " +
                java.util.Base64.getEncoder().encodeToString(r.getHashDocumento()));
        System.out.println("---------------------------------");
    }

    // ================================
    //     3 — VALIDAR RWA
    // ================================
    private void validarRWA() {
        try {
            System.out.print("ID do RWA a validar: ");
            String id = sc.nextLine();

            RWARecord alvo = null;

            for (Block b : blockchain.getBlocks()) {
                if (b.getID() == 0) continue;
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

            System.out.print("Caminho do ficheiro REAL para validar: ");
            String path = sc.nextLine();

            boolean valido = RWAValidator.validar(alvo, path);

            System.out.println("\nResultado: " +
                    (valido ? "✔ VÁLIDO" : "❌ INVÁLIDO"));

        } catch (Exception e) {
            System.out.println("Erro ao validar RWA: " + e.getMessage());
        }
    }

    // ================================
    //     4 — MOSTRAR BLOCKCHAIN
    // ================================
    private void mostrarBlockchain() {
        System.out.println("\n===== BLOCKCHAIN =====\n");

        for (Block b : blockchain.getBlocks()) {
            System.out.println("------ BLOCO " + b.getID() + " ------");
            System.out.println(b);
            System.out.println();
        }
    }
}

