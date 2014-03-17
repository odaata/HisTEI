package eu.emergingstandards.lists.schema;

import eu.emergingstandards.facsimile.EMSTFacsimile;
import eu.emergingstandards.facsimile.EMSTMedia;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.Arrays;
import java.util.List;

import static eu.emergingstandards.commons.EMSTTEINamespace.FACSIMILE_ELEMENT_NAME;
import static eu.emergingstandards.commons.EMSTTEINamespace.FACS_ATTRIB_NAME;
import static eu.emergingstandards.utils.EMSTUtils.decodeURL;

/**
 * Created by mike on 3/6/14.
 */
public class EMSTMediaSchemaList extends EMSTAbstractReferenceSchemaList<EMSTSchemaListItem> {

    public static final List<EMSTSchemaListAttribute> ATTRIBUTES =
            Arrays.asList(new EMSTSchemaListAttribute(FACS_ATTRIB_NAME));

    protected EMSTFacsimile facsimile;

    protected EMSTMediaSchemaList(AuthorAccess authorAccess) {
        super(authorAccess);
    }

    @Nullable
    public EMSTFacsimile getFacsimile() {
        if (facsimile == null) {
            facsimile = EMSTFacsimile.get(getAuthorAccess());
        }
        return facsimile;
    }

    @NotNull
    @Override
    public List<EMSTSchemaListAttribute> getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public boolean isEdited(AuthorNode editedNode) {
        String name = editedNode.getName();
        return (FACSIMILE_ELEMENT_NAME.equals(name) ||
                EMSTMedia.MEDIA_ELEMENT_NAMES.contains(name));
    }

    @Override
    public synchronized void refresh() {
        List<EMSTSchemaListItem> items = getItems();
        EMSTFacsimile facsimile = getFacsimile();

        if (facsimile != null) {
            for (EMSTMedia media : facsimile.getMediaElements()) {
                String id = media.getID();
                if (id != null) id = "#" + id;

                EMSTSchemaListItem listItem = EMSTSchemaListItem.get(id, decodeURL(media.getURLValue()));
                if (listItem != null) {
                    items.add(listItem);
                }
            }
        }
    }

}
