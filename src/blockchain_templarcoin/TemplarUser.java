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
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import utils.FolderUtils;
import utils.SecurityUtils;

/**
 * Created on 08/10/2025, 16:47:31
 *
 * @author manso - computer
 */
public class TemplarUser implements Serializable{

    public static final String FILE_PATH = "data_user/";

    private String userName;
    private PublicKey publicKey;
    transient private PrivateKey privateKey; // não gravar as chaves nas streams
    transient private Key aesKey; // não gravar as chaves nas streams

    protected TemplarUser() {
        //construtor privado que so pode ser chamado na classe 
        new File(FILE_PATH).mkdirs();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Key getAesKey() {
        return aesKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
   

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public static TemplarUser register(String name, String password) throws Exception {
        //verificar se o user já esta registado
        if( new File(FILE_PATH + name + ".pub").exists())
            throw new Exception("User already exists :" + name);
        
        TemplarUser user = new TemplarUser();
        user.userName = name;
        //gerar as chaves
        user.aesKey = SecurityUtils.generateAESKey(256);
        KeyPair kp = SecurityUtils.generateRSAKeyPair(2048);
        user.publicKey = kp.getPublic();
        user.privateKey = kp.getPrivate();
        //guardar a publica em claro
        Files.write(Path.of(FILE_PATH + name + ".pub"), user.publicKey.getEncoded());
        //encriptar a Key AES com a publica (que desaencripta com a privada)
        byte[] secretAes = SecurityUtils.encrypt(user.aesKey.getEncoded(), user.publicKey);
        Files.write(Path.of(FILE_PATH + name + ".aes"), secretAes);

        //encriptar a privada com a password
        byte[] secretPriv = SecurityUtils.encrypt(user.privateKey.getEncoded(), password);
        Files.write(Path.of(FILE_PATH + name + ".priv"), secretPriv);
        return user;
    }

    public static TemplarUser login(String name, String pass) throws Exception {
        TemplarUser user = new TemplarUser();
        user.userName = name;
        //ler a chave privada
        byte[] secretPriv = Files.readAllBytes(Path.of(FILE_PATH + name + ".priv"));
        //desencriptar com a password
        byte[] plainPriv = SecurityUtils.decrypt(secretPriv, pass);
        user.privateKey = SecurityUtils.getPrivateKey(plainPriv);
        //ler a AES
        byte[] secretAes = Files.readAllBytes(Path.of(FILE_PATH + name + ".aes"));
        //desencriptar com a chave privada
        byte[] plainAes = SecurityUtils.decrypt(secretAes, user.privateKey);
        user.aesKey = SecurityUtils.getAESKey(plainAes);
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    public static TemplarUser login(String name) throws Exception {
        TemplarUser user = new TemplarUser();
        user.userName = name;
        //ler a publica
        byte[] plainPub = Files.readAllBytes(Path.of(FILE_PATH + name + ".pub"));
        user.publicKey = SecurityUtils.getPublicKey(plainPub);
        return user;
    }

    @Override
    public String toString() {
        StringBuilder txt = new StringBuilder(userName);
        txt.append("\npub ").append(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        if (privateKey != null) {
            txt.append("\npriv ").append(Base64.getEncoder().encodeToString(privateKey.getEncoded()));
            txt.append("\nAES ").append(Base64.getEncoder().encodeToString(aesKey.getEncoded()));
        }
        return txt.toString();
    }
    
    public static void deleteAllUsers() throws Exception{
        FolderUtils.cleanFolder(FILE_PATH, true);
        
    }

      /**
     * lê a lista de utilizadores registados
     *
     * @return
     */
    public static List<TemplarUser> getUserList() {
        List<TemplarUser> lst = new ArrayList<>();
        //Ler os ficheiros da path dos utilizadores
        File[] files = new File(FILE_PATH).listFiles();
        if (files == null) {
            return lst;
        }
        //contruir um user com cada ficheiros
        for (File file : files) {
            //se for uma chave publica
            if (file.getName().endsWith(".pub")) {
                //nome do utilizador
                String userName = file.getName().substring(0, file.getName().lastIndexOf("."));
                try {
                    lst.add(login(userName));
                } catch (Exception e) {
                }
            }
        }
        return lst;

    }
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    private static final long serialVersionUID = 202510081647L;
    //:::::::::::::::::::::::::::  Copyright(c) M@nso  2025  :::::::::::::::::::

}
