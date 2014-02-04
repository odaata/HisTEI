package eu.emergingstandards.monitor;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.AbstractFileSystem;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by mike on 2/2/14.
 * <p/>
 * File Monitor to watch files for creation, deletion and modification
 * Based heavily on {@link org.apache.commons.vfs2.impl.DefaultFileMonitor}
 */
public class EMSTFileMonitor implements FileMonitor {

    private static final Logger logger = Logger.getLogger(EMSTFileMonitor.class.getName());

    private static final long DEFAULT_DELAY = 1;
    private static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

    private static final Map<Path, EMSTFileMonitor> monitorMap = new WeakHashMap<>();
//    private static ScheduledExecutorService scheduler = getScheduler();
//    private static ScheduledFuture<?> service = getService();

    protected static class EMSTMonitorService implements Runnable {
        @Override
        public void run() {
            try {
                synchronized (monitorMap) {
                    for (Path path : monitorMap.keySet()) {
                        EMSTFileMonitor monitor = monitorMap.get(path);
                        if (monitor != null) {
                            monitor.check();
                        }
                    }
                }
            } catch (Throwable th) {
                logger.error(th, th);
            }
        }
    }

    private static ScheduledExecutorService scheduler = getScheduler();

    @NotNull
    public static ScheduledExecutorService getScheduler() {
        if (scheduler == null || scheduler.isTerminated()) {
            scheduler = Executors.newScheduledThreadPool(1);
        }
        return scheduler;
    }

    private static ScheduledFuture<?> service = getService();

    @NotNull
    public static ScheduledFuture<?> getService() {
        if (service == null || service.isDone()) {
            service = getScheduler().scheduleWithFixedDelay(
                    new EMSTMonitorService(), DEFAULT_DELAY, DEFAULT_DELAY, DEFAULT_TIME_UNIT);
        }
        return service;
    }

    private static FileSystemManager fileSystemManager;

    @Nullable
    public static FileSystemManager getFileSystemManager() {
        if (fileSystemManager == null) {
            try {
                fileSystemManager = VFS.getManager();
            } catch (FileSystemException e) {
                logger.error(e, e);
            }
        }
        return fileSystemManager;
    }

    @Nullable
    public static EMSTFileMonitor add(Path path) {
        EMSTFileMonitor monitor = null;

        if (path != null) {
            if (getService().isDone()) {
                logger.error("EMSTFileMonitorService is not running!");
            } else {
                synchronized (monitorMap) {
                    monitor = monitorMap.get(path);
                    if (monitor == null) {
                        monitor = new EMSTFileMonitor(path);
                        monitorMap.put(path, monitor);
                    }
                }
            }
        }
        return monitor;
    }

    public static void remove(Path path) {
        if (path != null) {
            synchronized (monitorMap) {
                EMSTFileMonitor monitor = monitorMap.get(path);
                if (monitor != null) {
                    monitor.removeAllListeners();
                    monitorMap.remove(path);
                }
            }
        }
    }

    /* Instance Members */

    private FileObject file;
    private boolean exists;
    private long timestamp;


    protected EMSTFileMonitor(Path path) {
        FileSystemManager fsm = getFileSystemManager();
        if (fsm != null) {
            try {
                file = fsm.resolveFile(path.toString());
            } catch (FileSystemException e) {
                logger.error(e.getLocalizedMessage(), e);
            }

            if (file != null) {
                refresh();

                try {
                    exists = file.exists();
                } catch (FileSystemException fse) {
                    exists = false;
                    timestamp = -1;
                }

                if (exists) {
                    try {
                        timestamp = file.getContent().getLastModifiedTime();
                    } catch (FileSystemException fse) {
                        timestamp = -1;
                    }
                }
            }
        }
    }

    /**
     * Clear the cache and re-request the file object
     */
    private void refresh() {
        try {
            file.refresh();
        } catch (FileSystemException fse) {
            logger.error(fse.getLocalizedMessage(), fse);
        }
    }

    public void check() {
        refresh();

        try {
            // If the file existed and now doesn't
            if (exists && !file.exists()) {
                exists = file.exists();
                timestamp = -1;

                // Fire delete event
                ((AbstractFileSystem)
                        file.getFileSystem()).fireFileDeleted(file);
            } else if (exists && file.exists()) {

                // Check the timestamp to see if it has been modified
                if (timestamp != file.getContent().getLastModifiedTime()) {
                    timestamp = file.getContent().getLastModifiedTime();

                    // Fire change event
                    ((AbstractFileSystem)
                            file.getFileSystem()).fireFileChanged(file);
                }

            } else if (!exists && file.exists()) {
                exists = file.exists();
                timestamp = file.getContent().getLastModifiedTime();

                ((AbstractFileSystem)
                        file.getFileSystem()).fireFileCreated(file);
            }
        } catch (FileSystemException fse) {
            logger.error(fse.getLocalizedMessage(), fse);
        }
    }

    private List<FileListener> listeners = new ArrayList<>();

    public synchronized void addListener(FileListener listener) {
        listeners.add(listener);
        file.getFileSystem().addListener(file, listener);
    }

    public synchronized void removeListener(FileListener listener) {
        file.getFileSystem().removeListener(file, listener);
        listeners.remove(listener);
    }

    public synchronized void removeAllListeners() {
        for (FileListener listener : listeners) {
            file.getFileSystem().removeListener(file, listener);
        }
        listeners.clear();
    }

    @Override
    public void addFile(FileObject file) {

    }

    @Override
    public void removeFile(FileObject file) {

    }
}
