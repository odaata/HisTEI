package eu.emergingstandards.extensions;

import eu.emergingstandards.utils.OxygenUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorReferenceResolver;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;
import ro.sync.util.editorvars.EditorVariables;

import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static com.sun.deploy.util.StringUtils.join;

/**
 * Created by mike on 12/26/13.
 * <p/>
 * Resolves references to contextual information using the EMST Private URI Scheme.
 */
public class EMSTReferenceResolver implements AuthorReferenceResolver {

    /**
     * Logger for logging.
     */
    private static final Logger logger = Logger.getLogger(EMSTReferenceResolver.class.getName());

    /**
     * Verifies if the handler considers the node to have references.
     *
     * @param node The node to be analyzed.
     * @return <code>true</code> if it is has references.
     */
    @Override
    public boolean hasReferences(AuthorNode node) {
        String ref = getReferenceUniqueID(node);
        return ref != null;
    }

    /**
     * Verifies if the references of the given node must be refreshed
     * when the attribute with the specified name has changed.
     *
     * @param node          The node with the references.
     * @param attributeName The name of the changed attribute.
     * @return <code>true</code> if the references must be refreshed.
     */
    @Override
    public boolean isReferenceChanged(AuthorNode node, String attributeName) {
        return REF_ATTRIB.equals(attributeName);
    }

    /**
     * Resolve the references of the node.
     * <p/>
     * The returning SAXSource will be used for creating the referred content
     * using the parser and source inside it.
     *
     * @param node           The clone of the node.
     * @param systemID       The system ID of the node with references.
     * @param authorAccess   The author access implementation.
     * @param entityResolver The entity resolver that can be used to resolve:
     *                       <p/>
     *                       <ul>
     *                       <li>Resources that are already opened in editor.
     *                       For this case the InputSource will contains the editor content.</li>
     *                       <li>Resources resolved through XML catalog.</li>
     *                       </ul>
     * @return The SAX source including the parser and the parser's input source.
     */
    @Override
    public SAXSource resolveReference(
            AuthorNode node,
            String systemID,
            AuthorAccess authorAccess,
            EntityResolver entityResolver) {
        SAXSource saxSource = null;

        String path = getReferenceSystemID(node, authorAccess);
        if (path != null) {
//            try {
                /*InputSource inputSource = entityResolver.resolveEntity(null, path);
                if(inputSource == null) {
                    inputSource = new InputSource(path);
                }*/

            InputSource inputSource = new InputSource(new StringReader("<place>Test</place>"));
            XMLReader xmlReader = authorAccess.getXMLUtilAccess().newNonValidatingXMLReader();
            xmlReader.setEntityResolver(entityResolver);
            saxSource = new SAXSource(xmlReader, inputSource);

            logger.debug("resolveReference: " + path);
         /*   } catch (SAXException | IOException e) {
                logger.error(e, e);
            }*/
        }
        return saxSource;
    }

    /**
     * Returns the name of the node that contains the expanded referred content.
     *
     * @param node The node that contains references.
     * @return The display name of the node.
     */
    @Override
    public String getDisplayName(AuthorNode node) {
        String id = getReferenceUniqueID(node);

        logger.debug("getDisplayName: " + id);

        return id;
    }

    /**
     * Get an unique identifier for the node reference.<br/>
     * For the EMST Project, this is the {@code ref} attribute.
     * <p/>
     * The unique identifier is used to avoid resolving the references
     * recursively.
     *
     * @param node The node that has reference.
     * @return An unique identifier for the reference node.
     */
    @Override
    public String getReferenceUniqueID(AuthorNode node) {
        String id = null;

        HashMap<String, String> refMap = getReference(node);
        if (refMap != null) {
            AuthorElement element = (AuthorElement) node;
            id = element.getAttribute(REF_ATTRIB).getValue();
//            id = StringUtils.join(refMap.values(), ":");

            logger.debug("getReferenceUniqueID: " + id);
        }
        return id;
    }

    /**
     * Return the systemID of the referred content.
     *
     * @param node         The reference node.
     * @param authorAccess The author access.
     * @return The systemID of the referred content.
     */
    @Override
    public String getReferenceSystemID(AuthorNode node,
                                       AuthorAccess authorAccess) {
        String systemID = null;

        HashMap<String, String> refMap = getReference(node);
        if (refMap != null) {
            systemID = authorAccess.getUtilAccess().expandEditorVariables(
                    BASE_PATH + TYPES.get(refMap.get("type")) + "#" + refMap.get("id"),
                    authorAccess.getEditorAccess().getEditorLocation());

            logger.debug("getReferenceSystemID: " + systemID);
        }
        return systemID;
    }

    @Override
    public String getDescription() {
        return "Resolves references for the EMST project using the project's Private URI Scheme.";
    }

    private static final String REF_ATTRIB = "ref";
    private static final String BASE_PATH = "file://" +
            EditorVariables.PROJECT_DIRECTORY + "/contextual_info/";

    private static final Map<String, String> TYPES = new HashMap<>();

    static {
        TYPES.put("org", "organization.xml");
        TYPES.put("plc", "place.xml");
        TYPES.put("psn", "person.xml");
//        TYPES.put("bib", "bibliography.xml");
//        TYPES.put("evt", "event.xml");
    }

    private static final String TYPE_PATTERN = "(" + StringUtils.join(TYPES.keySet(), "|") + ")";
    private static final String ID_PATTERN = "(\\w+_[-0-9a-f]{32,36})";
    private static final Pattern REF_PATTERN = Pattern.compile(TYPE_PATTERN + ":" + ID_PATTERN);

    private HashMap<String, String> getReference(AuthorNode node) {
        HashMap<String, String> refMap = null;

        AuthorElement element = OxygenUtils.castAuthorElement(node);
        if (element != null) {
            String ref = OxygenUtils.getAttrValue(element.getAttribute(REF_ATTRIB));
            if (ref != null) {
                Matcher matcher = REF_PATTERN.matcher(ref);
                boolean found = matcher.matches();

                if (found) {
                    refMap = new HashMap<>();
                    refMap.put("type", matcher.group(1));
                    refMap.put("id", matcher.group(2));

                    logger.debug("getReference: " + refMap.toString());
                }
            }
        }
        return refMap;
    }

}
