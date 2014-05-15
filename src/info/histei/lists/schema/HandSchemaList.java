package info.histei.lists.schema;

import info.histei.commons.TEINamespace;
import info.histei.utils.MainUtils;
import info.histei.utils.OxygenUtils;
import info.histei.utils.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import javax.swing.text.BadLocationException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mike on 3/10/14.
 */
public class HandSchemaList extends AbstractReferenceSchemaList<SchemaListItem> {

    public static final List<SchemaListAttribute> ATTRIBUTES = Arrays.asList(
            new SchemaListAttribute(TEINamespace.HAND_ATTRIB_NAME),
            new SchemaListAttribute(TEINamespace.NEW_ATTRIB_NAME, TEINamespace.HAND_SHIFT_ELEMENT_NAME)
    );

    private AuthorElement handNotesElement;

    protected HandSchemaList(AuthorAccess authorAccess) {
        super(authorAccess);
    }

    @Nullable
    public AuthorElement getHandNotesElement() {
        if (handNotesElement == null) {
            handNotesElement = OxygenUtils.getAuthorElement("//teiHeader/profileDesc/handNotes[1]", authorAccess);
        }
        return handNotesElement;
    }

    @NotNull
    @Override
    public List<SchemaListAttribute> getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public boolean isEdited(AuthorNode editedNode) {
        String name = editedNode.getName();
        return (TEINamespace.HAND_NOTES_ELEMENT_NAME.equals(name) ||
                TEINamespace.HAND_NOTE_ELEMENT_NAME.equals(name));
    }

    @Override
    public synchronized void refresh() {
        List<SchemaListItem> items = getItems();
        List<AuthorElement> handNotes = OxygenUtils.getContentElements(getHandNotesElement());

        for (AuthorElement handNote : handNotes) {
            String id = OxygenUtils.getAttrValue(handNote.getAttribute(XMLUtils.XML_ID_ATTRIB_NAME));
            if (id != null) id = "#" + id;

            String tooltip = null;
            try {
                tooltip = StringUtils.trimToNull(handNote.getTextContent());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            SchemaListItem listItem = SchemaListItem.get(id, tooltip);
            if (listItem != null) {
                items.add(listItem);
            }
        }
    }

    @Override
    public synchronized void reset() {
        super.reset();
        handNotesElement = null;
    }
}
