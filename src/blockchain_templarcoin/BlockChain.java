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

package blockchain_templarcoin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import utils.FolderUtils;

/**
 * Created on 08/10/2025, 15:28:52
 *
 * @author manso - computer
 */
public class BlockChain implements Serializable {

    List<Block> blocks;

    /**
     * *
     * Creates a blockchain with an empty genesis block
     *
     * @throws Exception
     */
    private BlockChain(String fileName) throws Exception {
        this.fileName = fileName;
        //creates genesis block
        Block genesis = new Block(0, new byte[]{0, 0, 0, 0}, 3, Arrays.asList("Genesis Block"));
        genesis.mine();
        //creates list of blocks
        blocks = new CopyOnWriteArrayList<>();
        //add block to blockchain 
        blocks.add(genesis);
        //save new block
        new File(fileName).getParentFile().mkdirs();
        genesis.save(new File(fileName).getParent() + "/");
        save(fileName);
    }

    /**
     * creates a blockchain with a genesis block
     *
     * @param genesis
     */
    public BlockChain(String fileName, Block genesis) throws Exception {
        this(fileName);
        blocks = new CopyOnWriteArrayList<>();
        //add block to blockchain 
        blocks.add(genesis);
        //save new block
        new File(fileName).getParentFile().mkdirs();
        genesis.save(new File(fileName).getParent() + "/");
        save(fileName);
    }

    /**
     * Creates a new block not mined
     *
     * @param data data of block
     * @return block not mined
     */
    public Block createNewBlock(List data) {
        //last block of blockchain
        Block lastBlock = blocks.get(blocks.size() - 1);
        //build new block (NOT MINES
        return new Block(
                lastBlock.getID() + 1,
                lastBlock.getCurrentHash(),
                lastBlock.getDificulty(),
                data);
    }

    /**
     * Creates a new block not mined
     *
     * @param data data of block
     * @return block not mined
     */
    public void createNewBlock(Object[] data) throws Exception {
        createNewBlock(Arrays.asList(data));
    }

    /**
     * adds a new block to blockchain if the block is valid if block match to
     * the last block
     *
     * @param newBlock new block
     * @throws Exception
     */
    public void add(Block newBlock) throws Exception {
        //last block in blockchain
        Block last = getLastBlock();
        if (Arrays.equals(newBlock.getCurrentHash(),last.getCurrentHash())) {
            return;
        }
        System.out.println("LAST" + new String(last.getCurrentHash() ));
        System.out.println("NEW " + new String(newBlock.getCurrentHash() ));
        //block match to the last block
        if (!Arrays.equals(last.getCurrentHash(), newBlock.getPreviousHash())) {
            throw new Exception("block dont match - previous hash incorrect");
        }
        //block isvalid
        if (!newBlock.isValid()) {
            throw new Exception("Invalid block");
        }
        //ID of block is the position in the array
        if (blocks.size() != newBlock.getID()) {
            throw new Exception("Incorrect ID");
        }
        //::::::: SUCESS ::::::::::::
        //add block to blockchain 
        blocks.add(newBlock);
        //save new block
        newBlock.save(new File(fileName).getParent() + "/");
        //save blockchain
        save(fileName);
    }

    /**
     * gets the last block in blockchain
     *
     * @return
     */
    public Block getLastBlock() {
        return blocks.get(blocks.size() - 1);
    }

    public Block getBlockID(int id) {
        return blocks.get(id);
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    @Override
    public String toString() {
        return "BlockChain " + fileName + "\n" + blocks;
    }

    /**
     * saves the block chain in disk
     *
     * @param fileName filename
     * @param prefix
     * @throws java.lang.Exception
     */
    public final void save(String fileName) throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(fileName))) {
            out.writeObject(this);
        }
    }

    public static BlockChain load(String fileName) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(fileName))) {
            return (BlockChain) in.readObject();
        }
    }

    /**
     * load a blockchain in default fileName
     *
     * @param prefix prefix of blockchainfile
     * @return
     */
    public static BlockChain load(String path, String fileName) {
        try {
            return load(path + fileName);
        } catch (Exception ex) {
            try {
                BlockChain b = new BlockChain(path + fileName);
                return b;
            } catch (Exception ex1) {
                System.getLogger(BlockChain.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex1);
                return null;
            }
        }
    }

    public void restart() throws IOException {
        FolderUtils.cleanFolder(new File(fileName).getParent(), true);
    }

    public final String DEFAULT_FILE_PATH = "blockchain/"; // path of blackchain files
    public final String DEFAULT_FILE_NAME = "blockchain.bch"; // name of blockchainfile

    public String fileName = DEFAULT_FILE_PATH + DEFAULT_FILE_NAME; // name of blockchainfile

    public void setBlocks(List<Block> newBlocks) throws Exception {
        blocks = new CopyOnWriteArrayList<>(newBlocks);
        save(fileName);
        for (Block b : blocks) {
            b.save(new File(fileName).getParent() + "/");
        }
    }
    
    public List getTransactions(){
        List allTransactions = new ArrayList();
        for(Block b : blocks){
            allTransactions.addAll(b.getTransactions());
        }
        return allTransactions;
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081528L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::

    public static void main(String[] args) throws Exception {

        BlockChain blockchain = BlockChain.load("data/10015/", "blockchain.blc");

        List transactions = new ArrayList();
        transactions.add("abcd");

        Block newBlock = blockchain.createNewBlock(transactions);
        int nonce = MinerDistibuted.getNonce(newBlock.getHeaderDataBase64(), 3);
        newBlock.setNonce(nonce);
        blockchain.add(newBlock);
        System.out.println(blockchain.toString());

    }
}
