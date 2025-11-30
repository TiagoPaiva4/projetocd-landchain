package events;

import core.BlockChain;
import token.TokenRegistry;
import java.util.Map;

public class RentListener implements EventListener {

    private final TokenRegistry registry;

    // Precisamos do registry para saber quem são os donos
    public RentListener(TokenRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof RentDistributionEvent) {
            RentDistributionEvent e = (RentDistributionEvent) event;
            System.out.println("\n$$$ DISTRIBUIÇÃO DE RENDA INICIADA $$$");
            System.out.println("Ativo: " + e.getAssetID());
            System.out.println("Valor Total: " + e.getAmount() + " €");

            // 1. Obter quem tem os tokens
            Map<String, Integer> holders = registry.getAssetHolders(e.getAssetID());
            
            if (holders.isEmpty()) {
                System.out.println(">> Ninguém detém tokens deste ativo (ou supply é 0).");
                return;
            }

            // 2. Calcular total de tokens em circulação para este ativo
            int totalTokens = 0;
            for (int qtd : holders.values()) totalTokens += qtd;

            // 3. Calcular e imprimir quanto cada um ganha
            System.out.println("--- Pagamentos aos Acionistas ---");
            for (String wallet : holders.keySet()) {
                int userTokens = holders.get(wallet);
                
                // Regra de 3 simples: (MeusTokens / TotalTokens) * ValorRenda
                double share = ((double) userTokens / totalTokens) * e.getAmount();
                
                String walletShort = wallet.substring(0, 15) + "...";
                System.out.printf(" > Wallet: %s | Tem: %d (%.1f%%) | Recebe: %.2f €\n", 
                        walletShort, userTokens, ((double)userTokens/totalTokens)*100, share);
            }
            System.out.println("$$$ DISTRIBUIÇÃO CONCLUÍDA $$$\n");
        }
    }
}