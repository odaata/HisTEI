package eu.emergingstandards.lists;

import org.jetbrains.annotations.NotNull;

/**
 * Created by mike on 3/4/14.
 */
public interface ListItem {

    @NotNull
    String getValue();

    @NotNull
    String getLabel();

    @NotNull
    String getTooltip();

}
