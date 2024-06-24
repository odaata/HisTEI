package info.histei.lists.schema;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static info.histei.commons.TEINamespace.*;
import static info.histei.utils.XMLUtils.XML_LANG_ATTRIB_NAME;

/**
 * Created by mike on 3/7/14.
 */
public class StaticSchemaList extends AbstractSchemaList<SchemaListItem> {

    private static final List<SchemaList<SchemaListItem>> LISTS = new ArrayList<>(6);

    static {
//        @xml:lang attribute
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(XML_LANG_ATTRIB_NAME)),
                        new String[][]{
                                {"nl", "Dutch"},
                                {"en", "English"},
                                {"fr", "French"},
                                {"de", "German"},
                                {"ga", "Gaelic"},
                                {"ga-la", "Gaelic-Latin"},
                                {"la", "Latin"}
                        }
                )
        );
//        @script attribute on <handNote> and <handShift> elements
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(
                                new SchemaListAttribute(SCRIPT_ATTRIB_NAME, HAND_SHIFT_ELEMENT_NAME),
                                new SchemaListAttribute(SCRIPT_ATTRIB_NAME, HAND_NOTE_ELEMENT_NAME)
                        ),
                        new String[][]{
                                {"copied", "Copied from a previous version of the text"},
                                {"original", "Written by the original author"}
                        }
                )
        );
//        @break attribute on <lb> element
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(BREAK_ATTRIB_NAME)),
                        new String[][]{
                                {"yes", "break is between words"},
                                {"no", "break is inside a word"}
                        }
                )
        );
//        @status attribute (for change and revisionDesc)
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(STATUS_ATTRIB_NAME)),
                        new String[][]{
                                {"transcr_started", "initial transcription started but not yet complete"},
                                {"transcribed", "transcription complete"},
                                {"corr_started", "corrections started but not yet complete"},
                                {"corrected", "corrections completed"},
                                {"token_started", "tokenization started but not yet complete"},
                                {"tokenized", "tokenization complete"}
                        }
                )
        );
    }

    @NotNull
    public static List<SchemaList<SchemaListItem>> getAll() {
        return new ArrayList<>(LISTS);
    }

    private final List<SchemaListAttribute> attributes;
    private final String[][] itemStrings;

    protected StaticSchemaList(List<SchemaListAttribute> attributes, String[][] itemStrings) {
        this.attributes = attributes;
        this.itemStrings = itemStrings;

        reset();
    }

    @NotNull
    @Override
    public List<SchemaListAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public synchronized void reset() {
        super.reset();

        final List<SchemaListItem> items = getItems();

        for (String[] vals : itemStrings) {
            items.add(SchemaListItem.get(vals[0], vals[1]));
        }
    }
}
