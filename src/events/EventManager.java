/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package events;

import java.util.ArrayList;
import java.util.List;
import events.Event;
import events.EventListener;

/**
 *
 * @author Tiago Paiva
 */
public class EventManager {

    private final List<EventListener> listeners = new ArrayList<>();

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void publish(Event e) {
        for (EventListener l : listeners) {
            l.onEvent(e);
        }
    }
}

