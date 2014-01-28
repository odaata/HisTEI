package eu.emergingstandards.id;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

/**
 * Created by mike on 1/28/14.
 */
public class EMSTUniqueAttributesRecognizer extends DefaultUniqueAttributesRecognizer {

    public static GenerateIDElementsInfo GENERATE_ID_DEFAULTS = new GenerateIDElementsInfo(
            true, GenerateIDElementsInfo.DEFAULT_ID_GENERATION_PATTERN,
            new String[]{
                    "TEI",
                    "person",
                    "place",
                    "org"
//                "bibl",
//                    "evt"
            }
    );

    public EMSTUniqueAttributesRecognizer() {
        super("xml:id");
    }

    @Override
    protected GenerateIDElementsInfo getDefaultOptions() {
        return GENERATE_ID_DEFAULTS;
    }

    @Override
    public String getDescription() {
        return "EMST Unique attributes recognizer";
    }
}
