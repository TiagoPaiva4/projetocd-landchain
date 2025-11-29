/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rwa;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import core.BlockChain;
import events.NewRwaRegisteredEvent;
import utils.SecurityUtils;

import core.BlockChain;
import events.EventManager;
import events.NewRwaRegisteredEvent;
import events.RentDistributionEvent;
import utils.SecurityUtils;

/**
 *
 * @author Tiago Paiva
 */
public class Oracle {

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private final BlockChain blockchain;
    private final EventManager events;

    // Novo construtor correto
    public Oracle(BlockChain blockchain, EventManager events) throws Exception {
        this.blockchain = blockchain;
        this.events = events;

        // gerar par de chaves
        KeyPair keys = SecurityUtils.generateRSAKeyPair(2048);
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();
    }

    // Registrar RWA no sistema
    public void registarRWA(String assetID, String assetType, String filePath) throws Exception {

        // 1) Ler ficheiro real
        byte[] documento = Files.readAllBytes(Paths.get(filePath));

        // 2) Hash do ficheiro
        byte[] hash = SecurityUtils.calculateHash(documento, "SHA3-256");

        // 3) Assinatura da Oracle
        byte[] assinatura = SecurityUtils.sign(hash, privateKey);

        // 4) Criar registo RWA
        RWARecord record = new RWARecord(
                assetID,
                assetType,
                hash,
                assinatura,
                publicKey.getEncoded()
        );

        // 5) Inserir bloco na blockchain
        blockchain.add(new Object[]{record});

        // 6) Disparar evento (TokenRegistryListener vai criar tokens)
        events.publish(new NewRwaRegisteredEvent(record));
    }

    public void registarRenda(String assetID, double amount) throws Exception {
    RentDistributionEvent e = new RentDistributionEvent(assetID, amount);
    try {
        this.events.publish(e);
    } catch (Exception ex) {
        System.err.println("Erro ao publicar RentDistributionEvent: " + ex.getMessage());
        ex.printStackTrace();
        throw ex;
    }
}


}
