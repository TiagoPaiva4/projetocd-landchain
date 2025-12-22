package blockchain_templarcoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import utils.SecurityUtils;

public class TemplarTransaction {

    // Separador seguro para dividir os campos da mensagem
    private static final String SEPARATOR = "||";

    /**
     * Cria uma transação assinada pronta para ser enviada para a rede.
     */
    public static String create(String rwaData, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        // 1. Converter dados para Base64
        String rwaDataBase64 = Base64.getEncoder().encodeToString(rwaData.getBytes());

        // 2. Assinar os dados em Base64
        byte[] signature = SecurityUtils.sign(rwaDataBase64.getBytes(), privateKey);
        String signatureBase64 = Base64.getEncoder().encodeToString(signature);

        // 3. Converter a chave pública para Base64
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        // 4. Juntar tudo
        return publicKeyBase64 + SEPARATOR + signatureBase64 + SEPARATOR + rwaDataBase64;
    }

    /**
     * Valida se uma transação recebida é autêntica.
     */
    public static boolean isValid(String transactionBlock) {
        try {
            // "Pattern.quote" evita erros se o separador tiver caracteres especiais
            String[] parts = transactionBlock.split(java.util.regex.Pattern.quote(SEPARATOR));
            
            if (parts.length != 3) {
                return false;
            }

            String pubKeyB64 = parts[0];
            String sigB64 = parts[1];
            String dataB64 = parts[2]; 

            // Converter de volta
            PublicKey pubKey = SecurityUtils.getPublicKey(Base64.getDecoder().decode(pubKeyB64));
            byte[] signature = Base64.getDecoder().decode(sigB64);
            byte[] dataBytes = dataB64.getBytes(); 

            // CORREÇÃO AQUI: O método na tua classe SecurityUtils chama-se verifySign
            return SecurityUtils.verifySign(dataBytes, signature, pubKey);

        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Recupera os dados originais para mostrar na GUI.
     */
    public static String getData(String transactionBlock) {
         try {
            String[] parts = transactionBlock.split(java.util.regex.Pattern.quote(SEPARATOR));
            if (parts.length < 3) return "Dados Inválidos";
            
            String dataB64 = parts[2];
            return new String(Base64.getDecoder().decode(dataB64));
        } catch (Exception e) {
            return "Erro a ler dados";
        }
    }
}