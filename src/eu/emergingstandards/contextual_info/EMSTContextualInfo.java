package eu.emergingstandards.contextual_info;

import eu.emergingstandards.monitor.EMSTFileMonitor;
import eu.emergingstandards.utils.EMSTUtils;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.*;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.util.editorvars.EditorVariables;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static eu.emergingstandards.utils.EMSTOxygenUtils.expandOxygenPath;
import static eu.emergingstandards.utils.EMSTOxygenUtils.getCurrentAuthorEditorPage;

/**
 * Created by mike on 1/10/14.
 */
public class EMSTContextualInfo {

    /* Global (Static) members */

    private static final Logger logger = Logger.getLogger(EMSTContextualInfo.class.getName());
    //  Base strings for Paths
    public static final String SOURCE_BASE_PATH = EditorVariables.PROJECT_DIRECTORY + "/contextual_info/";
    public static final String XQUERY_BASE_PATH = EditorVariables.FRAMEWORK_DIRECTORY + "/resources/";
    public static final String XQUERY_PATH = XQUERY_BASE_PATH + "contextual_info.xql";

    private static final Map<EMSTContextualType, EMSTContextualInfo> infos = new EnumMap<>(EMSTContextualType.class);

    @Nullable
    public static EMSTContextualInfo get(EMSTContextualType contextualType) {
        EMSTContextualInfo info = null;

        if (contextualType != null) {
            info = infos.get(contextualType);

            if (info == null) {
                info = new EMSTContextualInfo(contextualType);
                infos.put(contextualType, info);
            }
        }
        return info;
    }

    /* Instance members */

    private EMSTContextualType contextualType;
    private boolean initialized;

    private WSAuthorEditorPage authorPage;
    private AuthorAccess authorAccess;

    private Path xQueryPath;
    private Path sourcePath;
    private XQueryEvaluator xQueryEvaluator;

    private Map<String, List<EMSTContextualItem>> items = new HashMap<>();

    protected EMSTContextualInfo(EMSTContextualType contextualType) {
        this.contextualType = contextualType;
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @Nullable
    public WSAuthorEditorPage getAuthorPage() {
        if (authorPage == null) {
            authorPage = getCurrentAuthorEditorPage();
        }
        return authorPage;
    }

    @Nullable
    public AuthorAccess getAuthorAccess() {
        if (authorAccess == null) {
            WSAuthorEditorPage page = getAuthorPage();
            if (page != null) {
                authorAccess = page.getAuthorAccess();
            }
        }
        return authorAccess;
    }

    @Nullable
    public Path getXQueryPath() {
        if (xQueryPath == null) {
            xQueryPath = expandOxygenPath(XQUERY_PATH, getAuthorAccess());

            if (xQueryMonitor == null && xQueryPath != null)
                xQueryMonitor = EMSTFileMonitor.add(xQueryPath);
        }
        return xQueryPath;
    }

    @Nullable
    public Path getSourcePath() {
        if (sourcePath == null) {
            sourcePath =
                    expandOxygenPath(
                            SOURCE_BASE_PATH + EMSTContextualSettings.get(getContextualType()).getFileName(),
                            getAuthorAccess()
                    );

            if (sourceMonitor == null && sourcePath != null)
                sourceMonitor = EMSTFileMonitor.add(sourcePath);
        }
        return sourcePath;
    }

    @Nullable
    public XQueryEvaluator getXQueryEvaluator() {
        if (xQueryEvaluator == null) {
            reload();
        }
        return xQueryEvaluator;
    }

    @NotNull
    public List<EMSTContextualItem> getItems(String type) {
        String typeCleaned = EMSTUtils.nullToEmpty(type);

        if (!initialized) {
            refresh();
        }

        return items.containsKey(typeCleaned) ? items.get(typeCleaned) : new ArrayList<EMSTContextualItem>();
    }

    public void reload() {
        Path xqy = getXQueryPath();
        if (xqy != null) {
//          Force synchronization so Oxygen gets the values on startup
            synchronized (this) {
                reset();

                Processor proc = new Processor(true);
                proc.setConfigurationProperty(FeatureKeys.XQUERY_VERSION, "3.0");
                try {
                    xQueryEvaluator = proc.newXQueryCompiler().compile(xqy.toFile()).load();
                } catch (SaxonApiException | IOException e) {
                    logger.error(e, e);
                }
            }
        }
    }

    public void refresh() {
        Path src = getSourcePath();
        if (src != null) {
            XQueryEvaluator xqe = getXQueryEvaluator();
            if (xqe != null) {
//              Force synchronization so Oxygen gets the values on startup
                synchronized (this) {
                    reset();
                    try {
                        xqe.setSource(new SAXSource(new InputSource(src.toUri().toString())));
                        for (XdmItem item : xqe) {
                            XdmNode node = (XdmNode) item;
                            EMSTContextualItem contextualItem = EMSTContextualItem.get(getContextualType(), node);
                            if (contextualItem != null) {
                                String type = contextualItem.getType();
                                List<EMSTContextualItem> typedItems = items.get(type);

                                if (typedItems == null) typedItems = new ArrayList<>();

                                typedItems.add(contextualItem);

                                items.put(type, typedItems);
                            }
                        }
                        addMonitors();
                        initialized = true;
                    } catch (SaxonApiException e) {
                        logger.error(e, e);
                    }
                }
            }
        }
    }

    private synchronized void reset() {
        initialized = false;
        removeMonitors();
        items.clear();
    }

    /* Monitor-related */

    private void addMonitors() {
        if (xQueryMonitor != null) xQueryMonitor.addListener(xQueryMonitorListener);
        if (sourceMonitor != null) sourceMonitor.addListener(sourceMonitorListener);
    }

    private void removeMonitors() {
        if (xQueryMonitor != null) xQueryMonitor.removeListener(xQueryMonitorListener);
        if (sourceMonitor != null) sourceMonitor.removeListener(sourceMonitorListener);
    }

    private void refreshAuthorNodes() {
        WSAuthorEditorPage page = getAuthorPage();
        if (page != null) {
            EMSTContextualElement.refreshAuthorNodes(page, getContextualType());
        }
    }

    private EMSTFileMonitor xQueryMonitor;

    private FileListener xQueryMonitorListener =
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
                    refresh();
                    refreshAuthorNodes();
                }
            };

    private EMSTFileMonitor sourceMonitor;

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
                    refreshAuthorNodes();
                }
            };
}
