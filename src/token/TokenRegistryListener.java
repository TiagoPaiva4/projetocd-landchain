/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package token;

import events.Event;
import events.EventListener;
import events.NewRwaRegisteredEvent;

/**
 *
 * @author Tiago Paiva
 */

public class TokenRegistryListener implements EventListener {

    private final TokenRegistry registry;

    public TokenRegistryListener(TokenRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof NewRwaRegisteredEvent e) {
            registry.mintTokensForRwa(e.getRecord());
        }
    }
}

