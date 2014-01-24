package eu.emergingstandards.watcher;

import java.nio.file.Path;
import java.util.EventObject;

/**
 * Created by mike on 1/16/14.
 */
public class EMSTWatchEvent extends EventObject {

    private EMSTWatchEventType type;
    private Path path;

    /**
     * Constructs a prototypical Event.
     *
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public EMSTWatchEvent(EMSTFileWatcher source, EMSTWatchEventType type, Path path) {
        super(source);
        this.type = type;
        this.path = path;
    }

    public EMSTWatchEventType getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }
}
