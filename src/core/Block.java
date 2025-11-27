//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2025   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////

package core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Created on 08/10/2025, 14:45:02
 *
 * @author manso - computer
 */
public class Block implements Serializable {

    //::::::: information of the block :::::::::::::::
    private final int ID;
    private final byte[] previousHash;
    private final byte[] merkleRoot;
    private final MerkleTree data; //Merkle Tree
    private final long timestamp; //unix era
    private final int dificulty; //number of zeros 
    //:::::::: security Protocol - POW ::::::::::::::::
    private int nonce;
    private byte[] currentHash;

    /**
     * constructor
     *
     * @param ID ID of the block
     * @param previousHash Hash of the previous block
     * @param dificulty Number of zeros in the POW
     * @param data List of elements to store in block
     */
    public Block(int ID, byte[] previousHash, int dificulty, List data) {
        this.ID = ID;
        this.previousHash = previousHash;
        this.dificulty = dificulty;
        this.timestamp = System.currentTimeMillis();
        //build a merkleTree
        this.data = new MerkleTree(data);
        this.merkleRoot = this.data.getRoot();
    }

    /**
     * data to be mined
     *
     * @return byte array with fields to be mined
     */
    public byte[] getHeaderData() {
        byte[] bytes = Utils.toBytes(ID);
        bytes = Utils.concatenate(bytes, Utils.toBytes(timestamp));
        bytes = Utils.concatenate(bytes, Utils.toBytes(previousHash));
        bytes = Utils.concatenate(bytes, Utils.toBytes(merkleRoot));
        return Utils.concatenate(bytes, Utils.toBytes(dificulty));
    }

    /**
     * Mine Current Block
     *
     * @throws Exception
     */
    public void mine() throws Exception {
        String dataTxt = Base64.getEncoder().encodeToString(getHeaderData());
        int pow = Miner.getNonce(dataTxt, this.dificulty);
        setNonce(pow);
    }

    /**
     * update nonce and the hash
     *
     * @param nonce nonce
     * @throws Exception
     */
    public void setNonce(int nonce) throws Exception {
        this.nonce = nonce;
        String hash = Base64.getEncoder().encodeToString(getHeaderData());
        this.currentHash = SecurityUtils.calculateHash(
                (hash + nonce).getBytes(), Miner.hashAlgorithm);
    }

    public String toStringHeader() {
        StringBuilder txt = new StringBuilder();
        txt.append("ID ").append(ID);
        txt.append("\nHash ").append(Base64.getEncoder().encodeToString(currentHash));
        txt.append("\ntimestamp ").append(new Date(timestamp));
        txt.append("\npreviousHash ").append(Base64.getEncoder().encodeToString(previousHash));
        txt.append("\nmerkleRoot ").append(Base64.getEncoder().encodeToString(merkleRoot));
        txt.append("\ndificulty ").append(dificulty);
        txt.append("\nnonce ").append(nonce);
        
        
        return txt.toString();
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(toStringHeader());
        txt.append("\n-------- data--------\n");
        for (Object element : data.getElements()) {
            txt.append(element).append("\n");
        }
        txt.append("-------- data--------");
        return txt.toString();
    }

    /**
     * validate the block - Contains zeros - Hash calculated match with
     * currentHash
     *
     * @return true if valid
     */
    public boolean isValid() {
        try {
            //:::::::::: zeros no inicio :::::::::::::::::::::::::
            String txtHash = Base64.getEncoder().encodeToString(currentHash);
            String hashZeros = txtHash.substring(0, this.dificulty);
            String allZeros = String.format("%0" + dificulty + "d", 0);
            if (!hashZeros.equals(allZeros)) {
                return false;
            }

            //:::::::::: o hash é valido ::::::::::::::::::::::::::::
            txtHash = Base64.getEncoder().encodeToString(getHeaderData()) + nonce;
            byte[] myHash = SecurityUtils.calculateHash(
                    txtHash.getBytes(), Miner.hashAlgorithm);
            return Arrays.equals(myHash, currentHash);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * save the block in the file path/ID.block
     *
     * @param path path of the block
     * @throws IOException
     */
    public void save(String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(path + ID + ".blk"))) {
            out.writeObject(this);
        }
    }

    /**
     * Loads a block from the file
     *
     * @param path path of blocks
     * @param ID id of the block
     * @return block readed
     */
    public static Block load(String path, int ID) {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(path + ID + ".blk"))) {
            return (Block) in.readObject();
        } catch (Exception ex) {
            return null;
        }
    }

    public int getID() {
        return ID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getPreviousHash() {
        return previousHash;
    }

    public byte[] getMerkleRoot() {
        return merkleRoot;
    }

    public MerkleTree getData() {
        return data;
    }

    public int getDificulty() {
        return dificulty;
    }

    public int getNonce() {
        return nonce;
    }

    public byte[] getCurrentHash() {
        return currentHash;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081445L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
