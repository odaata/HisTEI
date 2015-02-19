package info.histei;

import info.histei.extensions.HTAuthorExtensionStateListener;
import info.histei.extensions.HTNodeRendererCustomizer;
import info.histei.extensions.HTSchemaManagerFilter;
import info.histei.extensions.HTStylesFilter;
import ro.sync.contentcompletion.xml.SchemaManagerFilter;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;
import ro.sync.exml.workspace.api.node.customizer.XMLNodeRendererCustomizer;

import static info.histei.utils.OxygenUtils.refreshCurrentPage;

/**
 * Created by mike on 12/28/13.
 */
public class HTExtensionsBundle extends ExtensionsBundle {

    private HTAuthorExtensionStateListener authorExtensionStateListener;

    @Override
    public String getDocumentTypeID() {
        return "HisTEI.document.type";
    }

    @Override
    public String getDescription() {
        return "Custom extensions bundle for HisTEI";
    }

    @Override
    public AuthorExtensionStateListener createAuthorExtensionStateListener() {
        authorExtensionStateListener = new HTAuthorExtensionStateListener();

        return authorExtensionStateListener;
    }

    @Override
    public StylesFilter createAuthorStylesFilter() {
        return new HTStylesFilter();
    }

    @Override
    public SchemaManagerFilter createSchemaManagerFilter() {
        refreshCurrentPage();
        return new HTSchemaManagerFilter();
    }

    @Override
    public XMLNodeRendererCustomizer createXMLNodeCustomizer() {
        return new HTNodeRendererCustomizer();
    }

    @Override
    public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
        return authorExtensionStateListener;
    }

    @Override
    public ClipboardFragmentProcessor getClipboardFragmentProcessor() {
        return authorExtensionStateListener;
    }
}

