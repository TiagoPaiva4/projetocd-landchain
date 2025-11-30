package core;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import utils.SecurityUtils;

public class Transaction implements Serializable {

    public enum Type {
        ORACLE_REGISTER,
        MINT_TOKEN,
        TRANSFER_TOKEN,
        RENT_DISTRIBUTION,
        
        // NOVOS TIPOS PARA O ESCROW
        CREATE_SALE, // Vendedor coloca à venda
        BUY_SALE,     // Comprador aceita a venda
        CONFIRM_SALE    // Vendedor liberta para o comprador (NOVO)
    }

    private Type type;
    private String sender;
    private String receiver;
    private String assetID;
    private int amount;
    
    // NOVO CAMPO: Preço (em Euros, apenas informativo nesta versão)
    private double price; 
    
    // NOVO CAMPO: Referência (para o Comprador dizer qual venda está a comprar)
    private String transactionRef; 

    private String data;
    private long timestamp;
    private byte[] signature;

    // Construtor Atualizado (Compatível com o antigo + novos campos)
    public Transaction(Type type, String sender, String receiver, String assetID, int amount, double price, String data) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.assetID = assetID;
        this.amount = amount;
        this.price = price;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Setter para referência (usado na compra)
    public void setTransactionRef(String ref) {
        this.transactionRef = ref;
    }
    
    public String getTransactionRef() { return transactionRef; }
    public double getPrice() { return price; }

    // Assinar (Incluindo os novos campos na assinatura para segurança)
    public void sign(PrivateKey key) throws Exception {
        String content = type.toString() + sender + receiver + assetID + amount + price + timestamp + (transactionRef != null ? transactionRef : "");
        this.signature = SecurityUtils.sign(content.getBytes(), key);
    }

    public boolean isValid() {
        if (type == Type.MINT_TOKEN && sender.equals("SYSTEM")) return true;
        if (signature == null) return false;
        try {
            String content = type.toString() + sender + receiver + assetID + amount + price + timestamp + (transactionRef != null ? transactionRef : "");
            byte[] keyBytes = Base64.getDecoder().decode(sender);
            PublicKey pubKey = SecurityUtils.getPublicKey(keyBytes);
            return SecurityUtils.verifySign(content.getBytes(), signature, pubKey);
        } catch (Exception e) { return false; }
    }

    // Getters antigos mantêm-se...
    public Type getType() { return type; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getAssetID() { return assetID; }
    public int getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
    
    // Método auxiliar para obter o ID único da transação (Hash da assinatura serve como ID)
    public String getTransactionID() {
        // CORREÇÃO: Se a assinatura for nula (ex: MINT do sistema), geramos um ID alternativo
        if (signature == null) {
            return "SYSTEM_TX_" + type + "_" + timestamp + "_" + assetID;
        }
        return Base64.getEncoder().encodeToString(signature);
    }
    
    @Override
    public String toString() {
        return type + " | " + amount + " tokens | " + price + " EUR";
    }
}