package info.histei.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 5/9/14.
 */
public interface UniqueAttribute<C> {
    @NotNull
    String getAttributeName();

    @Nullable
    String getElementName();

    @Nullable
    String getParentElementName();

    boolean matches(C context);
}
