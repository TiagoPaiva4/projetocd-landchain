package token;

import utils.SecurityUtils;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Wallet implements Serializable {

    private final String name; // Apenas para display (ex: "Carteira do Joao")
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    
    // Map<AssetID, Quantidade>
    private final Map<String, Integer> balances = new HashMap<>();

    public Wallet(String name) throws Exception {
        this.name = name;
        // Gera chaves RSA de 2048 bits
        KeyPair pair = SecurityUtils.generateRSAKeyPair(2048);
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
    }

    public String getName() {
        return name;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    // Identificador único da carteira (Hash da chave pública)
    public String getAddress() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public int getBalance(String assetID) {
        return balances.getOrDefault(assetID, 0);
    }

    public void addTokens(String assetID, int amount) {
        balances.put(assetID, getBalance(assetID) + amount);
    }

    public void removeTokens(String assetID, int amount) throws Exception {
        int current = getBalance(assetID);
        if (current < amount) {
            throw new Exception("Saldo insuficiente na carteira de " + name);
        }
        balances.put(assetID, current - amount);
    }
    
    // ADICIONAR ESTE MÉTODO:
    public PrivateKey getPrivateKey() {
        return privateKey;
    }
    
    // Assina dados com a chave privada desta carteira
    public byte[] sign(byte[] data) throws Exception {
        return SecurityUtils.sign(data, this.privateKey);
    }

    @Override
    public String toString() {
        return "Wallet (" + name + ") | Addr: " + getAddress().substring(0, 15) + "...";
    }
}