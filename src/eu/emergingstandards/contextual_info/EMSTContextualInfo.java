package eu.emergingstandards.contextual_info;

import eu.emergingstandards.utils.EMSTUtils;
import eu.emergingstandards.watcher.EMSTFileWatcher;
import eu.emergingstandards.watcher.EMSTWatchEvent;
import eu.emergingstandards.watcher.EMSTWatchEventType;
import eu.emergingstandards.watcher.EMSTWatcherListener;
import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.s9api.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.xml.sax.InputSource;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.exml.workspace.api.editor.page.author.WSAuthorEditorPage;
import ro.sync.util.editorvars.EditorVariables;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mike on 1/10/14.
 */
public class EMSTContextualInfo {

    /* Global (Static) members */

    private static Logger logger = Logger.getLogger(EMSTContextualInfo.class.getName());
//  Base strings for Paths
    public static final String SOURCE_BASE_PATH = EditorVariables.PROJECT_DIRECTORY + "/contextual_info/";
    public static final String XQUERY_BASE_PATH = EditorVariables.FRAMEWORK_DIRECTORY + "/resources";
    public static final String XQUERY_PATH = XQUERY_BASE_PATH + "/contextual_info.xql";
//  For querying the xml returned from the xquery
    private static final QName VALUE_QNAME = new QName("value");
    private static final QName LABEL_QNAME = new QName("label");

//  Main settings
    public static final Map<String, String> TYPES = new HashMap<>();

    static {
        TYPES.put("psn", "person.xml");
        TYPES.put("plc", "place.xml");
//        TYPES.put("org", "organization.xml");
//        TYPES.put("bib", "bibliography.xml");
//        TYPES.put("evt", "event.xml");
    }

//  Regexp info for retrieving IDs
    private static final String TYPE_REGEX = "(" + StringUtils.join(TYPES.keySet(), "|") + ")";
//    private static final String ID_REGEX = "(\\w+_[-0-9a-f]{32,36})";
    private static final String ID_REGEX = "(\\S+)";
    private static final Pattern REF_PATTERN = Pattern.compile(TYPE_REGEX + ":" + ID_REGEX);

    public static final Map<String, Map<String, String>> ELEMENTS = new HashMap<>();
    static {
//        persName element
        Map<String, String> initMap = new HashMap<>();
        initMap.put("type", "psn");
        initMap.put("refAttributeName", "ref");
        initMap.put("sourceParent", "person");
        ELEMENTS.put("persName", new HashMap<>(initMap));

//        handNote element
        initMap = new HashMap<>();
        initMap.put("type", "psn");
        initMap.put("refAttributeName", "scribeRef");
        initMap.put("sourceParent", "person");
        ELEMENTS.put("handNote", new HashMap<>(initMap));

//        placeName element
        initMap = new HashMap<>();
        initMap.put("type", "plc");
        initMap.put("refAttributeName", "ref");
        initMap.put("sourceParent", "place");
        ELEMENTS.put("placeName", new HashMap<>(initMap));

/*  No support for organizations yet!
//        orgName element
        initMap = new HashMap<>();
        initMap.put("type", "org");
        initMap.put("refAttributeName", "ref");
        initMap.put("sourceParent", "org");
        ELEMENTS.put("orgName", new HashMap<>(initMap));

//        repository element
        initMap = new HashMap<>();
        initMap.put("type", "org");
        initMap.put("refAttributeName", "ref");
        initMap.put("sourceParent", "org");
        ELEMENTS.put("repository", new HashMap<>(initMap));*/
    }

    private static Map<String, EMSTContextualInfo> contextualInfos = new HashMap<>();

    @Nullable
    public static EMSTContextualInfo get(AuthorNode authorNode) {
        EMSTContextualInfo info = null;
        Map<String, String> elementProperties = EMSTContextualInfo.ELEMENTS.get(authorNode.getName());

        if (elementProperties != null) {
            AuthorNode parent = authorNode.getParent();
            if (parent == null || ! elementProperties.get("sourceParent").equals(parent.getName())) {

                String type = elementProperties.get("type");
                info = contextualInfos.get(type);

                if (info == null) {
                    if (TYPES.get(type) != null) {
                        info = new EMSTContextualInfo(type, authorNode);
                        contextualInfos.put(type, info);
                    }
                }
            }
        }
        return info;
    }

    /* Instance members */

    private WSAuthorEditorPage authorPage;
    private AuthorAccess authorAccess;
    private Map<AuthorNode, String> authorNodes = new WeakHashMap<>();

    private String type;
    private Path xQueryPath;
    private Path sourcePath;
    private XQueryEvaluator xQueryEvaluator;

    private List<String> values;
    private List<String> labels;

    protected EMSTContextualInfo(String type, AuthorNode authorNode) {
        this.type = type;
        this.authorNodes.put(authorNode, type);
    }

