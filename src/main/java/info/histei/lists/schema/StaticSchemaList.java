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

    private static final List<SchemaList<SchemaListItem>> LISTS = new ArrayList<>(12);

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
//        @key attribute on <resp> elements
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(KEY_ATTRIB_NAME, RESP_ELEMENT_NAME)),
                        new String[][]{
                                {"ann", "Annotator"},
                                {"crr", "Corrector"},
                                {"edt", "Editor"},
                                {"res", "Researcher"},
                                {"trc", "Transcriber"}
                        }
                )
        );
//        @type attribute on <date> elements within <creation>
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(TYPE_ATTRIB_NAME, DATE_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
                        new String[][]{
                                {"arrived", "when the letter or other document arrived at its destination"},
                                {"copied", "when the text was copied"},
                                {"deposed", "when the person involved was deposed"},
                                {"sent", "when the letter or other document was sent"},
                                {"written", "when the text was written"}
                        }
                )
        );
//        @type attribute on <persName> elements within <creation>
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(TYPE_ATTRIB_NAME, PERS_NAME_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
                        new String[][]{
                                {"author", "person who wrote the text or is responsible for its contents"},
                                {"addressee", "person the letter or other document was addressed to"},
                                {"copyist", "person who copied the text"},
                                {"deponent", "person who being interviewed in a deposition"},
                                {"sender", "person who sent the letter or other document"}
                        }
                )
        );
//        @type attribute on place elements within <creation>
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(
                                new SchemaListAttribute(TYPE_ATTRIB_NAME, DISTRICT_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new SchemaListAttribute(TYPE_ATTRIB_NAME, SETTLEMENT_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new SchemaListAttribute(TYPE_ATTRIB_NAME, REGION_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new SchemaListAttribute(TYPE_ATTRIB_NAME, COUNTRY_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new SchemaListAttribute(TYPE_ATTRIB_NAME, BLOC_ELEMENT_NAME, CREATION_ELEMENT_NAME)
                        ),
                        new String[][]{
                                {"arrived", "where the letter or other document arrived"},
                                {"copied", "where the text was copied"},
                                {"deposed", "where the person involved was deposed"},
                                {"sent", "where the letter or other document was sent from"},
                                {"written", "where the text was written"}
                        }
                )
        );
//        @type attribute on <orgName> elements within <creation>
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(TYPE_ATTRIB_NAME, ORG_NAME_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
                        new String[][]{
                                {"author", "org for which the author wrote the text"},
                                {"addressee", "org the letter or other document was addressed to"},
                                {"copyist", "org for which the text was copied"},
                                {"deposition", "org holding the deposition"},
                                {"sender", "org that sent the letter or other document"}
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
//        @reason attribute
        LISTS.add(
                new StaticSchemaList(
                        Arrays.asList(new SchemaListAttribute(REASON_ATTRIB_NAME)),
                        new String[][]{
                                {"rubbing", "any kind of rubbing/strikethrough/blot"},
                                {"hand", "anything illegible because of the handwriting"},
                                {"overwriting", "legibility is hampered by overwriting"},
                                {"damage", "illegible/unclear due to damage: smoke, hole, tear, water damage"},
                                {"faded", "writing is faded and difficult to read"}
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
