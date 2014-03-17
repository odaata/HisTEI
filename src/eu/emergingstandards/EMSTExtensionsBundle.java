package eu.emergingstandards;

import eu.emergingstandards.extensions.EMSTAuthorExtensionStateListener;
import eu.emergingstandards.extensions.EMSTNodeRendererCustomizer;
import eu.emergingstandards.extensions.EMSTSchemaManagerFilter;
import eu.emergingstandards.extensions.EMSTStylesFilter;
import ro.sync.contentcompletion.xml.SchemaManagerFilter;
import ro.sync.ecss.extensions.api.AuthorExtensionStateListener;
import ro.sync.ecss.extensions.api.ExtensionsBundle;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.content.ClipboardFragmentProcessor;
import ro.sync.exml.workspace.api.node.customizer.XMLNodeRendererCustomizer;

import static eu.emergingstandards.utils.EMSTOxygenUtils.refreshCurrentPage;

/**
 * Created by mike on 12/28/13.
 */
public class EMSTExtensionsBundle extends ExtensionsBundle {

//    private static Logger logger = Logger.getLogger(EMSTExtensionsBundle.class.getName());

    private EMSTAuthorExtensionStateListener authorExtensionStateListener;
    private EMSTSchemaManagerFilter schemaManagerFilter;

    @Override
    public String getDocumentTypeID() {
        return "EMST.document.type";
    }

    @Override
    public String getDescription() {
        return "Custom extensions bundle for the Emerging Standards Project";
    }

    @Override
    public AuthorExtensionStateListener createAuthorExtensionStateListener() {
        authorExtensionStateListener = new EMSTAuthorExtensionStateListener();

        return authorExtensionStateListener;
    }

    @Override
    public StylesFilter createAuthorStylesFilter() {
        return new EMSTStylesFilter();
    }

    @Override
    public SchemaManagerFilter createSchemaManagerFilter() {
        refreshCurrentPage();
        return new EMSTSchemaManagerFilter();
    }

    @Override
    public XMLNodeRendererCustomizer createXMLNodeCustomizer() {
        return new EMSTNodeRendererCustomizer();
    }

    @Override
    public UniqueAttributesRecognizer getUniqueAttributesIdentifier() {
        return authorExtensionStateListener;
    }

    @Override
    public ClipboardFragmentProcessor getClipboardFragmentProcessor() {
        return authorExtensionStateListener;
    }

    /* Thought the customHref method would reolve the link Hrefs as specified in the CSS
    *   Alas, it only resolves things passed into the url() css function which is what I use to render the link image :-( */

    /*@Override
    public URL resolveCustomHref(URL currentEditorURL, AuthorNode authorNode,
                                 String linkHref, AuthorAccess authorAccess) throws CustomResolverException, IOException {

        if (authorNode.getType() == AuthorNode.NODE_TYPE_PSEUDO_ELEMENT) {
            authorNode = authorNode.getParent();
        }

        EMSTContextualStyledList contextualElement = EMSTContextualStyledList.get(authorNode);
        if (contextualElement != null) {
            return contextualElement.getURL();
        } else {
            return null;
        }
    }*/

    /*@Override
    public AuthorReferenceResolver createAuthorReferenceResolver() {
        return new EMSTReferenceResolver();
    }*/

//  Disabling ccfilter since categories still need a uri and the private scheme is easiest to use

//  This was sent to me via e-mail from Alex at Oxygen (see e-mail below)
//      It inserts values into the schema based on node/attrib name values
//      Configuration is under ${framework}/resources/ccfilter
   /* @Override
    public SchemaManagerFilter createSchemaManagerFilter() {
        return new ConfigurationSchemaManagerFilter();
    }*/

    /*
    * E-mail from Alex at Oxygen explaining how to use the content completion filter - aka ConfigurationSchemaManagerFilter
    *
Hello,

First just a short presentation about what this configuration support does. Basically it offers the possibility to
contribute to Oxygen content completion by writing an XML configuration file. In this file you can give values for
attributes or elements content. These values can either be statically specified in the file or can be obtain by
executing a given XSLT file. We'll get into details later on when I will present all the resources from the package.

I have attached an archive with the required resources. Here is how to install it:
1. Unzip customization_layer.zip inside {oxygenInstallDir}/frameworks/dita
2. Start Oxygen (version 15.0 or newer), go to Preferences, Document Type Associations and edit the DITA framework
3. Go on the Classpath tab and add these two entries:
${framework}/customization_layer/ccfilter/
${framework}/customization_layer/ccfilter/ccfilter.jar
4. Go on the Extensions tab and click "Choose" for the "Content completion filter". Select the schema
ConfigurationSchemaManagerFilter entry.

To check if it works open the attached prodname.dita and invoke content completion inside "prodname" element.
It should present two values (Car and Bus) given by the configuration file.

Please note that these samples were initially created for a TEI vocabulary but they provide a good
stating point to create some for DITA. I've change them a bit to provide support for the "prodname" element
so that you can see the filter contributing.

Here is a short description of what the "customization_layer" folder contains:
- ccfilter.jar contains the JAVA implementation for the content completion filter. We indent to bundle this code
into the future version of Oxygen but for now you have to add this jar manually in the framework Classpath.
- cc_value_config.xml is the configuration file. Here we specify patterns for element names and attributes
for which we want to offer values. There is also a schema for this format: configurationCC.xsd.
Basically you can specify values for an element content or for an attribute and you can either
contribute to the ones detected by Oxygen or you can replace them with your own. These values can be given statically :

    <match elementName="prodname">
        <items>
            <item value="Car"/>
            <item value="Bus"/>
        </items>
    </match>

or can be obtained by executing an XSLT:

    <match elementName="writing" elementNS="http://www.tei-c.org/ns/1.0" attrName="who">
        <xslt href="../xsl/get_values_from_db.xsl" useCache="false"  action="replace"/>
    </match>

- the stylesheets used in cc_value_config.xml can be found in the "xsl" folder. They provide a good starting point
for building your own. "get_values_from_db.xsl" takes the values from an eXist database while "get_speakers.xsl"
takes the values directly from the edited document.

Please let me know if you have any troubles installing the support or if there is anything else I can help you with.

Best regards,
Alex
    *
    * */
}

