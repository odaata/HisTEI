package eu.emergingstandards.contextual_info;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualSettings {

//    private static final Logger logger = Logger.getLogger(EMSTContextualInfo.class.getName());

    private static final Map<EMSTContextualType, EMSTContextualSettings> TYPES = new EnumMap<>(EMSTContextualType.class);

    static {
        List<EMSTContextualSettings> settings = new ArrayList<>();

        settings.add(new EMSTContextualSettings(EMSTContextualType.PERSON, "person.xml"));
        settings.add(new EMSTContextualSettings(EMSTContextualType.PLACE, "place.xml"));
        settings.add(new EMSTContextualSettings(EMSTContextualType.ORGANIZATION, "org.xml"));
        settings.add(new EMSTContextualSettings(EMSTContextualType.GENRE, "genre.xml"));
//        settings.add(new EMSTContextualSettings(EMSTContextualType.BIBLIOGRAPHY, "bibliography.xml"));
//        settings.add(new EMSTContextualSettings(EMSTContextualType.EVENT, "event.xml"));

        for (EMSTContextualSettings s : settings) {
            TYPES.put(s.contextualType, s);
        }
    }

    @NotNull
    public static EMSTContextualSettings get(EMSTContextualType contextualType) {
        return TYPES.get(contextualType);
    }

    /* Instance Members */

    private EMSTContextualType contextualType;
    private String fileName;

    protected EMSTContextualSettings(EMSTContextualType contextualType, String fileName) {
        this.contextualType = contextualType;
        this.fileName = fileName;
    }

    @NotNull
    public EMSTContextualType getContextualType() {
        return contextualType;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

    @NotNull
    public Map<String, EMSTContextualElementProperties> getElements() {
        return EMSTContextualElementProperties.filter(contextualType);
    }
}
