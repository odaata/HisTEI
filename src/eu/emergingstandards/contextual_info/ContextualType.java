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
public enum ContextualType {
    PERSON("Person", "psn", "person.xml"),
    PLACE("Place", "plc", "place.xml"),
    ORGANIZATION("Organization", "org", "org.xml"),
    GENRE("Genre", "gen", "genre.xml");
//  BIBLIOGRAPHY("Bibliography", "bib"),
//  EVENT("Event", "evt");

    // Lookup table
    private static final Map<String, ContextualType> lookup = new HashMap<>();

    static {
        for (ContextualType contextualType : ContextualType.values())
            lookup.put(contextualType.getKey(), contextualType);
    }

    @Nullable
    public static ContextualType get(String key) {
        if (key != null) {
            return lookup.get(key);
        } else {
            return null;
        }
    }

    @NotNull
    public static Set<String> getKeys() {
        Set<String> keys = new HashSet<>();

        for (ContextualType contextualType : ContextualType.values()) {
            keys.add(contextualType.getKey());
        }
        return keys;
    }

    /* Instance members */

    private String name;
    private String key;
    private String fileName;

    private ContextualType(String name, String key, String fileName) {
        this.name = name;
        this.key = key;
        this.fileName = fileName;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

}
