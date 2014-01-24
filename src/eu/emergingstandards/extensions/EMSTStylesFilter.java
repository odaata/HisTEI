package eu.emergingstandards.extensions;

import eu.emergingstandards.contextual_info.EMSTContextualInfo;
import ro.sync.ecss.css.EditorContent;
import ro.sync.ecss.css.StaticContent;
import ro.sync.ecss.css.Styles;
import ro.sync.ecss.extensions.api.StylesFilter;
import ro.sync.ecss.extensions.api.editor.InplaceEditorArgumentKeys;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 1/6/14.
 *
 * Implements custom styling for the EMST project
 * This is primarily useful for generating combo boxes from external contextual information XML files
 */
public class EMSTStylesFilter implements StylesFilter {

    @Override
    public Styles filter(Styles styles, AuthorNode authorNode) {
        EMSTContextualInfo contextualInfo = EMSTContextualInfo.get(authorNode);

        if (contextualInfo != null) {
            Map<String, Object> comboboxArgs = new HashMap<>();
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_TYPE, InplaceEditorArgumentKeys.TYPE_COMBOBOX);
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_EDIT, contextualInfo.getEditProperty(authorNode));
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_VALUES, contextualInfo.getOxygenValues());
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_LABELS, contextualInfo.getOxygenLabels());
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_EDITABLE, "false");
            comboboxArgs.put(InplaceEditorArgumentKeys.PROPERTY_FONT_INHERIT, "true");

            Map<String, Object> buttonArgs = new HashMap<>();
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_TYPE, InplaceEditorArgumentKeys.TYPE_BUTTON);
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_ACTION_ID, "edit.contextual.info");
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_TRANSPARENT, "true");
            buttonArgs.put(InplaceEditorArgumentKeys.PROPERTY_FONT_INHERIT, "true");

            StaticContent[] mixedContent = new StaticContent[]{new EditorContent(comboboxArgs), new EditorContent(buttonArgs)};
            styles.setProperty(Styles.KEY_MIXED_CONTENT, mixedContent);
//                  Collapse text so only combobox is used to edit field
            styles.setProperty(Styles.KEY_VISIBITY, "-oxy-collapse-text");
        }
        return styles;
    }

    @Override
    public String getDescription() {
        return "Emerging Standards Framework style filter.";
    }
}
