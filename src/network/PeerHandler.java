package network;

import core.Block;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class PeerHandler implements Runnable {
    
    private Socket socket;
    private P2PNode node;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private boolean running = true;

    // MUDANÇA AQUI: Adicionar 'throws IOException' e iniciar streams logo
    public PeerHandler(Socket socket, P2PNode node) throws IOException {
        this.socket = socket;
        this.node = node;
        // Iniciar os streams IMEDIATAMENTE para evitar NullPointer
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.out.flush(); // Importante para desbloquear o InputStream do outro lado
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            while (running) {
                // Fica à espera de receber um objeto Message
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            }
        } catch (Exception e) {
            // System.out.println("Peer desconectado.");
            close();
        }
    }

    public void sendMessage(Message msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
                out.reset(); // <--- ADICIONA ISTO! É CRÍTICO!
            }
        } catch (Exception e) {
            close();
        }
    }

    private void close() {
        try {
            running = false;
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(Message msg) {
        try {
            switch (msg.type) {
                case NEW_TRANSACTION:
                    System.out.println("Nova transacao recebida.");
                    break;

                case NEW_BLOCK:
                    Block newBlock = (Block) msg.data;
                    System.out.println("Novo bloco recebido ID: " + newBlock.getID());
                    try {
                        node.getBlockchain().add(newBlock);
                        System.out.println("Bloco sincronizado!");
                    } catch (Exception e) {
                        // Se falhar e o ID for maior, pede a chain toda
                        if(newBlock.getID() > node.getBlockchain().getLastBlock().getID()){
                            sendMessage(new Message(Message.Type.REQUEST_CHAIN, null));
                        }
                    }
                    break;

                case REQUEST_CHAIN:
                    System.out.println("Peer pediu a blockchain. Enviando...");
                    List<Block> chain = node.getBlockchain().getBlocks(); 
                    sendMessage(new Message(Message.Type.SEND_CHAIN, chain));
                    break;

                case SEND_CHAIN:
                    List<Block> receivedChain = (List<Block>) msg.data;
                    List<Block> myChain = node.getBlockchain().getBlocks();
                    if (receivedChain.size() > myChain.size()) {
                        System.out.println("Recebida cadeia maior. Atualizando...");
                        // Atenção: validação simplificada
                        myChain.clear();
                        myChain.addAll(receivedChain);
                        node.getBlockchain().save(core.BlockChain.FILE_PATH + "blockchain.bch");
                        System.out.println("Blockchain atualizada.");
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Erro processar msg: " + e.getMessage());
        }
    }
}