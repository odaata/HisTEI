package info.histei.contextual_info;

import info.histei.events.AbstractEventDispatcher;
import info.histei.events.RefreshEventListener;
import info.histei.lists.Listable;
import info.histei.monitor.HTFileMonitor;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.*;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;
import ro.sync.util.editorvars.EditorVariables;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static info.histei.utils.MainUtils.castPathToURL;
import static info.histei.utils.MainUtils.nullToEmpty;
import static info.histei.utils.OxygenUtils.expandOxygenPath;
import static info.histei.utils.OxygenUtils.getCurrentAuthorAccess;

/**
 * Created by mike on 1/10/14.
 */
public class ContextualInfo extends AbstractEventDispatcher<RefreshEventListener>
        implements Listable, RefreshEventListener {

    private static final Logger logger = Logger.getLogger(ContextualInfo.class.getName());

    //  Base strings for Paths
    public static final String SOURCE_BASE_PATH = EditorVariables.PROJECT_DIRECTORY + "/contextual_info/";
    public static final String XQUERY_BASE_PATH = EditorVariables.FRAMEWORK_DIRECTORY + "/resources/";
    public static final String XQUERY_PATH = XQUERY_BASE_PATH + "contextual_info.xql";

    private static Path xQueryPath;
    private static XQueryExecutable xQueryExecutable;

    private static final Map<ContextualType, Path> sourcePaths =
            Collections.synchronizedMap(new EnumMap<ContextualType, Path>(ContextualType.class));
    private static final Map<ContextualType, ContextualInfo> infos =
            Collections.synchronizedMap(new EnumMap<ContextualType, ContextualInfo>(ContextualType.class));

    /* Monitor-related */
    private static HTFileMonitor xQueryMonitor;
    private static final FileListener xQueryMonitorListener =
            new FileListener() {
                @Override
                public void fileCreated(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                @Override
                public void fileDeleted(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                @Override
                public void fileChanged(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                private void handleEvent() {
                    reload();
                    refreshInfos();
                }
            };

    @NotNull
    static ContextualInfo get(final ContextualType contextualType) {
        ContextualInfo info;
        info = infos.get(contextualType);

        if (info == null) {
            info = new ContextualInfo(contextualType);
            infos.put(contextualType, info);
        }
        return info;
    }

    @Nullable
    public static Path getXQueryPath() {
        if (xQueryPath == null) {
            xQueryPath = expandOxygenPath(XQUERY_PATH, getCurrentAuthorAccess());

            if (xQueryMonitor == null) {
                xQueryMonitor = HTFileMonitor.add(xQueryPath);

                if (xQueryMonitor != null) {
                    xQueryMonitor.addListener(xQueryMonitorListener);
                }
            }
        }
        return xQueryPath;
    }

    @Nullable
    public static Path getSourcePath(ContextualType contextualType) {
        Path sourcePath = sourcePaths.get(contextualType);

        if (sourcePath == null) {
            sourcePath =
                    expandOxygenPath(
                            SOURCE_BASE_PATH + contextualType.getFileName(),
                            getCurrentAuthorAccess()
                    );

            if (sourcePath != null) {
                sourcePaths.put(contextualType, sourcePath);
            }
        }
        return sourcePath;
    }

    @Nullable
    public static XQueryExecutable getXQueryExecutable() {
        if (xQueryExecutable == null) {
            reload();
        }
        return xQueryExecutable;
    }

    private static synchronized void reload() {
        xQueryExecutable = null;

        Path xqy = getXQueryPath();
        if (xqy != null && Files.exists(xqy)) {
            Processor proc = new Processor(true);
            proc.setConfigurationProperty(FeatureKeys.XQUERY_VERSION, "3.0");
            try {
                xQueryExecutable = proc.newXQueryCompiler().compile(xqy.toFile());
            } catch (SaxonApiException | IOException e) {
                logger.error(e, e);
            }
        }
    }

    private static void refreshInfos() {
        synchronized (infos) {
            for (ContextualInfo info : infos.values()) {
                info.refresh();
            }
        }
    }

    /* Instance members */

    private ContextualType contextualType;
    private boolean initialized;
    private final Map<String, List<ContextualItem>> items =
            Collections.synchronizedMap(new HashMap<String, List<ContextualItem>>());

    /* Monitor-related */
    private HTFileMonitor sourceMonitor;
    private FileListener sourceMonitorListener =
            new FileListener() {
                @Override
                public void fileCreated(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                @Override
                public void fileDeleted(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                @Override
                public void fileChanged(FileChangeEvent event) throws Exception {
                    handleEvent();
                }

                private void handleEvent() {
                    refresh();
                    notifyListeners();
                }
            };

    protected ContextualInfo(ContextualType contextualType) {
        this.contextualType = contextualType;
    }

    @NotNull
    public ContextualType getContextualType() {
        return contextualType;
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Nullable
    public Path getPath() {
        Path sourcePath = getSourcePath(getContextualType());

        if (sourceMonitor == null) {
            sourceMonitor = HTFileMonitor.add(sourcePath);

            if (sourceMonitor != null) {
                sourceMonitor.addListener(sourceMonitorListener);
            }
        }
        return sourcePath;
    }

    @Nullable
    public URL getURL() {
        return castPathToURL(getPath());
    }

    @NotNull
    public List<ContextualItem> getItems(final String type) {
        String typeCleaned = nullToEmpty(type);

        if (!initialized) {
            refresh();
        }

        synchronized (items) {
            return items.containsKey(typeCleaned) ? items.get(typeCleaned) : new ArrayList<ContextualItem>();
        }
    }

    @NotNull
    @Override
    public List<ContextualItem> getItems() {
        return getItems(null);
    }

    @Override
    public void refresh() {
        Path src = getPath();
        if (src != null && Files.exists(src)) {
            XQueryExecutable xqx = getXQueryExecutable();
            if (xqx != null) {
                XQueryEvaluator xqe = xqx.load();
                if (xqe != null) {
                    reset();
                    addItems(src, xqe);
                }
            }
        }
    }

    private synchronized void addItems(Path src, XQueryEvaluator xqe) {
        try {
            xqe.setSource(new SAXSource(new InputSource(src.toUri().toString())));
            for (XdmItem item : xqe) {
                XdmNode node = (XdmNode) item;
                ContextualItem contextualItem = ContextualItem.get(getContextualType(), node);
                if (contextualItem != null) {

                    String type = contextualItem.getType();
                    List<ContextualItem> typedItems = items.get(type);

                    if (typedItems == null) {
                        typedItems = new ArrayList<>();
                        items.put(type, typedItems);
                    }
                    typedItems.add(contextualItem);
                }
            }
            initialized = true;
        } catch (SaxonApiException e) {
            items.clear();
            logger.error(e, e);
        }
    }

    @Override
    public synchronized void reset() {
        initialized = false;
        items.clear();
    }

    /* Event Dispatcher */

    @Override
    protected synchronized void notifyListeners() {
        for (RefreshEventListener listener : getListeners()) {
            listener.refresh();
        }
    }

}
