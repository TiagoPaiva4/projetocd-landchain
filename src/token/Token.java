package token;

import java.io.Serializable;

public class Token implements Serializable {

    private final String assetID;   // ID do imóvel ou ativo
    private final int amount;       // quantidade de tokens emitidos

    public Token(String assetID, int amount) {
        this.assetID = assetID;
        this.amount = amount;
    }

    public String getAssetID() {
        return assetID;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Token{" +
                "assetID='" + assetID + '\'' +
                ", amount=" + amount +
                '}';
    }
}
