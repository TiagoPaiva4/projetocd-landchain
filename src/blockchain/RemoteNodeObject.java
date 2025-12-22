package blockchain;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.RMI;

/**
 * Nó P2P Genérico - Apenas transporta dados e mantém consenso.
 * Não contém regras de negócio (RWA).
 */
public class RemoteNodeObject extends UnicastRemoteObject implements RemoteNodeInterface {

    public static String REMOTE_OBJECT_NAME = "remoteNode";

    String address;
    Set<RemoteNodeInterface> network;
    Set<String> transactions;
    Nodelistener listener;
    MinerDistibuted miner = new MinerDistibuted();
    BlockChain blockchain;
    Block currentBlock;

    // Construtor Original (Sem nome da carteira)
    public RemoteNodeObject(int port, Nodelistener listener) throws RemoteException {
        super(port);
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.address = RMI.getRemoteName(host, port, REMOTE_OBJECT_NAME);
            this.network = new CopyOnWriteArraySet<>();
            this.transactions = new CopyOnWriteArraySet<>();
            
            // Carrega a blockchain padrão da pasta "blockchain/" ou "data/port/"
            // Nota: O método load pode variar conforme a tua implementação do BlockChain.java
            // Aqui assumimos o padrão de carregar da pasta local baseada na porta
            this.blockchain = BlockChain.load("data/" + port + "/", "blockchain.bch");
            
            this.listener = listener;
            if (listener != null) {
                listener.onStart("Object " + address + " listening");
                listener.onBlockchain(blockchain);
            } else {
                System.err.println("Object " + address + " listening");
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemoteNodeObject.class.getName()).log(Level.SEVERE, null, ex);
            if (listener != null) {
                listener.onException(ex, "Start remote Object");
            }
        }
    }

    @Override
    public String getAdress() throws RemoteException {
        return address;
    }

    @Override
    public void addNode(RemoteNodeInterface node) throws RemoteException {
        if (network.contains(node)) {
            return;
        }
        network.add(node);
        this.transactions.addAll(node.getTransactions());
        node.addNode(this);
        synchronizeBlockchain(node);
        for (RemoteNodeInterface iremoteP2P : network) {
            iremoteP2P.addNode(node);
        }
        if (listener != null) {
            listener.onConect(node.getAdress());
        }
    }

    @Override
    public List<RemoteNodeInterface> getNetwork() throws RemoteException {
        return new ArrayList<>(network);
    }

    @Override
    public void addTransaction(String data) throws RemoteException {
        if (this.transactions.contains(data)) {
            return;
        }
        this.transactions.add(data);
        for (RemoteNodeInterface node : network) {
            new Thread(() -> {
                try {
                    node.addTransaction(data);
                } catch (Exception e) {
                    network.remove(node);
                }
            }).start();
        }
        if (listener != null) {
            listener.onConect("");
            listener.onTransaction(data);
        } 
    }

    @Override
    public List<String> getTransactions() throws RemoteException {
        return new ArrayList<>(transactions);
    }

    //::::::::::: M I N E R  :::::::::::
    public void startMiner(int dificulty) throws RemoteException {        
        startMiner(address, dificulty);
    }

    @Override
    public void startMiner(String message, int dificulty) throws RemoteException {
        if (miner.isMining() || transactions.isEmpty()) {
            return; 
        }
        currentBlock = blockchain.createNewBlock(new ArrayList<>(transactions));
        miner.isWorking.set(true);
        for (RemoteNodeInterface node : network) {
            new Thread(() -> {
                try {
                    node.startMiner(message, dificulty);
                } catch (Exception e) {
                    network.remove(node);
                }
            }).start();
        }        
        new Thread(() -> {
            miner.mine(currentBlock.getHeaderDataBase64(), dificulty);
        }).start();
    }

    @Override
    public void stopMining(int nonce) throws RemoteException {
        if (!miner.isMining()) {
            return; 
        }
        miner.stopMining(nonce);
        for (RemoteNodeInterface node : network) {
            new Thread(() -> {
                try {
                    node.stopMining(nonce);
                } catch (Exception e) {
                    network.remove(node);
                }
            }).start();
        }
    }
    
    public void onNonceFound(int nonce) throws Exception {
        stopMining(nonce);
        currentBlock.setNonce(nonce);
        addBlock(currentBlock);
    }

    @Override
    public MinerDistibuted getMiner() throws RemoteException {
        return miner;
    }

    @Override
    public void addBlock(Block block) throws RemoteException {
        try {
            if (this.blockchain.getBlocks().contains(block)) {
                return;
            }
            blockchain.add(block);
            
            // AQUI ESTAVA O RWA - REMOVIDO
            // A notificação onBlockchain avisa a GUI, que atualizará o RWA Service externamente
            
            for (RemoteNodeInterface node : network) {
                node.addBlock(block);
            }
            List<String> blockTrasactions = block.getData().getElements();
            for (String blockt : blockTrasactions) {
                if (transactions.contains(blockt)) {
                    transactions.remove(blockt);
                }
            }
            if (listener != null) {
                listener.onTransaction("");
                listener.onBlockchain(blockchain);
            }
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    @Override
    public int getBlockchainSize() throws RemoteException {
        return blockchain.getBlocks().size();
    }

    @Override
    public BlockChain getBlockchain() throws RemoteException {
        return blockchain;
    }

    @Override
    public void setBlockchain(BlockChain b) throws RemoteException {
        try {
            this.blockchain.setBlocks(b.getBlocks());
            
            if (listener != null) {
                listener.onBlockchain(blockchain);
            }
        } catch (Exception ex) {
            throw new RemoteException(ex.getMessage());
        }
    }

    public Block getlastBlock() throws RemoteException {
        return blockchain.getLastBlock();
    }

    @Override
    public void synchronizeBlockchain(RemoteNodeInterface node) throws RemoteException {
        if (node.getBlockchainSize() > getBlockchainSize()) {
            setBlockchain(node.getBlockchain());
            if (listener != null) listener.onBlockchain(blockchain);
        }
        else if (node.getBlockchainSize() < getBlockchainSize()) {
            node.setBlockchain(blockchain);
        } 
        else {
            if (getlastBlock().getTimestamp() > node.getlastBlock().getTimestamp()) {
                setBlockchain(node.getBlockchain());
                if (listener != null) listener.onBlockchain(blockchain);
            }
            if (getlastBlock().getTimestamp() < node.getlastBlock().getTimestamp()) {
                node.setBlockchain(blockchain);
            }
        }
    }
}