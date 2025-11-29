package app;

import core.Block;
import core.BlockChain;
import events.EventManager;
import events.RentListener;
import token.TokenRegistry;
import token.TokenRegistryListener;
import rwa.Oracle;
import rwa.RWAExplorer;
import rwa.RWAValidator;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        try {
            // 1 — Criar GENESIS BLOCK
            Block genesis = new Block(
                    0,
                    new byte[32],
                    3,
                    List.of("GENESIS")
            );
            genesis.mine();

            // 2 — Blockchain
            BlockChain bc = new BlockChain(genesis);

            // 3 — Event Manager (único)
            EventManager events = new EventManager();

            // 4 — Token Registry
            TokenRegistry registry = new TokenRegistry();
            events.subscribe(new TokenRegistryListener(registry));

            // 5 — Rent Listener
            events.subscribe(new RentListener(bc));

            // 6 — Oracle usa o MESMO EventManager
            Oracle oracle = new Oracle(bc, events);

            // 7 — Validator
            RWAValidator validator = new RWAValidator();

            // 8 — Explorer
            RWAExplorer explorer = new RWAExplorer(bc, oracle, validator);

            // 9 — Arrancar aplicação
            explorer.start();

        } catch (Exception e) {
            e.printStackTrace(); // <<< MUITO IMPORTANTE
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
