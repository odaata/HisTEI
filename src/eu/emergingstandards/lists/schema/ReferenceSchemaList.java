package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.ListItem;
import org.jetbrains.annotations.NotNull;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

/**
 * Created by mike on 3/10/14.
 */
public interface ReferenceSchemaList<I extends ListItem> extends SchemaList<I> {

    @NotNull
    AuthorAccess getAuthorAccess();

    boolean isEdited(AuthorNode editedNode);

    void refresh();

    void addListener();

    void removeListener();

}
