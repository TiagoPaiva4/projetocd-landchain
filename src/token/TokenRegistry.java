package token;

import core.Block;
import core.BlockChain;
import core.Transaction;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TokenRegistry implements Serializable {
    
    // Ledger: Map<EndereçoWallet, Map<AssetID, Quantidade>>
    private final Map<String, Map<String, Integer>> ledger = new HashMap<>();

    public TokenRegistry() {
    }

    // Este método recalcula tudo do zero lendo a blockchain
    // É a forma mais segura em Blockchain 1.0 para evitar erros de sincronia
    public void sync(BlockChain bc) {
        ledger.clear(); // Limpa a cache
        for (Block b : bc.getBlocks()) {
            processBlock(b);
        }
    }

    private void processBlock(Block block) {
        // Percorre os dados do bloco à procura de Transações
        List<Object> data = block.getData().getElements();
        for(Object obj : data) {
            if (obj instanceof Transaction) {
                processTransaction((Transaction) obj);
            }
        }
    }
    
    private void processTransaction(Transaction tx) {
        if (!tx.isValid()) return; // Ignora se a assinatura for falsa

        String sender = tx.getSender();
        String receiver = tx.getReceiver();
        String asset = tx.getAssetID();
        int amount = tx.getAmount();

        // 1. Se NÃO for MINT (SYSTEM), retirar do remetente
        if (!sender.equals("SYSTEM")) { 
            int balance = getBalance(sender, asset);
            if (balance >= amount) {
                updateBalance(sender, asset, balance - amount);
            } else {
                return; // Saldo insuficiente, ignora tx
            }
        }

        // 2. Adicionar ao destinatário
        int receiverBalance = getBalance(receiver, asset);
        updateBalance(receiver, asset, receiverBalance + amount);
    }
    
    public int getBalance(String walletAddr, String assetID) {
        return ledger.getOrDefault(walletAddr, new HashMap<>())
                     .getOrDefault(assetID, 0);
    }
    
    private void updateBalance(String walletAddr, String assetID, int newAmount) {
        ledger.computeIfAbsent(walletAddr, k -> new HashMap<>()).put(assetID, newAmount);
    }
    
    public void printBalances() {
        System.out.println("=== ESTADO GERAL DO TOKEN REGISTRY ===");
        if (ledger.isEmpty()) {
            System.out.println("(Vazio)");
        }
        
        for (String wallet : ledger.keySet()) {
            
            String idVisual = "..." + wallet.substring(wallet.length() - 15);
            
            System.out.println("Carteira [" + idVisual + "]");
            
            Map<String, Integer> assets = ledger.get(wallet);
            for (String asset : assets.keySet()) {
                System.out.println("   -> Ativo: " + asset + " | Qtd: " + assets.get(asset));
            }
            System.out.println("-----------------------------------");
        }
    }
    

    public void printWalletBalance(String walletAddress) {
        System.out.println("=== SALDO DA MINHA CARTEIRA ===");
        
        if (!ledger.containsKey(walletAddress)) {
            System.out.println(" (Sem registos nesta blockchain) ");
            return;
        }

        Map<String, Integer> assets = ledger.get(walletAddress);
        
        if (assets.isEmpty()) {
            System.out.println(" (Carteira Vazia) ");
            return;
        }

        for (String assetID : assets.keySet()) {
            int qtd = assets.get(assetID);
            System.out.println(" > Ativo: " + assetID + " | Quantidade: " + qtd);
        }
        System.out.println("===============================");
    }

}