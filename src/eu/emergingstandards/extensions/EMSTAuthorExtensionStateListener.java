package eu.emergingstandards.extensions;

import eu.emergingstandards.lists.schema.SchemaListProvider;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;

/**
 * Created by mike on 3/7/14.
 */
public class EMSTAuthorExtensionStateListener extends EMSTUniqueAttributesRecognizer {

    private SchemaListProvider schemaListProvider;

    @Nullable
    public SchemaListProvider getSchemaListProvider() {
        return schemaListProvider;
    }

    @Override
    public String getDescription() {
        return "EMST Author Extension State Listener";
    }

    /**
     * @param authorAccess
     * @see ro.sync.ecss.extensions.api.AuthorExtensionStateListener#activated(ro.sync.ecss.extensions.api.AuthorAccess)
     */
    @Override
    public void activated(AuthorAccess authorAccess) {
        super.activated(authorAccess);

        if (schemaListProvider == null) {
            schemaListProvider = SchemaListProvider.add(authorAccess);
        }
    }

    /**
     * @param authorAccess
     * @see ro.sync.ecss.extensions.api.AuthorExtensionStateListener#deactivated(ro.sync.ecss.extensions.api.AuthorAccess)
     */
    @Override
    public void deactivated(AuthorAccess authorAccess) {
        super.deactivated(authorAccess);

        SchemaListProvider.remove(authorAccess);
        schemaListProvider = null;
    }
}
