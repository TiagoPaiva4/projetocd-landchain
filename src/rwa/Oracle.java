/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rwa;

/**
 *
 * @author Tiago Paiva
 */
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import core.BlockChain;
import utils.SecurityUtils;

public class Oracle {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private BlockChain blockchain;

    public Oracle(BlockChain blockchain) throws Exception {
        this.blockchain = blockchain;

        // gerar par de chaves
        KeyPair keys = SecurityUtils.generateRSAKeyPair(2048);
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();
    }

    // REGISTA UM RWA
    public void registarRWA(String assetID, String assetType, String filePath) throws Exception {

        // 1) ler documento real
        byte[] documento = Files.readAllBytes(Paths.get(filePath));

        // 2) hash do documento
        byte[] hash = SecurityUtils.calculateHash(documento, "SHA3-256");

        // 3) assinatura da oracle
        byte[] assinatura = SecurityUtils.sign(hash, privateKey);

        // 4) criar o objeto RWARecord
        RWARecord record = new RWARecord(
                assetID,
                assetType,
                hash,
                assinatura,
                publicKey.getEncoded()
        );

        // 5) inserir na blockchain
        blockchain.add(new Object[]{record});
    }
}