/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import core.Block;
import core.BlockChain;
import java.util.List;
import rwa.RWAExplorer;
import events.EventManager;
import token.TokenRegistry;
import token.TokenRegistryListener;
import rwa.RWAValidator;
import rwa.RWAExplorer;
import rwa.Oracle;
/**
 *
 * @author Tiago Paiva
 */


import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {

            // 1) Criar génesis block
            Block genesis = new Block(
                    0,
                    new byte[32],   // previous hash vazio
                    3,              // dificuldade
                    List.of("GENESIS")
            );
            genesis.mine();

            // 2) Criar blockchain
            BlockChain bc = new BlockChain(genesis);

            // 3) Criar gestor de eventos (EventManager)
            EventManager events = new EventManager();

            // 4) Criar TokenRegistry + Listener que reage aos eventos RWA
            TokenRegistry registry = new TokenRegistry();
            events.subscribe(new TokenRegistryListener(registry));

            // 5) Criar o validador de RWA
            RWAValidator validator = new RWAValidator();

            // 6) Criar Oracle (responsável por registar ativos reais na blockchain)
            Oracle oracle = new Oracle(bc, events);

            // 7) Criar e arrancar o menu (a interface CLI do utilizador)
            RWAExplorer menu = new RWAExplorer(bc, oracle, validator);
            menu.start();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
