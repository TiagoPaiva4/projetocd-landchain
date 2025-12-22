package blockchain_templarcoin;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import utils.SecurityUtils;

/**
 * Esta classe gere o formato seguro das transações RWA.
 * Formato: CHAVE_PUBLICA_BASE64 || ASSINATURA_BASE64 || DADOS_RWA_BASE64
 */
public class TemplarTransaction {

    // Separador seguro para dividir os campos da mensagem
    private static final String SEPARATOR = "||";

    /**
     * Cria uma transação assinada pronta para ser enviada para a rede.
     * * @param rwaData - Os dados do ativo (ex: JSON ou String do RWARecord)
     * @param privateKey - A chave privada do dono (para assinar)
     * @param publicKey - A chave pública do dono (para validar)
     * @return String formatada e pronta para o addTransaction()
     */
    public static String create(String rwaData, PrivateKey privateKey, PublicKey publicKey) throws Exception {
        // 1. Converter os dados do RWA para Base64 (garante que não há caracteres estragados)
        String rwaDataBase64 = Base64.getEncoder().encodeToString(rwaData.getBytes());

        // 2. Assinar os dados (assinamos a versão Base64 para garantir integridade exata)
        byte[] signature = SecurityUtils.sign(rwaDataBase64.getBytes(), privateKey);
        String signatureBase64 = Base64.getEncoder().encodeToString(signature);

        // 3. Converter a chave pública para Base64
        String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());

        // 4. Juntar tudo numa String única
        return publicKeyBase64 + SEPARATOR + signatureBase64 + SEPARATOR + rwaDataBase64;
    }

    /**
     * Valida se uma transação recebida é autêntica.
     * * @param transactionBlock - A string completa recebida da rede
     * @return true se a assinatura corresponder aos dados e à chave pública
     */
    public static boolean isValid(String transactionBlock) {
        try {
            // Separar os campos usando o separador (com escape para regex)
            String[] parts = transactionBlock.split(java.util.regex.Pattern.quote(SEPARATOR));
            
            // Tem de ter exatamente 3 partes
            if (parts.length != 3) {
                return false;
            }

            String pubKeyB64 = parts[0];
            String sigB64 = parts[1];
            String dataB64 = parts[2]; // Isto são os dados RWA em Base64

            // Converter de volta de Base64 para objetos Java
            PublicKey pubKey = SecurityUtils.getPublicKey(Base64.getDecoder().decode(pubKeyB64));
            byte[] signature = Base64.getDecoder().decode(sigB64);
            byte[] dataBytes = dataB64.getBytes(); 

            // Verificar criptograficamente a assinatura
            return SecurityUtils.verify(dataBytes, signature, pubKey);

        } catch (Exception e) {
            // Se der erro no parse ou na validação
            return false;
        }
    }
    
    /**
     * Recupera os dados originais (legíveis) da transação para mostrar na GUI.
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