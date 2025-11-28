/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 *
 * @author Tiago Paiva
 */
import java.io.Serializable;

public class Transaction implements Serializable {

    public enum Type {
        ORACLE_REGISTER,
        MINT_TOKEN,
        TRANSFER_TOKEN,
        RENT_DISTRIBUTION,
        CONVERSION_FUNGIBLE_TO_NFT
    }

    private Type type;
    private String data;   // JSON ou string simples
    private long timestamp;

    public Transaction(Type type, String data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String toString() {
        return type + " | " + data;
    }
}