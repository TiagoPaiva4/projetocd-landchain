package token;

import java.util.HashMap;
import java.util.Map;

public class WalletManager {

    private final Map<String, Wallet> wallets = new HashMap<>();

    public Wallet getOrCreateWallet(String owner) {
        // O lambda original (Wallet::new) não funciona com Exceptions.
        // Temos de fazer o try-catch manualmente:
        return wallets.computeIfAbsent(owner, name -> {
            try {
                return new Wallet(name);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao criar wallet: " + e.getMessage());
            }
        });
    }

    public Wallet getWallet(String owner) {
        return wallets.get(owner);
    }

    // Nota: Este método transfer é antigo e não usa a blockchain. 
    // Deverias usar o TokenRegistry para ver saldos reais.
    public void transfer(String from, String to, String assetID, int amount) throws Exception {
        Wallet w1 = getOrCreateWallet(from);
        Wallet w2 = getOrCreateWallet(to);

        // Lógica simples de memória (apenas para testes antigos)
        w1.removeTokens(assetID, amount);
        w2.addTokens(assetID, amount);

        System.out.println("Transferecia local de " + amount + " para " + to);
    }

    public void printWallets() {
        System.out.println("\n===== CARTEIRAS (Memória Local) =====");
        wallets.values().forEach(System.out::println);
    }
}