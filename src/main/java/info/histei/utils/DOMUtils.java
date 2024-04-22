package info.histei.utils;

import info.histei.commons.NamespaceType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

/**
 * Created by mike on 2/8/14.
 * <p/>
 * This is a code graveyard. Leftovers from playing around with the Java DOM stuff
 * It was insanely complex and in the end I just want to generate default elements
 * with some attributes and a namespace tossed in, so I gave up on it for
 * (don't tell anyone) straight up string processing that produces XML
 * <p/>
 * It's the late 90's all over again! Yippeee!!!!
 */
public class DOMUtils {

    private static final Logger logger = LogManager.getLogger(DOMUtils.class.getName());

    private static DocumentBuilder documentBuilder;
    private static Document defaultDocument;

    @Nullable
    public static DocumentBuilder getDocumentBuilder() {
        if (documentBuilder == null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//          Default Settings for HisTEI Project - the most "content-focused" settings
            dbf.setNamespaceAware(true);
            dbf.setCoalescing(true);
            dbf.setExpandEntityReferences(true);
            dbf.setIgnoringComments(true);
            dbf.setIgnoringElementContentWhitespace(true);

            try {
                documentBuilder = dbf.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                logger.error(e, e);
            }
        }
        return documentBuilder;
    }

    @Nullable
    public static Transformer getTransformer() {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;

        try {
            transformer = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            logger.error(e, e);
        }
        return transformer;
    }

    @Nullable
    public static Document newDocument() {
        if (defaultDocument == null) {
            DocumentBuilder builder = getDocumentBuilder();
            if (builder != null) {
                defaultDocument = builder.newDocument();
            }
        }
        return defaultDocument;
    }

    @Nullable
    public static Element createElement(Document document, String namespace, String elementName) {
        Element element = null;
        Document doc = document == null ? newDocument() : document;

        if (doc != null && namespace != null && elementName != null) {
            element = doc.createElementNS(namespace, elementName);
        }
        return element;
    }

    @Nullable
    public static Element createElement(String namespace, String elementName) {
        return createElement(null, namespace, elementName);
    }

    @Nullable
    public static Element createElement(String elementName) {
        return createElement(NamespaceType.TEI.getURLID(), elementName);
    }

    @Nullable
    public static Element createElement(Document document, String elementName) {
        return createElement(document, NamespaceType.TEI.getURLID(), elementName);
    }
}
