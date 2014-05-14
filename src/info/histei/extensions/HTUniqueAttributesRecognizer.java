package info.histei.extensions;

import ro.sync.ecss.extensions.commons.id.DefaultUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

/**
 * Created by mike on 1/28/14.
 */
public class HTUniqueAttributesRecognizer extends DefaultUniqueAttributesRecognizer {

    public static final GenerateIDElementsInfo GENERATE_ID_DEFAULTS = new GenerateIDElementsInfo(
            true, GenerateIDElementsInfo.DEFAULT_ID_GENERATION_PATTERN,
            new String[]{
                    "TEI",
                    "person",
                    "place",
                    "org",
                    "bibl",
                    "biblStruct",
                    "biblFull",
                    "relation",
                    "event",
                    "ab",
                    "seg",
                    "term",
                    "s",
                    "cl",
                    "phr",
                    "w"
            }
    );

    public HTUniqueAttributesRecognizer() {
        super("xml:id");
    }

    @Override
    protected GenerateIDElementsInfo getDefaultOptions() {
        return GENERATE_ID_DEFAULTS;
    }

    @Override
    public String getDescription() {
        return "HisTEI Unique attributes recognizer";
    }
}
