package rwa;

import core.BlockChain;
import core.Transaction; // <--- NOVO
import events.EventManager;
import events.NewRwaRegisteredEvent;
import events.RentDistributionEvent;
import token.Wallet;      // <--- NOVO
import utils.SecurityUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList; // <--- NOVO
import java.util.List;      // <--- NOVO

/**
 * @author Tiago Paiva
 */
public class Oracle {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final BlockChain blockchain;
    private final EventManager events;

    public Oracle(BlockChain blockchain, EventManager events) throws Exception {
        this.blockchain = blockchain;
        this.events = events;

        // gerar par de chaves da Oracle
        KeyPair keys = SecurityUtils.generateRSAKeyPair(2048);
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();
    }

    // MUDANÇA: Agora pede a Wallet do criador
    public void registarRWA(String assetID, String assetType, String filePath, Wallet creatorWallet) throws Exception {

        // 1) Ler ficheiro real e criar Hash
        byte[] documento = Files.readAllBytes(Paths.get(filePath));
        byte[] hash = SecurityUtils.calculateHash(documento, "SHA3-256");
        byte[] assinatura = SecurityUtils.sign(hash, privateKey);

        // 2) Criar registo RWA (Metadados)
        RWARecord record = new RWARecord(
                assetID,
                assetType,
                hash,
                assinatura,
                publicKey.getEncoded()
        );
        
        // 3) CRIAR TRANSAÇÃO DE 1000 TOKENS (MINT)
        // Sender: "SYSTEM", Receiver: Endereço do Criador
        Transaction mintTx = new Transaction(
                Transaction.Type.MINT_TOKEN,
                "SYSTEM",
                creatorWallet.getAddress(),
                assetID,
                1000,
                "Initial Supply"
        );

        // 4) Juntar tudo numa lista (Registo + Tokens no mesmo bloco)
        List<Object> blockData = new ArrayList<>();
        blockData.add(record);
        blockData.add(mintTx);

        // 5) Inserir bloco na blockchain
        blockchain.add(blockData);

        // 6) Disparar evento
        events.publish(new NewRwaRegisteredEvent(record));
        
        System.out.println(">>> Oracle: RWA registado e 1000 tokens enviados para " + creatorWallet.getName());
    }

    public void registarRenda(String assetID, double amount) throws Exception {
        RentDistributionEvent e = new RentDistributionEvent(assetID, amount);
        try {
            this.events.publish(e);
            // Nota: Idealmente, isto também criaria uma transação na blockchain,
            // mas para o evento de distribuição, o RentListener trata disso.
        } catch (Exception ex) {
            System.err.println("Erro ao publicar RentDistributionEvent: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }
}