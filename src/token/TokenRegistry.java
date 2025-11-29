package token;

import rwa.RWARecord;
import java.util.HashMap;
import java.util.Map;

public class TokenRegistry {

    // total de tokens por ativo
    private final Map<String, Token> tokens = new HashMap<>();

    // wallets
    private final WalletManager walletManager = new WalletManager();

    // número fixo de tokens por ativo
    private static final int TOKENS_PER_ASSET = 1000;

    public void mintTokensForRwa(RWARecord record) {

        String assetID = record.getAssetID();

        if (tokens.containsKey(assetID)) {
            System.out.println("RWA " + assetID + " já tem tokens emitidos!");
            return;
        }

        // cria o token principal
        Token token = new Token(assetID, TOKENS_PER_ASSET);
        tokens.put(assetID, token);

        // atribui todos os tokens à Oracle (proprietário inicial)
        Wallet oracleWallet = walletManager.getOrCreateWallet("ORACLE");
        oracleWallet.addTokens(assetID, TOKENS_PER_ASSET);

        System.out.println("✔ Emitidos " + TOKENS_PER_ASSET +
                " tokens para o ativo " + assetID);
    }

    public WalletManager getWalletManager() {
        return walletManager;
    }

    public Token getToken(String assetID) {
        return tokens.get(assetID);
    }

    public void printTokens() {
        System.out.println("\n===== TOKENS EMITIDOS =====");
        tokens.values().forEach(System.out::println);
    }
}
