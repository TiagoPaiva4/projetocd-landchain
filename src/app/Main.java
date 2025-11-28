/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package app;

import core.Block;
import core.BlockChain;
import java.util.List;
import rwa.RWAExplorer;
/**
 *
 * @author Tiago Paiva
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Criar genesis block
            Block genesis = new Block(
                    0,
                    new byte[32],
                    3,
                    List.of("GENESIS")
            );
            genesis.mine();

            // Criar blockchain
            BlockChain bc = new BlockChain(genesis);

            // Iniciar menu
            RWAExplorer menu = new RWAExplorer(bc);
            menu.start();

        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
