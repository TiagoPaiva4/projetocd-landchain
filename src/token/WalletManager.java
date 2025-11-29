package token;

import java.util.HashMap;
import java.util.Map;

public class WalletManager {

    private final Map<String, Wallet> wallets = new HashMap<>();

    public Wallet getOrCreateWallet(String owner) {
        return wallets.computeIfAbsent(owner, Wallet::new);
    }

    public Wallet getWallet(String owner) {
        return wallets.get(owner);
    }

    public void transfer(String from, String to, String assetID, int amount) {
        Wallet w1 = getOrCreateWallet(from);
        Wallet w2 = getOrCreateWallet(to);

        w1.removeTokens(assetID, amount);
        w2.addTokens(assetID, amount);

        System.out.println("✔ Transferência de " + amount +
                " tokens de '" + from + "' para '" + to + "' (" + assetID + ")");
    }

    public void printWallets() {
        System.out.println("\n===== CARTEIRAS =====");
        wallets.values().forEach(System.out::println);
    }
}
