/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

/**
 *
 * @author Tiago Paiva
 */
import core.Block;
import core.BlockChain;
import java.util.List;
import rwa.Oracle;
import rwa.RWARecord;
import rwa.RWAValidator;

public class RWADemo {

    public static void main(String[] args) {

        try {
            //-------------------------------------------------------
            // 1) Criar GENESIS BLOCK
            //-------------------------------------------------------
            System.out.println("=== Criar Genesis Block ===");

            Block genesis = new Block(
                    0,                  // ID
                    new byte[32],       // previousHash = 32 zeros
                    3,                  // difficulty (podes ajustar)
                    List.of("GENESIS")  // data do genesis
            );

            genesis.mine();  // minerar o bloco inicial

            //-------------------------------------------------------
            // 2) Criar Blockchain com genesis
            //-------------------------------------------------------
            BlockChain blockchain = new BlockChain(genesis);

            //-------------------------------------------------------
            // 3) Criar Oracle
            //-------------------------------------------------------
            Oracle oracle = new Oracle(blockchain);

            //-------------------------------------------------------
            // 4) Caminho para o ficheiro REAL
            //-------------------------------------------------------
            String filePath = "C:\\Users\\Tiago Paiva\\Downloads\\SO.pdf"; // <-- ALTERA AQUI

            //-------------------------------------------------------
            // 5) Registar RWA
            //-------------------------------------------------------
            System.out.println("\n=== Registar RWA ===");

            oracle.registarRWA("RWA001", "IMÓVEL", filePath);

            //-------------------------------------------------------
            // 6) Obter último bloco após registo
            //-------------------------------------------------------
            Block ultimo = blockchain.getLastBlock();

            List<Object> dados = ultimo.getData().getElements();

            // O RWA está em dados[0]
            RWARecord record = (RWARecord) dados.get(0);

            System.out.println("\nRWA registado:");
            System.out.println(record);

            //-------------------------------------------------------
            // 7) Validar RWA
            //-------------------------------------------------------
            System.out.println("\n=== Validar RWA ===");

            boolean valido = RWAValidator.validar(record, filePath);

            System.out.println("\nResultado final: " +
                    (valido ? "✔ RWA VÁLIDO" : "❌ RWA INVÁLIDO"));

        } catch (Exception e) {
            System.out.println("ERRO NO DEMO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
