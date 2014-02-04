package eu.emergingstandards.contextual_info;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by mike on 2/3/14.
 */
public enum EMSTContextualType {
    PERSON("Person", "psn"),
    PLACE("Place", "plc"),
    ORGANIZATION("Organization", "org"),
    GENRE("Genre", "gen");
//  BIBLIOGRAPHY("Bibliography", "bib"),
//  EVENT("Event", "evt");

    // Lookup table
    private static final Map<String, EMSTContextualType> lookup = new HashMap<>();

    static {
        for (EMSTContextualType type : EMSTContextualType.values())
            lookup.put(type.getKey(), type);
    }

    @Nullable
    public static EMSTContextualType get(String key) {
        return lookup.get(key);
    }

    @NotNull
    public static Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (EMSTContextualType type : EMSTContextualType.values()) {
            keys.add(type.getKey());
        }
        return keys;
    }

    /* Instance members */

    private String name;
    private String key;

    private EMSTContextualType(String name, String key) {
        this.name = name;
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }
}
