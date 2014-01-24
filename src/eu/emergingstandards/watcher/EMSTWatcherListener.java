package eu.emergingstandards.watcher;

import java.util.EventListener;

/**
 * Created by mike on 1/16/14.
 */
public interface EMSTWatcherListener extends EventListener {
    public void fileChanged(EMSTWatchEvent watchEvent);
}
