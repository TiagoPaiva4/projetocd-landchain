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

import java.io.Serializable;
import java.security.PublicKey;
import utils.SecurityUtils;
import utils.Utils;

/**
 * Created on 14/10/2025, 11:47:10
 *
 * @author manso - computer
 */
public class TemplarTransaction implements Serializable {

    //atributos auxiliares (apagar em produção)
    String txtSender;
    String txtReceiver;

    PublicKey sender;
    PublicKey receiver;
    long timestamp;
    double value;
    byte[] signature;

    public TemplarTransaction(String senderName, String receiverName, double val, String pass) throws Exception {
        //ler as credenciais do sender e do receiver
        TemplarUser uSender = TemplarUser.login(senderName, pass);
        TemplarUser uReceiver = TemplarUser.login(receiverName);

        this.txtSender = uSender.getUserName();
        this.sender = uSender.getPublicKey();

        this.txtReceiver = uReceiver.getUserName();
        this.receiver = uReceiver.getPublicKey();

        this.value = val;

        this.timestamp = System.currentTimeMillis();

        //juntar os bytes de todos os dados
        byte[] allData = Utils.concatenate(this.sender.getEncoded(), this.receiver.getEncoded());
        allData = Utils.concatenate(allData, Utils.doubleToBytes(value));
        allData = Utils.concatenate(allData, Utils.longToBytes(timestamp));
        //assinar
        this.signature = SecurityUtils.sign(allData, uSender.getPrivateKey());
    }

    

    public String getTxtSender() {
        return txtSender;
    }

    public String getTxtReceiver() {
        return txtReceiver;
    }

    public PublicKey getSender() {
        return sender;
    }

    public PublicKey getReceiver() {
        return receiver;
    }

    public double getValue() {
        return value;
    }

    public byte[] getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return txtSender + " -> " + value + " -> " + txtReceiver;
    }
    
    

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510141147L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::


///////////////////////////////////////////////////////////////////////////
}
