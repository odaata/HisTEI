package eu.emergingstandards;

import eu.emergingstandards.extensions.EMSTStylesFilter;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.StylesFilter;

/**
 * Created by mike on 12/28/13.
 */
public class EMSTExtensionsBundle extends ExtensionsBundle {

//    private static Logger logger = Logger.getLogger(EMSTExtensionsBundle.class.getName());

    /**
     * The unique identifier of the Document Type.
     * This identifier will be used to store custom SDF options.
     */
    @Override
    public String getDocumentTypeID() {
        return "EMST.document.type";
    }

    /**
     * Bundle description.
     */
    public String getDescription() {
        return "Custom extensions bundle for the Emerging Standards Project";
    }

    @Override
    public StylesFilter createAuthorStylesFilter() {
        return new EMSTStylesFilter();
    }

    /**
     * Expand content references.
     */
    /*@Override
    public AuthorReferenceResolver createAuthorReferenceResolver() {
        return new EMSTReferenceResolver();
    }*/
}
