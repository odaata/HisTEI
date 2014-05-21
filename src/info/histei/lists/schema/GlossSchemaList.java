package info.histei.lists.schema;

import info.histei.commons.TEINamespace;
import info.histei.utils.OxygenUtils;
import info.histei.utils.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import javax.swing.text.BadLocationException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 3/10/14.
 */
public class GlossSchemaList extends AbstractReferenceSchemaList<SchemaListItem> {

    public static final List<SchemaListAttribute> ATTRIBUTES =
            Arrays.asList(new SchemaListAttribute(TEINamespace.TARGET_ATTRIB_NAME, TEINamespace.GLOSS_ELEMENT_NAME));

    protected GlossSchemaList(AuthorAccess authorAccess) {
        super(authorAccess);
    }

    @NotNull
    @Override
    public List<SchemaListAttribute> getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public boolean isEdited(AuthorNode editedNode) {
        String name = editedNode.getName();
        return (TEINamespace.AB_ELEMENT_NAME.equals(name) ||
                TEINamespace.SEG_ELEMENT_NAME.equals(name) ||
                TEINamespace.TERM_ELEMENT_NAME.equals(name));
    }

    @Override
    public synchronized void refresh() {
        List<SchemaListItem> items = getItems();
        List<AuthorElement> targets = OxygenUtils.getAuthorElements("//term | //ab | //seg[empty(@function)]", getAuthorAccess());

        for (AuthorElement target : targets) {
            String id = OxygenUtils.getAttrValue(target.getAttribute(XMLUtils.XML_ID_ATTRIB_NAME));
            if (id != null) id = "#" + id;

            String tooltip = null;
            try {
                tooltip = StringUtils.trimToNull(target.getTextContent());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            SchemaListItem listItem = SchemaListItem.get(id, tooltip);
            if (listItem != null) {
                items.add(listItem);
            }
        }
    }
}
