package events;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private final List<EventListener> listeners = new ArrayList<>();

    public synchronized void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public synchronized void publish(Event e) {
        // copia a lista para evitar ConcurrentModification durante callbacks
        List<EventListener> snapshot = new ArrayList<>(listeners);
        for (EventListener l : snapshot) {
            try {
                l.onEvent(e);
            } catch (Exception ex) {
                System.err.println("Event listener error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
