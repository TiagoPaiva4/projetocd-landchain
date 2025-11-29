package core;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import utils.SecurityUtils;

public class Transaction implements Serializable {

    public enum Type {
        ORACLE_REGISTER,            // Registo do RWA (Metadados)
        MINT_TOKEN,                 // Criação dos 1000 Tokens iniciais
        TRANSFER_TOKEN,             // Enviar tokens entre carteiras
        RENT_DISTRIBUTION,          // Pagar renda
        CONVERSION_FUNGIBLE_TO_NFT  // Opcional
    }

    private Type type;
    private String sender;      // Chave Pública do Remetente (Base64) ou "SYSTEM"
    private String receiver;    // Chave Pública do Destino (Base64)
    private String assetID;     // ID do RWA (ex: "LISBOA-001")
    private int amount;         // Quantidade de tokens
    
    private String data;        // Dados extra (opcional)
    private long timestamp;
    private byte[] signature;   // Assinatura de segurança

    // Construtor Completo
    public Transaction(Type type, String sender, String receiver, String assetID, int amount, String data) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.assetID = assetID;
        this.amount = amount;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    // Assinar a transação com a Chave Privada da Wallet
    public void sign(PrivateKey key) throws Exception {
        // Concatenamos os dados críticos para garantir que ninguém os alterou
        String content = type + sender + receiver + assetID + amount + timestamp;
        this.signature = SecurityUtils.sign(content.getBytes(), key);
    }

    // Verificar se a assinatura é válida
    public boolean isValid() {
        // Se for emissão do sistema (MINT), não precisa de assinatura de carteira
        if (type == Type.MINT_TOKEN && sender.equals("SYSTEM")) {
            return true;
        }
        
        if (signature == null) return false;

        try {
            String content = type + sender + receiver + assetID + amount + timestamp;
            // Converter a String sender de volta para PublicKey
            byte[] keyBytes = Base64.getDecoder().decode(sender);
            PublicKey pubKey = SecurityUtils.getPublicKey(keyBytes);
            
            return SecurityUtils.verifySign(content.getBytes(), signature, pubKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Getters
    public Type getType() { return type; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getAssetID() { return assetID; }
    public int getAmount() { return amount; }
    public String getData() { return data; }

    @Override
    public String toString() {
        return String.format("[%s] %s -> %s | %d Tokens (%s)", 
                type, 
                sender.equals("SYSTEM") ? "SYSTEM" : sender.substring(0, 10) + "...", 
                receiver.substring(0, 10) + "...", 
                amount, 
                assetID);
    }
}