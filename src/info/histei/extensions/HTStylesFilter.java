package info.histei.extensions;

import info.histei.contextual_info.ContextualStyledList;
import info.histei.utils.MainUtils;
import info.histei.utils.OxygenUtils;
import info.histei.utils.XMLUtils;
import ro.sync.ecss.css.EditorContent;
import ro.sync.ecss.css.LabelContent;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.editor.InplaceEditorArgumentKeys;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.HashMap;
import java.util.Map;

import static info.histei.commons.TEINamespace.FACSIMILE_ELEMENT_NAME;

/**
 * Created by mike on 1/6/14.
 * <p/>
 * Implements custom styling for the HisTEI project
 * This is primarily useful for generating combo boxes from external contextual information XML files
 */
public class HTStylesFilter implements StylesFilter {

    private static final String CONTEXTUAL_INFO_ACTION_ID = "contextual.info.edit";

    @Override
    public Styles filter(Styles styles, AuthorNode authorNode) {
        if (!styles.isInline()) {
            addContextualStyledList(styles, authorNode);

            addFacsimileElement(styles, authorNode);
        }
        return styles;
    }

    /*
    * Add the contextual element if the current Authornode represents a contextual reference
    * */
    private void addContextualStyledList(Styles styles, AuthorNode authorNode) {
        ContextualStyledList contextualStyledList = ContextualStyledList.get(authorNode);
        if (contextualStyledList != null) {
            Map<String, Object> comboboxArgs = new HashMap<>();

            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_TYPE, InplaceEditorArgumentKeys.TYPE_COMBOBOX);
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_EDIT_QUALIFIED, contextualStyledList.getEditPropertyQualified());
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_VALUES, contextualStyledList.getOxygenValues());
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_LABELS, contextualStyledList.getOxygenLabels());
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_TOOLTIPS, contextualStyledList.getOxygenTooltips());

            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_EDITABLE, "false");
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_FONT_INHERIT, "true");

            Map<String, Object> buttonArgs = new HashMap<>();
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_TYPE, InplaceEditorArgumentKeys.TYPE_BUTTON);
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_ACTION_ID, CONTEXTUAL_INFO_ACTION_ID);
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_TRANSPARENT, "true");
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_FONT_INHERIT, "true");
//          Set showText and showIcon to true so the buttons render on same level as other components
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_SHOW_TEXT, "true");
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_SHOW_ICON, "true");

            StaticContent[] mixedContent = new StaticContent[]{new EditorContent(comboboxArgs), new EditorContent(buttonArgs)};
            styles.setProperty(Styles.KEY_MIXED_CONTENT, mixedContent);
        }
    }

    /*
    *   Adds a label for the xml:base attribute - only way to decode the URL for human consumption is via Java
    *       so this function just adds a label with the url decoded
    * */
    private void addFacsimileElement(Styles styles, AuthorNode authorNode) {
        if (FACSIMILE_ELEMENT_NAME.equals(authorNode.getName())) {
            AuthorElement authorElement = OxygenUtils.castAuthorElement(authorNode);

            if (authorElement != null) {
                String xmlBase = OxygenUtils.getAttrValue(authorElement.getAttribute(XMLUtils.XML_BASE_ATTRIB_NAME));
                if (xmlBase != null) {
                    Map<String, Object> labelProps = new HashMap<>();

                    labelProps.put("text", MainUtils.decodeURL(xmlBase));
                    labelProps.put("styles", "* { font-weight:normal; }");
                    StaticContent[] mixedContent = new StaticContent[]{new LabelContent(labelProps)};

                    styles.setProperty(Styles.KEY_MIXED_CONTENT, mixedContent);
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Emerging Standards Framework styles filter.";
    }
}
