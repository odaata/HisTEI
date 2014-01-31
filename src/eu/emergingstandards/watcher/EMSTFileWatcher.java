package eu.emergingstandards.watcher;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Created by mike on 1/16/14.
 */
public class EMSTFileWatcher {

    private static final Logger logger = Logger.getLogger(EMSTFileWatcher.class.getName());

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }

    private static WatchService watchService;
    public static WatchService getWatchService() {
        if (watchService == null) {
            try {
                watchService = FileSystems.getDefault().newWatchService();
                getExecutorService().execute(new EMSTWatchQueueReader(watchService));
            } catch (IOException e) {
                watchService = null;
            }
        }
        return watchService;
    }

    private static ExecutorService executorService;
    public static ExecutorService getExecutorService() {
        if (executorService == null || executorService.isTerminated()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return executorService;
    }

    private static final Map<WatchKey, Path> watchKeys = new HashMap<>();
    private static final Map<Path, EMSTFileWatcher> watchers = new WeakHashMap<>();

    @Nullable
    public static EMSTFileWatcher get(Path path) {
        EMSTFileWatcher watcher = null;

        if (path != null) {
            watcher = watchers.get(path);
            if (watcher == null) {
                try {
                    WatchService watchService = getWatchService();
                    if (watchService != null) {
                        Path parentPath = path.getParent();
                        if (!watchKeys.containsValue(parentPath)) {
                            WatchKey key = parentPath.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                            watchKeys.put(key, parentPath);
                        }
                        watcher = new EMSTFileWatcher(path);
                        watchers.put(path, watcher);
                    }
                } catch (IOException e) {
                    logger.error(e, e);
                }
            }
        }
        return watcher;
    }

    private static class EMSTWatchQueueReader implements Runnable {

        /** the watchService that is passed in from above */
        private WatchService watchService;

        private EMSTWatchQueueReader(WatchService watchService) {
            this.watchService = watchService;
        }

        /**
         * In order to implement a file watcher, we loop forever
         * ensuring requesting to take the next item from the file
         * watchers queue.
         */
        @Override
        public void run() {
            try {
                // get the first event before looping
                WatchKey key = watchService.take();
                while (key != null) {
                    Path path = watchKeys.get(key);
                    if (path != null) {
                        for (WatchEvent event : key.pollEvents()) {
                            /*System.out.printf("Received %s event for file: %s",
                                    event.kind(), event.context() );*/

                            WatchEvent.Kind kind = event.kind();

                            if (kind == OVERFLOW) {
                                continue;
                            }

                            WatchEvent<Path> ev = cast(event);
                            Path name = ev.context();
                            Path file = path.resolve(name);

                            logger.debug("WatchEvent - dir: " + name.toString() + "; file: " + path.toString());

                            EMSTFileWatcher watcher = watchers.get(file);
                            if (watcher != null) {
                                EMSTWatchEventType type = null;
                                if (kind == ENTRY_CREATE) {
                                    type = EMSTWatchEventType.FILE_CREATED;
                                } else if (kind == ENTRY_DELETE) {
                                    type = EMSTWatchEventType.FILE_DELETED;
                                } else if (kind == ENTRY_MODIFY) {
                                    type = EMSTWatchEventType.FILE_MODIFIED;
                                }
                                watcher.fireEvent(type);
                            }
                        }
                    }
                    // reset key and remove from set if directory no longer accessible
                    boolean valid = key.reset();
                    if (!valid) {
                        watchKeys.remove(key);

                        // all directories are inaccessible, so stop watching
                        if (watchKeys.isEmpty()) {
                            break;
                        }
                    }
                    key = watchService.take();
                }
            } catch (InterruptedException e) {
                logger.error(e, e);
            }
        }
    }

    /*  Instance Members */

    private Path path;

    protected EMSTFileWatcher(Path path) {
        this.path = path;
    }

    private List<EMSTWatcherListener> listeners = new ArrayList<>();
    public synchronized void addEventListener(EMSTWatcherListener listener)  {
        listeners.add(listener);
    }
    public synchronized void removeEventListener(EMSTWatcherListener listener)   {
        listeners.remove(listener);
    }

    private synchronized void fireEvent(EMSTWatchEventType type) {
        EMSTWatchEvent event = new EMSTWatchEvent(this, type, path);
        logger.debug("Firing WatchEvents for type:" + type.toString() + "; path: " + path.toString());
        for (EMSTWatcherListener listener : listeners) {
            listener.fileChanged(event);
        }
    }
}
