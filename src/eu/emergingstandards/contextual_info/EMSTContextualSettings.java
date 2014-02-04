package eu.emergingstandards.contextual_info;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 2/3/14.
 */
public class EMSTContextualSettings {

    private static final Logger logger = Logger.getLogger(EMSTContextualInfo.class.getName());
    //  Base strings for Paths
    /*private static final String SOURCE_BASE_PATH = EditorVariables.PROJECT_DIRECTORY + "/contextual_info/";
    private static final String XQUERY_BASE_PATH = EditorVariables.FRAMEWORK_DIRECTORY + "/resources";
    private static final String XQUERY_PATH = XQUERY_BASE_PATH + "/contextual_info.xql";*/

    //  Main settings
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
            TYPES.put(s.type, s);
        }
    }

    @NotNull
    public static EMSTContextualSettings get(EMSTContextualType type) {
        return TYPES.get(type);
    }

    /* Instance Members */

    private EMSTContextualType type;
    private String fileName;

    protected EMSTContextualSettings(EMSTContextualType type, String fileName) {
        this.type = type;
        this.fileName = fileName;
    }

    @NotNull
    public EMSTContextualType getType() {
        return type;
    }

    @NotNull
    public String getFileName() {
        return fileName;
    }

    @NotNull
    public Map<String, EMSTContextualElementProperties> getElements() {
        return EMSTContextualElementProperties.filter(type);
    }
}
