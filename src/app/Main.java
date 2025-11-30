package app;

import core.Block;
import core.BlockChain;
import events.EventManager;
import events.RentListener;
import network.P2PNode;
import network.Message;
import token.TokenRegistry;
import token.TokenRegistryListener;
import rwa.Oracle;
import rwa.RWAExplorer;
import rwa.RWAValidator;

import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("=== CONFIGURACAO P2P RWA ===");
            
            // ---------------------------------------------------------
            // 0 — Configurar Rede (Porta e Conexão)
            // ---------------------------------------------------------
            System.out.print("Digite a porta deste No (ex: 5000): ");
            int myPort = scanner.nextInt();
            scanner.nextLine(); // limpar buffer

            // ---------------------------------------------------------
            // 1 & 2 — Blockchain (Carregar ou Criar Genesis)
            // ---------------------------------------------------------
            BlockChain bc;
            try {
                // Tenta carregar do disco primeiro
                System.out.println("A carregar blockchain do disco...");
                bc = BlockChain.load(BlockChain.FILE_PATH + "blockchain.bch");
                
                if (bc == null) {
                    throw new Exception("Blockchain nao encontrada.");
                }
                System.out.println("Blockchain carregada! Tamanho: " + bc.getBlocks().size());
            } catch (Exception e) {
                // Se falhar, cria o GENESIS
                System.out.println("Criando GENESIS BLOCK...");
                Block genesis = new Block(
                        0,
                        new byte[32], // Hash anterior zerado
                        3,            // Dificuldade
                        List.of("GENESIS BLOCK", "RWA NETWORK START")
                );
                genesis.mine();
                bc = new BlockChain(genesis);
            }

            // ---------------------------------------------------------
            // 3 — Iniciar Rede P2P
            // ---------------------------------------------------------
            P2PNode node = new P2PNode(myPort, bc);
            node.start(); // Inicia o servidor em background

            System.out.print("Deseja conectar a outro peer? (s/n): ");
            String connect = scanner.nextLine();
            if (connect.equalsIgnoreCase("s")) {
                System.out.print("IP (ex: localhost): ");
                String host = scanner.nextLine();
                System.out.print("Porta (ex: 5001): ");
                int port = Integer.parseInt(scanner.nextLine());
                node.connectToPeer(host, port);
            }

            // ---------------------------------------------------------
            // 4 — Event Manager (Lógica de Negócio RWA)
            // ---------------------------------------------------------
            EventManager events = new EventManager();

            // 5 — Token Registry
            TokenRegistry registry = new TokenRegistry();
            events.subscribe(new TokenRegistryListener(registry));

            // 6 — Rent Listener (AGORA RECEBE O REGISTRY TAMBÉM)
            events.subscribe(new RentListener(registry));

            // 7 — Oracle (Usa o EventManager e a Blockchain partilhada)
            Oracle oracle = new Oracle(bc, events);

            // 8 — Validator
            RWAValidator validator = new RWAValidator();

            // ---------------------------------------------------------
            // 9 — Arrancar Aplicação Visual/Menu (Explorer)
            // ---------------------------------------------------------
            System.out.println("\n=== A INICIAR RWA EXPLORER ===");
            
            RWAExplorer explorer = new RWAExplorer(bc, oracle, validator);
            
            explorer.setNode(node); 
            explorer.setRegistry(registry); // <--- ADICIONA ISTO (Se quiseres ver saldos no menu)
            
            explorer.start();

        } catch (Exception e) {
            e.printStackTrace(); 
            System.out.println("Erro Crítico: " + e.getMessage());
        }
    }
}