    @Nullable
    public WSAuthorEditorPage getAuthorPage() {
        if (authorPage == null) {
            authorPage = EMSTUtils.getCurrentAuthorEditorPage();
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

    @NotNull
    public String getType() {
        return type;
    }

    @Nullable
    public Path getXQueryPath() {
        if (xQueryPath == null) {
            xQueryPath =
                    EMSTUtils.expandOxygenPath(XQUERY_PATH, getAuthorAccess());
        }
        return xQueryPath;
    }

    @Nullable
    public Path getSourcePath() {
        if (sourcePath == null) {
            sourcePath =
                    EMSTUtils.expandOxygenPath(SOURCE_BASE_PATH + TYPES.get(getType()), getAuthorAccess());
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

    @Nullable
    public List<String> getValues() {
        if (values == null) {
            refresh();
        }
        return values;
    }

    @Nullable
    public List<String> getLabels() {
        if (labels == null) {
            refresh();
        }
        return labels;
    }

    @Nullable
    public Map<String, String> getElementProperties(AuthorNode authorNode) {
        if (authorNode != null)
            return EMSTContextualInfo.ELEMENTS.get(authorNode.getName());
        else
            return null;
    }

    @NotNull
    public String getRefAttributeName(AuthorNode authorNode) {
        String refAttributeName = "";

        Map<String, String> props = getElementProperties(authorNode);
        if (props != null) refAttributeName = props.get("refAttributeName");

        return refAttributeName;
    }

    @NotNull
    public String getID(AuthorNode authorNode) {
        String id = "";
        String refAttributeName = getRefAttributeName(authorNode);

        if (! refAttributeName.isEmpty()) {
            if (authorNode.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
                AuthorElement element = (AuthorElement) authorNode;
                AttrValue refAttribute = element.getAttribute(refAttributeName);
                if (refAttribute != null) {
                    String value = refAttribute.getValue();
                    if (value != null) {
                        Matcher matcher = REF_PATTERN.matcher(value);
                        if (matcher.matches()) {
                            id = matcher.group(2);
                        }
                    }
                }
            }
        }
        return id;
    }

    @Nullable
    public URL getURL(AuthorNode authorNode) {
        if (authorNode == null) return null;

        URL url = null;

        Path srcPath = getSourcePath();
        if (srcPath != null) {
            try {
                url = srcPath.toUri().toURL();

                String id = getID(authorNode);
                if (! id.isEmpty())
                    url = new URL(srcPath.toUri().toURL(), "#" + id);

            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @NotNull
    public String getEditProperty(AuthorNode authorNode) {
        String editProperty = "";

        String refAttributeName = getRefAttributeName(authorNode);
        if (! refAttributeName.isEmpty()) editProperty = "@" + refAttributeName;

        return editProperty;
    }

    @NotNull
    public String getOxygenValues() {
        String oxygenValues = "";

        List<String> values = getValues();
        if (values != null)
            oxygenValues = StringUtils.join(values, ",");

        return oxygenValues;
    }

    @NotNull
    public String getOxygenLabels() {
        String oxygenLabels = "";

        List<String> labels = getLabels();
        if (labels != null)
            oxygenLabels = StringUtils.join(EMSTUtils.escapeCommas(labels), ",");

        return oxygenLabels;
    }

    public void reload() {
        Path xqy = getXQueryPath();
        if (xqy != null) {
            reset();

            xQueryEvaluator = null;
            try {
                Processor proc = new Processor(true);
                proc.setConfigurationProperty(FeatureKeys.XQUERY_VERSION, "3.0");
                xQueryEvaluator = proc.newXQueryCompiler().compile(xqy.toFile()).load();
                refresh();
            } catch (SaxonApiException | IOException e) {
                xQueryEvaluator = null;
                logger.error(e, e);
            }
        }
    }

    public void refresh() {
        Path src = getSourcePath();
        if (src != null) {
            XQueryEvaluator xqe = getXQueryEvaluator();
            if (xqe != null) {
                reset();

                try {
                    xqe.setSource(new SAXSource(new InputSource(src.toUri().toString())));
                    for (XdmItem item : xqe) {
                        XdmNode node = (XdmNode) item;
                        String value = node.getAttributeValue(VALUE_QNAME);
                        values.add(getType() + ":" + value);

                        String label = node.getAttributeValue(LABEL_QNAME);
                        labels.add(label);
                    }
                    addWatchers();
                } catch (SaxonApiException e) {
                    logger.error(e, e);
                }
            }
        }
    }

    private void reset() {
        removeWatchers();
        values = new ArrayList<>();
        labels = new ArrayList<>();
    }

    /* Event-related */

    private EMSTFileWatcher xQueryWatcher;
    private EMSTFileWatcher sourceWatcher;
    private EMSTWatcherListener xQueryWatcherListener =
            new EMSTWatcherListener() {
                @Override
                public void fileChanged(EMSTWatchEvent watchEvent) {
                    if (watchEvent.getType() == EMSTWatchEventType.FILE_DELETED)
                        xQueryPath = null;

                    reload();
                    refreshAuthorNodes();
                }
            };
    private EMSTWatcherListener sourceWatcherListener =
            new EMSTWatcherListener() {
                @Override
                public void fileChanged(EMSTWatchEvent watchEvent) {
                    if (watchEvent.getType() == EMSTWatchEventType.FILE_DELETED)
                        sourcePath = null;

                    refresh();
                    refreshAuthorNodes();
                }
            };

    private void addWatchers() {
        xQueryWatcher = EMSTFileWatcher.get(getXQueryPath());
        sourceWatcher = EMSTFileWatcher.get(getSourcePath());

        xQueryWatcher.addEventListener(xQueryWatcherListener);
        sourceWatcher.addEventListener(sourceWatcherListener);
    }

    private void removeWatchers() {
        if (xQueryWatcher != null) {
            xQueryWatcher.removeEventListener(xQueryWatcherListener);
            xQueryWatcher = null;
        }
        if (sourceWatcher != null) {
            sourceWatcher.removeEventListener(sourceWatcherListener);
            sourceWatcher = null;
        }
    }

    private void refreshAuthorNodes() {
        WSAuthorEditorPage page = getAuthorPage();
        if (page != null) {
            for (AuthorNode authorNode : authorNodes.keySet()) {
                page.refresh(authorNode);
            }
        }
    }
}
