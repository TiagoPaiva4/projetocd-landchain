package token;

import utils.SecurityUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Wallet implements Serializable {

    private static final long serialVersionUID = 1L; // Importante para compatibilidade

    private final String name;
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    
    // Map<AssetID, Quantidade>
    private final Map<String, Integer> balances = new HashMap<>();

    public Wallet(String name) throws Exception {
        this.name = name;
        KeyPair pair = SecurityUtils.generateRSAKeyPair(2048);
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
    }

    // ==========================================
    //  NOVOS MÉTODOS: PERSISTÊNCIA SEGURA
    // ==========================================

    /**
     * Guarda a carteira num ficheiro encriptado com password
     */
    public void save(String filename, String password) throws Exception {
        // 1. Converter o objeto Wallet em Bytes (Serialização)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        byte[] walletBytes = bos.toByteArray();

        // 2. Encriptar os bytes com a password
        byte[] encryptedData = SecurityUtils.encrypt(walletBytes, password);

        // 3. Escrever no disco
        if(!filename.endsWith(".wallet")) filename += ".wallet";
        Files.write(Paths.get(filename), encryptedData);
        
        System.out.println("Carteira guardada em: " + filename);
    }

    /**
     * Carrega uma carteira do disco desencriptando com a password
     */
    public static Wallet load(String filename, String password) throws Exception {
        if(!filename.endsWith(".wallet")) filename += ".wallet";

        // 1. Ler os bytes encriptados do disco
        byte[] encryptedData = Files.readAllBytes(Paths.get(filename));

        // 2. Desencriptar usando a password
        byte[] walletBytes = SecurityUtils.decrypt(encryptedData, password);

        // 3. Converter bytes de volta em Objeto (Deserialização)
        ByteArrayInputStream bis = new ByteArrayInputStream(walletBytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        
        return (Wallet) ois.readObject();
    }

    // ==========================================
    //  MÉTODOS EXISTENTES
    // ==========================================

    public String getName() { return name; }
    public PublicKey getPublicKey() { return publicKey; }
    public PrivateKey getPrivateKey() { return privateKey; } // Necessário para assinar

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
    
    public byte[] sign(byte[] data) throws Exception {
        return SecurityUtils.sign(data, this.privateKey);
    }

    @Override
    public String toString() {
        return "Wallet (" + name + ") | Addr: " + getAddress().substring(0, 15) + "...";
    }
}