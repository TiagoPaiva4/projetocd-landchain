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
        for (Object obj : data) {
            if (obj instanceof Transaction) {
                processTransaction((Transaction) obj);
            }
        }
    }

    private void processTransaction(Transaction tx) {
        if (!tx.isValid()) {
            return;
        }

        String sender = tx.getSender();
        String receiver = tx.getReceiver();
        String asset = tx.getAssetID();
        int amount = tx.getAmount();

        // 1. Venda: Vendedor -> Escrow
        if (tx.getType() == Transaction.Type.CREATE_SALE) {
            receiver = "ESCROW_POOL";

            // Debitar do vendedor e creditar no Pool
            int balSender = getBalance(sender, asset);
            if (balSender >= amount) {
                updateBalance(sender, asset, balSender - amount);
                int balPool = getBalance("ESCROW_POOL", asset);
                updateBalance("ESCROW_POOL", asset, balPool + amount);
            }
            return; // Fim desta tx
        } // 2. Compra: Agora NÃO move tokens, apenas avisa (na blockchain) que há interessado
        else if (tx.getType() == Transaction.Type.BUY_SALE) {
            // Não fazemos nada ao saldo aqui! 
            // O EscrowManager é que vai ler isto e mudar o estado para "Pendente"
            return;
        } // 3. Confirmação (NOVO): Escrow -> Comprador
        else if (tx.getType() == Transaction.Type.CONFIRM_SALE) {
            // Sender é o Vendedor (que assinou), mas tokens saem do Pool
            String realSender = "ESCROW_POOL";

            int balPool = getBalance(realSender, asset);
            if (balPool >= amount) {
                updateBalance(realSender, asset, balPool - amount);

                int balBuyer = getBalance(receiver, asset);
                updateBalance(receiver, asset, balBuyer + amount);
            }
            return;
        }

        // --- Lógica Normal de Transferência P2P ---
        if (!sender.equals("SYSTEM")) {
            int balance = getBalance(sender, asset);
            if (balance >= amount) {
                updateBalance(sender, asset, balance - amount);
                int balRec = getBalance(receiver, asset);
                updateBalance(receiver, asset, balRec + amount);
            }
        } else { // SYSTEM MINT
            int balRec = getBalance(receiver, asset);
            updateBalance(receiver, asset, balRec + amount);
        }
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
