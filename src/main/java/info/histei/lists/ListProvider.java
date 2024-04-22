package info.histei.lists;

import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;

/**
 * Created by mike on 5/9/14.
 */
public interface ListProvider<C, L extends Listable<? extends ListItem>> {
    AuthorAccess getAuthorAccess();

    @Nullable
    L getList(C context);
}
