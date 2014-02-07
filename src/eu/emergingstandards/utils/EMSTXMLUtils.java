package eu.emergingstandards.utils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by mike on 2/6/14.
 */
public class EMSTXMLUtils {

    private static final Logger logger = Logger.getLogger(EMSTXMLUtils.class.getName());

    public static final String XML_ID_ATTRIB_NAME = "xml:id";
    public static final String XML_BASE_ATTRIB_NAME = "xml:base";

    private static DocumentBuilder documentBuilder;
    private static Document defaultDocument;

    @Nullable
    public static DocumentBuilder getDocumentBuilder() {
        if (documentBuilder == null) {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//          Default Settings for EMST Project - the most "content-focused" settings
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
    public static Element createElement(String namespace, String elementName) {
        Element element = null;
        Document doc = newDocument();

        if (doc != null) {
            element = doc.createElementNS(namespace, elementName);
        }
        return element;
    }

    @Nullable
    public static Element createElement(String elementName) {
        return createElement(EMSTNamespaceType.TEI.getURLID(), elementName);
    }

    private EMSTXMLUtils() {
    }
}
