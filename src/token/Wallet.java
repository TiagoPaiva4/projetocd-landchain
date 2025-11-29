package token;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Wallet implements Serializable {

    private final String owner; // nome do detentor
    private final Map<String, Integer> balances = new HashMap<>();

    public Wallet(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public int getBalance(String assetID) {
        return balances.getOrDefault(assetID, 0);
    }

    public void addTokens(String assetID, int amount) {
        balances.put(assetID, getBalance(assetID) + amount);
    }

    public void removeTokens(String assetID, int amount) {
        int current = getBalance(assetID);
        if (current < amount) throw new RuntimeException("Saldo insuficiente");
        balances.put(assetID, current - amount);
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "owner='" + owner + '\'' +
                ", balances=" + balances +
                '}';
    }
}
