package eu.emergingstandards;

import eu.emergingstandards.extensions.EMSTStylesFilter;
import eu.emergingstandards.id.EMSTUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;

/**
 * Created by mike on 12/28/13.
 */
public class EMSTExtensionsBundle extends ExtensionsBundle {

//    private static Logger logger = Logger.getLogger(EMSTExtensionsBundle.class.getName());

    private EMSTUniqueAttributesRecognizer uniqueAttributesRecognizer;

    @Override
    public String getDocumentTypeID() {
        return "EMST.document.type";
    }

    public String getDescription() {
        return "Custom extensions bundle for the Emerging Standards Project";
    }

    @Override
    public AuthorExtensionStateListener createAuthorExtensionStateListener() {
        uniqueAttributesRecognizer = new EMSTUniqueAttributesRecognizer();
        return uniqueAttributesRecognizer;
    }

    @Override
    public StylesFilter createAuthorStylesFilter() {
        return new EMSTStylesFilter();
    }

    @Override
    public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
        return uniqueAttributesRecognizer;
    }

    @Override
    public ClipboardFragmentProcessor getClipboardFragmentProcessor() {
        return uniqueAttributesRecognizer;
    }

    /**
     * Expand content references.
     */
    /*@Override
    public AuthorReferenceResolver createAuthorReferenceResolver() {
        return new EMSTReferenceResolver();
    }*/
}
