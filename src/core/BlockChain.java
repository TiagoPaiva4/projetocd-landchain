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
import utils.FolderUtils;

/**
 * Created on 08/10/2025, 15:28:52
 *
 * @author manso - computer
 */
public class BlockChain implements Serializable {

    public static final String FILE_PATH = "data_blocks/";

    List<Block> blocks;

    private BlockChain() {
        //creates a path to store blocks and blockchain
        new File(FILE_PATH).mkdirs();
    }

    /**
     * creates a blockchain with a genesis block
     *
     * @param genesis
     */
    public BlockChain(Block genesis) throws Exception {
        this();
        blocks = new ArrayList<>();
         //add block to blockchain 
        blocks.add(genesis);
        //save new block
        genesis.save(FILE_PATH);
        save(FILE_PATH + "blockchain.bch");
    }
    
    public void add(Object[] elements) throws Exception {
        add(Arrays.asList(elements));
    }

    
    
    public void add(List data) throws Exception {
        //last block of blockchain
        Block lastBlock = blocks.get(blocks.size()-1);
        //builde new block
        Block newBlock = new Block(
                lastBlock.getID() + 1,
                lastBlock.getCurrentHash(),
                lastBlock.getDificulty(),
                data);
        //mine block
        newBlock.mine();
        //add new block to blockchain
        add(newBlock);
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
        newBlock.save(FILE_PATH);
        save(FILE_PATH + "blockchain.bch");
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
        return "BlockChain" + blocks;
    }

    /**
     * saves the block chain in disk
     *
     * @param fileName
     * @throws java.lang.Exception
     */
    public void save(String fileName) throws Exception {
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
    public static void deleteAllBlocks() throws IOException{
        FolderUtils.cleanFolder(FILE_PATH, true);
    }

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081528L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
