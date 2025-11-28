/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rwa;

/**
 *
 * @author Tiago Paiva
 */
import java.io.Serializable;
import java.util.Base64;

public class RWARecord implements Serializable {

    private final String assetID;
    private final String assetType;
    private final byte[] hashDocumento;
    private final byte[] assinatura;
    private final byte[] publicKeyOracle;
    private final long timestamp;

    public RWARecord(String assetID, String assetType,
                     byte[] hashDocumento, byte[] assinatura,
                     byte[] publicKeyOracle) {

        this.assetID = assetID;
        this.assetType = assetType;
        this.hashDocumento = hashDocumento;
        this.assinatura = assinatura;
        this.publicKeyOracle = publicKeyOracle;
        this.timestamp = System.currentTimeMillis();
    }

    // -------------------------
    //   GETTERS NECESSÁRIOS
    // -------------------------

    public String getAssetID() {
        return assetID;
    }

    public String getAssetType() {
        return assetType;
    }

    public byte[] getHashDocumento() {
        return hashDocumento;
    }

    public byte[] getAssinatura() {
        return assinatura;
    }

    public byte[] getPublicKeyOracle() {
        return publicKeyOracle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "RWARecord {" +
                "\n assetID=" + assetID +
                "\n assetType=" + assetType +
                "\n hash=" + Base64.getEncoder().encodeToString(hashDocumento) +
                "\n assinatura=" + Base64.getEncoder().encodeToString(assinatura) +
                "\n }";
    }
}
