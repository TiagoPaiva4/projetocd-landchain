//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: 
//::                                                                         ::
//::     Antonio Manuel Rodrigues Manso                                      ::
//::                                                                         ::
//::                                                                         ::
//::     I N S T I T U T O    P O L I T E C N I C O   D E   T O M A R        ::
//::     Escola Superior de Tecnologia de Tomar                              ::
//::     e-mail: manso@ipt.pt                                                ::
//::     url   : http://orion.ipt.pt/~manso                                  ::
//::                                                                         ::
//::     This software was build with the purpose of investigate and         ::
//::     learning.                                                           ::
//::                                                                         ::
//::                                                               (c)2024   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////
package blockchain_templarcoin;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Created on 27/11/2024, 17:42:15
 *
 * @author manso - computer
 */
public interface RemoteNodeInterface extends Remote {

    //:::: N E T WO R K  :::::::::::
    public String getAdress() throws RemoteException;

    public void addNode(RemoteNodeInterface node) throws RemoteException;

    public List<RemoteNodeInterface> getNetwork() throws RemoteException;

    //::::::::::: T R A N S A C T I O N S  :::::::::::
    public void addTransaction(String data) throws RemoteException;

    public List<String> getTransactions() throws RemoteException;

    //::::::::::: M I N E R  :::::::::::
    public void startMiner(String message, int dificulty) throws RemoteException;

    public void stopMining(int nonce) throws RemoteException;

    public MinerDistibuted getMiner() throws RemoteException;
    //::::::::::: B L O C K C H A I N  :::::::::::

    public void addBlock(Block b) throws RemoteException;

    public int getBlockchainSize() throws RemoteException;

    public Block getlastBlock() throws RemoteException;

    public BlockChain getBlockchain() throws RemoteException;

    public void setBlockchain(BlockChain b) throws RemoteException;

    public void synchronizeBlockchain(RemoteNodeInterface node) throws RemoteException;

    public List getRegisteredTransactions() throws RemoteException;
}
