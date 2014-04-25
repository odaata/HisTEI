package eu.emergingstandards.lists;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by mike on 3/4/14.
 */
public interface Listable<I extends ListItem> {

    @NotNull
    List<I> getItems();

    void reset();
}
