package eu.emergingstandards.events;

import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by mike on 2/12/14.
 */
public abstract class EMSTAbstractEventDispatcher<L extends EventListener> implements EMSTEventDispatcher<L> {

    private Map<L, Integer> listeners = Collections.synchronizedMap(new WeakHashMap<L, Integer>());

    @NotNull
    public synchronized List<L> getListeners() {
        Map<Integer, L> sortedListeners = new TreeMap<>();

        for (L listener : listeners.keySet()) {
            sortedListeners.put(listeners.get(listener), listener);
        }
        return new ArrayList<>(sortedListeners.values());
    }

    @Override
    public synchronized void addListener(L listener) {
        listeners.put(listener, listeners.size() + 1);
    }

    @Override
    public synchronized void removeListener(L listener) {
        listeners.remove(listener);
    }

    @Override
    public synchronized void removeAllListeners() {
        listeners.clear();
    }

    protected abstract void notifyListeners();
}
