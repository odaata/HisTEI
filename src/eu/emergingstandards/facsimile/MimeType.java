package eu.emergingstandards.facsimile;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static eu.emergingstandards.commons.TEINamespace.GRAPHIC_ELEMENT_NAME;
import static eu.emergingstandards.commons.TEINamespace.MEDIA_ELEMENT_NAME;

/**
 * Created by mike on 2/6/14.
 */
public enum MimeType {
    JPG(GRAPHIC_ELEMENT_NAME, "image/jpeg", "image"),
    PNG(GRAPHIC_ELEMENT_NAME, "image/png", "image"),
    PDF(MEDIA_ELEMENT_NAME, "application/pdf", "pdf");

    // Lookup table
    private static final Map<String, MimeType> lookup = new HashMap<>();

    static {
        for (MimeType type : MimeType.values())
            lookup.put(type.getMimeType(), type);
    }

    @Nullable
    public static MimeType get(String mimeType) {
        if (mimeType != null) {
            return lookup.get(mimeType);
        } else {
            return null;
        }
    }

    /* Instance members */

    private String elementName;
    private String mimeType;
    private String idAbbr;

    private MimeType(String elementName, String mimeType, String idAbbr) {
        this.elementName = elementName;
        this.mimeType = mimeType;
        this.idAbbr = idAbbr;
    }

    public String getElementName() {
        return elementName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getIdAbbr() {
        return idAbbr;
    }
}
