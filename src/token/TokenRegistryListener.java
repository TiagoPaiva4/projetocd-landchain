package token;

import events.Event;
import events.EventListener;
import events.NewRwaRegisteredEvent;

public class TokenRegistryListener implements EventListener {

    private final TokenRegistry registry;

    public TokenRegistryListener(TokenRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onEvent(Event newEvent) {
        // Verifica se o evento é do tipo certo
        if (newEvent instanceof NewRwaRegisteredEvent) {
            
            // Faz o "Cast" (converte) para o tipo específico
            NewRwaRegisteredEvent e = (NewRwaRegisteredEvent) newEvent;
            
            // Agora o método getAssetID() já vai ser encontrado após o "Clean and Build"
            System.out.println("Listener: Detetado novo RWA registado: " + e.getAssetID());
        }
    }
}