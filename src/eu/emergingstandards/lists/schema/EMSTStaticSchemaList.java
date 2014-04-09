package eu.emergingstandards.lists.schema;

import eu.emergingstandards.lists.EMSTListItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static eu.emergingstandards.commons.EMSTTEINamespace.*;
import static eu.emergingstandards.utils.EMSTXMLUtils.XML_LANG_ATTRIB_NAME;

/**
 * Created by mike on 3/7/14.
 */
public class EMSTStaticSchemaList extends EMSTAbstractSchemaList<EMSTSchemaListItem> {

    private static final List<EMSTSchemaList<? extends EMSTListItem>> LISTS = new ArrayList<>(5);

    static {
//        @xml:lang attribute
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(XML_LANG_ATTRIB_NAME)),
                        new String[][]{
                                {"nl", "Dutch"},
                                {"en", "English"},
                                {"fr", "French"},
                                {"de", "German"},
                                {"la", "Latin"}
                        }
                )
        );
//        @key attribute on <resp> elements
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(KEY_ATTRIB_NAME, RESP_ELEMENT_NAME)),
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
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, DATE_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
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
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, PERS_NAME_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
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
                new EMSTStaticSchemaList(
                        Arrays.asList(
                                new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, DISTRICT_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, SETTLEMENT_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, REGION_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, COUNTRY_ELEMENT_NAME, CREATION_ELEMENT_NAME),
                                new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, BLOC_ELEMENT_NAME, CREATION_ELEMENT_NAME)
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
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, ORG_NAME_ELEMENT_NAME, CREATION_ELEMENT_NAME)),
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
                new EMSTStaticSchemaList(
                        Arrays.asList(
                                new EMSTSchemaListAttribute(SCRIPT_ATTRIB_NAME, HAND_SHIFT_ELEMENT_NAME),
                                new EMSTSchemaListAttribute(SCRIPT_ATTRIB_NAME, HAND_NOTE_ELEMENT_NAME)
                        ),
                        new String[][]{
                                {"copied", "Copied from a previous version of the text"},
                                {"original", "Written by the original author"}
                        }
                )
        );
//        @type attribute on <div> elements
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, DIV_ELEMENT_NAME)),
                        new String[][]{
                                {"address", "address, e.g. transcribed from a letter"},
                                {"entry", "entry in a journal, diary, travelogue, etc."},
                                {"frontispiece", "illustration facing the title page of a book"},
                                {"letter", "letter or piece of correspondence"},
                                {"preface", "introduction to a book, typically stating its subject, scope, or aims"},
                                {"notes", "notes made by the author in a transcribed work"}
                        }
                )
        );
//        @type attribute on <fw> elements
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(TYPE_ATTRIB_NAME, FW_ELEMENT_NAME)),
                        new String[][]{
                                {"header", "a running title at the top of the page"},
                                {"footer", "a running title at the bottom of the page"},
                                {"pageNum", "(page number) a page number or foliation symbol"},
                                {"lineNum", "(line number) a line number, either of prose or poetry"},
                                {"sig", "(signature) a signature or gathering symbol"},
                                {"catch", "(catchword) a catch-word"}
                        }
                )
        );
//        @break attribute on <lb> element
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(BREAK_ATTRIB_NAME)),
                        new String[][]{
                                {"yes", "break is between words"},
                                {"no", "break is inside a word"}
                        }
                )
        );
//        @reason attribute
        LISTS.add(
                new EMSTStaticSchemaList(
                        Arrays.asList(new EMSTSchemaListAttribute(REASON_ATTRIB_NAME)),
                        new String[][]{
                                {"rubbing", "any kind of rubbing/strikethrough/blot"},
                                {"hand", "anything illegible because of the handwriting"},
                                {"overwriting", "legibility is hampered by overwriting"},
                                {"damage", "illegible/unclear due to damage: smoke, hole, tear, water damage"}
                        }
                )
        );

    }

    @NotNull
    public static List<EMSTSchemaList<? extends EMSTListItem>> getAll() {
        return new ArrayList<>(LISTS);
    }

    private final List<EMSTSchemaListAttribute> attributes;
    private final String[][] itemStrings;

    protected EMSTStaticSchemaList(List<EMSTSchemaListAttribute> attributes, String[][] itemStrings) {
        this.attributes = attributes;
        this.itemStrings = itemStrings;

        reset();
    }

    @NotNull
    @Override
    public List<EMSTSchemaListAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public synchronized void reset() {
        super.reset();

        final List<EMSTSchemaListItem> items = getItems();

        for (String[] vals : itemStrings) {
            items.add(EMSTSchemaListItem.get(vals[0], vals[1]));
        }
    }
}
