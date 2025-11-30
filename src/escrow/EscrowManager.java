package escrow;

import core.Block;
import core.BlockChain;
import core.Transaction;
import java.util.ArrayList;
import java.util.List;

public class EscrowManager {

    private final BlockChain blockchain;

    public EscrowManager(BlockChain blockchain) {
        this.blockchain = blockchain;
    }

    // Vendas livres (ninguém comprou ainda)
    public List<Transaction> getOpenOrders() {
        List<Transaction> creates = new ArrayList<>();
        List<String> reservedIds = new ArrayList<>();

        for (Block b : blockchain.getBlocks()) {
            for (Object obj : b.getData().getElements()) {
                if (obj instanceof Transaction) {
                    Transaction tx = (Transaction) obj;
                    if (tx.getType() == Transaction.Type.CREATE_SALE) {
                        creates.add(tx);
                    } else if (tx.getType() == Transaction.Type.BUY_SALE) {
                        reservedIds.add(tx.getTransactionRef());
                    }
                }
            }
        }
        creates.removeIf(t -> reservedIds.contains(t.getTransactionID()));
        return creates;
    }

    // Vendas pendentes (Vendedor precisa aceitar)
    // Retorna a transação de COMPRA (BUY_SALE) que contem a referência da venda
    public List<Transaction> getPendingApprovals(String sellerAddress) {
        List<Transaction> mySales = new ArrayList<>(); // Vendas criadas por mim
        List<Transaction> buyRequests = new ArrayList<>(); // Pedidos de compra
        List<String> confirmedRefs = new ArrayList<>(); // Já finalizadas

        // 1. Varrer tudo
        for (Block b : blockchain.getBlocks()) {
            for (Object obj : b.getData().getElements()) {
                if (obj instanceof Transaction) {
                    Transaction tx = (Transaction) obj;
                    
                    if (tx.getType() == Transaction.Type.CREATE_SALE && tx.getSender().equals(sellerAddress)) {
                        mySales.add(tx);
                    } 
                    else if (tx.getType() == Transaction.Type.BUY_SALE) {
                        buyRequests.add(tx);
                    }
                    else if (tx.getType() == Transaction.Type.CONFIRM_SALE) {
                        confirmedRefs.add(tx.getTransactionRef()); // Refere-se à venda original
                    }
                }
            }
        }

        // 2. Cruzar dados: Quero BUY_REQUESTS que apontam para as MINHAS VENDAS e ainda não confirmadas
        List<Transaction> actionableRequests = new ArrayList<>();
        
        for (Transaction buy : buyRequests) {
            // Verificar se este buy aponta para uma venda minha
            boolean isMySale = mySales.stream().anyMatch(sale -> sale.getTransactionID().equals(buy.getTransactionRef()));
            // Verificar se já não foi confirmada
            boolean isAlreadyDone = confirmedRefs.contains(buy.getTransactionRef());

            if (isMySale && !isAlreadyDone) {
                actionableRequests.add(buy);
            }
        }
        
        return actionableRequests;
    }
    
    // Auxiliar para ir buscar a Venda original através do ID (para sabermos qtd e asset)
    public Transaction getSaleByRef(String refID) {
        for (Block b : blockchain.getBlocks()) {
             for (Object obj : b.getData().getElements()) {
                 if(obj instanceof Transaction tx && tx.getTransactionID().equals(refID)) {
                     return tx;
                 }
             }
        }
        return null;
    }
}