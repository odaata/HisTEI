package eu.emergingstandards.events;

import java.util.EventListener;
import java.util.List;

/**
 * Created by mike on 2/12/14.
 */
public interface EMSTEventDispatcher<L extends EventListener> {

    List<L> getListeners();

    void addListener(L listener);

    void removeListener(L listener);

    void removeAllListeners();
}
