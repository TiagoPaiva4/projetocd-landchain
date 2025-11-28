/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package rwa;

/**
 *
 * @author Tiago Paiva
 */
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.Arrays;

import utils.SecurityUtils;

public class RWAValidator {

    /**
     * Valida um RWARecord com base num documento real.
     *
     * Verifica:
     *  1) Hash do documento == hash guardado no RWARecord
     *  2) Assinatura digital da Oracle é válida
     *  3) Chave pública é válida e corresponde ao emissor
     *
     * @param record   RWARecord guardado na blockchain
     * @param filePath caminho do documento original
     * @return true se tudo estiver válido
     */
    public static boolean validar(RWARecord record, String filePath) {
        try {
            // 1) Ler documento original
            byte[] documentoReal = Files.readAllBytes(Paths.get(filePath));

            // 2) Recalcular hash
            byte[] hashReal = SecurityUtils.calculateHash(documentoReal, "SHA3-256");

            if (!Arrays.equals(hashReal, record.getHashDocumento())) {
                System.out.println("❌ Hash inválido — documento não corresponde ao original.");
                return false;
            }

            // 3) Validar assinatura digital da Oracle
            PublicKey chaveOracle = SecurityUtils.getPublicKey(record.getPublicKeyOracle());

            boolean assinaturaValida =
                    SecurityUtils.verifySign(
                            record.getHashDocumento(),
                            record.getAssinatura(),
                            chaveOracle
                    );

            if (!assinaturaValida) {
                System.out.println("❌ Assinatura digital inválida!");
                return false;
            }

            // 4) Verificar timestamp
            long agora = System.currentTimeMillis();
            if (record.getTimestamp() > agora) {
                System.out.println("❌ Timestamp inválido (no futuro).");
                return false;
            }

            System.out.println("✔ RWA validado com sucesso!");
            return true;

        } catch (Exception e) {
            System.out.println("❌ Erro na validação: " + e.getMessage());
            return false;
        }
    }

}