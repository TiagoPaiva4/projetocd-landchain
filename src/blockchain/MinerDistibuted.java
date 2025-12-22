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
//::                                                               (c)2024   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
 //////////////////////////////////////////////////////////////////////////////
package blockchain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created on 27/11/2024, 14:14:13
 *
 * @author manso - computer
 */
public class MinerDistibuted {

    //algorithm of hash
    public static String hashAlgorithm = "SHA3-256";
    //shared objects
    AtomicInteger nonce = new AtomicInteger(0); //nonce of message
    AtomicBoolean isWorking = new AtomicBoolean();
    AtomicBoolean isChampion = new AtomicBoolean();
    MinerListener listener;
    String message;

    public void addListener(MinerListener listener) {
        this.listener = listener;
    }

    public void stopMining(int number) {
        if (!isWorking.get()) {//not working
            return;
        }
        //stop mining
        isWorking.set(false);
        nonce.set(number);
        if (listener != null) {
            listener.onStopMining(number);
        } else {
            System.out.println("Stoping miner .. " + nonce);
        }
    }

    public String getHash() {
        return getHash(message + nonce.get());
    }

    public int getNonce() {
        return nonce.get();
    }

    public boolean isMining() {
        return isWorking.get();
    }

    public boolean isWinner() {
        return isChampion.get();
    }

    public int mine(String msg, int dificulty) {
        this.message = msg;
        try {
            //notify listener
            if (listener != null) {
                listener.onStartMining(msg, dificulty);
            } else {
                System.out.println("Start Mining " + dificulty + "\t" + msg);
            }
            //start shared objects
            isWorking.set(true);
            isChampion.set(false);
            nonce = new AtomicInteger(0); //nonce of message
            //start tickets at random
            Random rnd = new Random();
            AtomicInteger ticket = new AtomicInteger(Math.abs(rnd.nextInt())); // tickets to numbers
            //number of cores to work Allcores = Runtime.getRuntime().availableProcessors()
            int cores = 1;
            //start threads
            MinerThr thr[] = new MinerThr[cores];
            for (int i = 0; i < thr.length; i++) {
                thr[i] = new MinerThr(nonce, ticket, dificulty, msg);
                thr[i].start();
            }
            //wait to threads stop
            for (MinerThr minerThr : thr) {
                minerThr.join();
            }
            //return de value of the nonce
            return nonce.get();
        } catch (InterruptedException ex) {
            return 0;
        }

    }

    private class MinerThr extends Thread {

        AtomicInteger trueNonce; //nounce found
        AtomicInteger numberTicket; // number to test
        int dificulty; // number of zeros
        String message; // message to mine

        public MinerThr(AtomicInteger nonce, AtomicInteger ticket, int dificulty, String msg) {
            this.trueNonce = nonce;
            this.numberTicket = ticket;
            this.dificulty = dificulty;
            this.message = msg;
        }

        @Override
        public void run() {
            //zeros to find in hash
            String zeros = String.format("%0" + dificulty + "d", 0);
            //nounce not found
            while (trueNonce.get() == 0) {
                //get a number to test
                int n = numberTicket.getAndIncrement();
                //calculate hash with nonce
                String hash = getHash(message + n);
                //starts with zeros
                if (hash.startsWith(zeros)) {
                    isChampion.set(true);
                    //update true nonce
                    trueNonce.set(n);
                    //log information
                    System.getLogger(MinerDistibuted.class.getName()).log(System.Logger.Level.INFO,
                            Thread.currentThread().getName()
                            + "Found nonce = " + n
                            + "\nHash = " + hash
                            + "\nMessage = ", message);

                    //notify listener
                    if (listener != null) {
                        listener.onNonceFound(n);
                    } else {
                        System.out.println("Hash " + hash + "\nMessage " + message);
                    }

                }
            }
        }

    }

    public static String getHash(String msg) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            md.update(msg.getBytes());
            return Base64.getEncoder().encodeToString(md.digest());
        } catch (NoSuchAlgorithmException ex) {
            return "ERROR";
        }
    }

    public static int getNonce(String msg, int dificulty) {
        System.err.println("MESAAGE : " + msg);
        MinerDistibuted miner = new MinerDistibuted();
        return miner.mine(msg, dificulty);
    }

    public static void main(String[] args) {
        String msg = "Transaction demo to miner";
        int n = getNonce(msg, 3);
        System.out.println("Message = " + msg);
        System.out.println("Nonce = " + n);
        System.out.println("Hash = " + getHash(msg + n));

    }

}
