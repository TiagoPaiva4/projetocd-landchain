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
//::                                                               (c)2022   ::
//:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
//////////////////////////////////////////////////////////////////////////////
package core;

import utils.SecurityUtils;

/**
 * Created on 28/09/2022, 11:13:39
 *
 * @author IPT - computer
 * @version 1.0
 */
public class Miner {
    public static String hashAlgorithm= "SHA3-256";
    //maximum number of Nonce
    public static int MAX_NONCE = (int)1E9;

    public static int getNonce(String data, int dificulty) throws Exception {
        //String of zeros
        String zeros = String.format("%0" + dificulty + "d", 0);
       //starting nonce
        int nonce = 0;
        while (nonce < MAX_NONCE) {
            //System.out.println(nonce);
            //calculate hash of block
            String hash = SecurityUtils.calculateHash(data+nonce, hashAlgorithm);
            //verify hash
           // System.out.println(nonce +  " " + hash);
            if (hash.startsWith(zeros)) {
                return nonce;
            }
            //next nounce
            nonce++;
        }
        return nonce;
    }
    
    
    

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202209281113L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2022  :::::::::::::::::::
    ///////////////////////////////////////////////////////////////////////////
}
