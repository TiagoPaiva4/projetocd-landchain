/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package network;


import core.Block;
import core.BlockChain;
import core.Transaction;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Tiago Paiva
 */

public class P2PNode {
    private int port;
    private List<PeerHandler> peers; // Lista de nós conectados
    private BlockChain blockchain;   // Referência para a tua blockchain local
    private boolean running;

    public P2PNode(int port, BlockChain blockchain) {
        this.port = port;
        this.blockchain = blockchain;
        this.peers = new ArrayList<>();
    }

    // No método start()
    public void start() {
        this.running = true;
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("No P2P iniciado na porta: " + port);
                while (running) {
                    Socket socket = serverSocket.accept();
                    try {
                        // Tenta criar o handler
                        PeerHandler peer = new PeerHandler(socket, this);
                        peers.add(peer);
                        new Thread(peer).start();
                    } catch (Exception e) {
                        System.out.println("Erro ao aceitar conexão: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }).start();
    }

    // No método connectToPeer()
    public void connectToPeer(String host, int port) {
        try {
            Socket socket = new Socket(host, port);
            // Agora o try-catch apanha o erro se falhar a criar streams
            PeerHandler peer = new PeerHandler(socket, this);
            peers.add(peer);
            new Thread(peer).start();
            
            System.out.println("Conectado a " + host + ":" + port);
            
            // Pede a blockchain
            peer.sendMessage(new Message(Message.Type.REQUEST_CHAIN, null));
            
        } catch (Exception e) {
            System.out.println("Nao foi possivel conectar a " + host + ":" + port);
        }
    }

    // Enviar dados para TODOS os nós (Broadcasting)
    public void broadcast(Message msg) {
        for (PeerHandler peer : peers) {
            peer.sendMessage(msg);
        }
    }

    // Getter para a blockchain ser acessível pelo Handler
    public BlockChain getBlockchain() {
        return blockchain;
    }
}