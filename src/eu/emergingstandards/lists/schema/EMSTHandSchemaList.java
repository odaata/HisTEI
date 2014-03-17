package eu.emergingstandards.lists.schema;

import eu.emergingstandards.utils.EMSTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import javax.swing.text.BadLocationException;
import java.util.Arrays;
import java.util.List;

import static eu.emergingstandards.commons.EMSTTEINamespace.*;
import static eu.emergingstandards.utils.EMSTOxygenUtils.*;
import static eu.emergingstandards.utils.EMSTXMLUtils.XML_ID_ATTRIB_NAME;

/**
 * Created by mike on 3/10/14.
 */
public class EMSTHandSchemaList extends EMSTAbstractReferenceSchemaList<EMSTSchemaListItem> {

    public static final List<EMSTSchemaListAttribute> ATTRIBUTES = Arrays.asList(
            new EMSTSchemaListAttribute(HAND_ATTRIB_NAME),
            new EMSTSchemaListAttribute(NEW_ATTRIB_NAME, HAND_SHIFT_ELEMENT_NAME)
    );

    private AuthorElement handNotesElement;

    protected EMSTHandSchemaList(AuthorAccess authorAccess) {
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
    public List<EMSTSchemaListAttribute> getAttributes() {
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
        List<EMSTSchemaListItem> items = getItems();
        List<AuthorElement> handNotes = getContentElements(getHandNotesElement());

        for (AuthorElement handNote : handNotes) {
            String id = getAttrValue(handNote.getAttribute(XML_ID_ATTRIB_NAME));
            if (id != null) id = "#" + id;

            String tooltip = null;
            try {
                tooltip = EMSTUtils.emptyToNull(handNote.getTextContent());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }

            EMSTSchemaListItem listItem = EMSTSchemaListItem.get(id, tooltip);
            if (listItem != null) {
                items.add(listItem);
            }
        }
    }
}
