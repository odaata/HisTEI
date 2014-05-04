package info.histei.lists.schema;

import info.histei.facsimile.Facsimile;
import info.histei.facsimile.Media;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.util.Arrays;
import java.util.List;

import static info.histei.commons.TEINamespace.FACSIMILE_ELEMENT_NAME;
import static info.histei.commons.TEINamespace.FACS_ATTRIB_NAME;
import static info.histei.utils.MainUtils.decodeURL;

/**
 * Created by mike on 3/6/14.
 */
public class MediaSchemaList extends AbstractReferenceSchemaList<SchemaListItem> {

    public static final List<SchemaListAttribute> ATTRIBUTES =
            Arrays.asList(new SchemaListAttribute(FACS_ATTRIB_NAME));

    protected MediaSchemaList(AuthorAccess authorAccess) {
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
        return (FACSIMILE_ELEMENT_NAME.equals(name) ||
                Media.MEDIA_ELEMENT_NAMES.contains(name));
    }

    @Override
    public synchronized void refresh() {
        List<SchemaListItem> items = getItems();
        Facsimile facsimile = Facsimile.get(getAuthorAccess());

        if (facsimile != null) {
            for (Media media : facsimile.getMediaElements()) {
                String id = media.getID();
                if (id != null) id = "#" + id;

                SchemaListItem listItem = SchemaListItem.get(id, decodeURL(media.getURLValue()));
                if (listItem != null) {
                    items.add(listItem);
                }
            }
        }
    }

}
