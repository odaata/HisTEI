package eu.emergingstandards.contextual_info;

import eu.emergingstandards.monitor.EMSTFileMonitor;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
    //  For querying the xml returned from the xquery
    private static final QName VALUE_QNAME = new QName("value");
    private static final QName LABEL_QNAME = new QName("label");

    private static final Map<EMSTContextualType, EMSTContextualInfo> infos = new EnumMap<>(EMSTContextualType.class);

    @Nullable
    public static EMSTContextualInfo get(EMSTContextualType type) {
        EMSTContextualInfo info = null;

        if (type != null) {
            info = infos.get(type);

            if (info == null) {
                info = new EMSTContextualInfo(type);
                infos.put(type, info);
            }
        }
        return info;
    }

    /* Instance members */

    private EMSTContextualType type;

    private WSAuthorEditorPage authorPage;
    private AuthorAccess authorAccess;

    private Path xQueryPath;
    private Path sourcePath;
    private XQueryEvaluator xQueryEvaluator;

    private List<String> values = new ArrayList<>();
    private List<String> labels = new ArrayList<>();

    protected EMSTContextualInfo(EMSTContextualType type) {
        this.type = type;
    }

    @NotNull
    public EMSTContextualType getType() {
        return type;
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

            if (xQueryPath != null && xQueryMonitor == null)
                xQueryMonitor = EMSTFileMonitor.add(xQueryPath);
        }
        return xQueryPath;
    }

    @Nullable
    public Path getSourcePath() {
        if (sourcePath == null) {
            sourcePath =
                    expandOxygenPath(
                            SOURCE_BASE_PATH + EMSTContextualSettings.get(getType()).getFileName(),
                            getAuthorAccess()
                    );

            if (sourcePath != null && sourceMonitor == null)
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
    public List<String> getValues() {
        if (values.isEmpty()) {
            refresh();
        }
        return values;
    }

    @NotNull
    public List<String> getLabels() {
        if (labels.isEmpty()) {
            refresh();
        }
        return labels;
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
                            String value = node.getAttributeValue(VALUE_QNAME);
                            values.add(getType().getKey() + ":" + value);

                            String label = node.getAttributeValue(LABEL_QNAME);
                            labels.add(label);
                        }
                        addMonitors();
                    } catch (SaxonApiException e) {
                        logger.error(e, e);
                    }
                }
            }
        }
    }

    private synchronized void reset() {
        removeMonitors();
        values.clear();
        labels.clear();
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
            EMSTContextualElement.refreshAuthorNodes(page, getType());
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
