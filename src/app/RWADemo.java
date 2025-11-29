package app;

import core.Block;
import core.BlockChain;
import events.EventManager;
import rwa.Oracle;
import rwa.RWARecord;
import rwa.RWAValidator;

import java.util.List;

public class RWADemo {

    public static void main(String[] args) {

        try {
            //-------------------------------------------------------
            // 1) Criar GENESIS BLOCK
            //-------------------------------------------------------
            System.out.println("=== Criar Genesis Block ===");

            Block genesis = new Block(
                    0,
                    new byte[32],
                    3,
                    List.of("GENESIS")
            );

            genesis.mine();

            //-------------------------------------------------------
            // 2) Criar Blockchain
            //-------------------------------------------------------
            BlockChain blockchain = new BlockChain(genesis);

            //-------------------------------------------------------
            // 3) Criar EventManager
            //-------------------------------------------------------
            EventManager events = new EventManager();

            //-------------------------------------------------------
            // 4) Criar Oracle (agora requer EventManager!)
            //-------------------------------------------------------
            Oracle oracle = new Oracle(blockchain, events);

            //-------------------------------------------------------
            // 5) Caminho do documento real
            //-------------------------------------------------------
            String filePath = "C:\\Users\\Tiago Paiva\\Downloads\\SO.pdf";

            //-------------------------------------------------------
            // 6) Registar RWA
            //-------------------------------------------------------
            System.out.println("\n=== Registar RWA ===");

            oracle.registarRWA("RWA001", "IMÓVEL", filePath);

            //-------------------------------------------------------
            // 7) Capturar último bloco
            //-------------------------------------------------------
            Block ultimo = blockchain.getLastBlock();

            List<Object> dados = ultimo.getData().getElements();

            RWARecord record = (RWARecord) dados.get(0);

            System.out.println("\nRWA registado:");
            System.out.println(record);

            //-------------------------------------------------------
            // 8) Validar o RWA
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
