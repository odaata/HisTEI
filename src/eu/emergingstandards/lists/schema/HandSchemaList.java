package eu.emergingstandards.lists.schema;

import eu.emergingstandards.utils.MainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import javax.swing.text.BadLocationException;
import java.util.Arrays;
import java.util.List;

import static eu.emergingstandards.commons.TEINamespace.*;
import static eu.emergingstandards.utils.OxygenUtils.*;
import static eu.emergingstandards.utils.XMLUtils.XML_ID_ATTRIB_NAME;

/**
 * Created by mike on 3/10/14.
 */
public class HandSchemaList extends AbstractReferenceSchemaList<SchemaListItem> {

    public static final List<SchemaListAttribute> ATTRIBUTES = Arrays.asList(
            new SchemaListAttribute(HAND_ATTRIB_NAME),
            new SchemaListAttribute(NEW_ATTRIB_NAME, HAND_SHIFT_ELEMENT_NAME)
    );

    private AuthorElement handNotesElement;

    protected HandSchemaList(AuthorAccess authorAccess) {
        super(authorAccess);
    }

    @Nullable
    public AuthorElement getHandNotesElement() {
        if (handNotesElement == null) {
            handNotesElement = getAuthorElement("//teiHeader/profileDesc/handNotes[1]", authorAccess);
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
        return (HAND_NOTES_ELEMENT_NAME.equals(name) ||
                HAND_NOTE_ELEMENT_NAME.equals(name));
    }

    @Override
    public synchronized void refresh() {
        List<SchemaListItem> items = getItems();
        List<AuthorElement> handNotes = getContentElements(getHandNotesElement());

        for (AuthorElement handNote : handNotes) {
            String id = getAttrValue(handNote.getAttribute(XML_ID_ATTRIB_NAME));
            if (id != null) id = "#" + id;

            String tooltip = null;
            try {
                tooltip = MainUtils.emptyToNull(handNote.getTextContent());
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
