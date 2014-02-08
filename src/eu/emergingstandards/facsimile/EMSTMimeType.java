package eu.emergingstandards.facsimile;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 2/6/14.
 */
public enum EMSTMimeType {
    JPG(EMSTMedia.GRAPHIC_ELEMENT_NAME, "image/jpeg", "image"),
    PNG(EMSTMedia.GRAPHIC_ELEMENT_NAME, "image/png", "image"),
    PDF(EMSTMedia.MEDIA_ELEMENT_NAME, "application/pdf", "pdf");

    // Lookup table
    private static final Map<String, EMSTMimeType> lookup = new HashMap<>();

    static {
        for (EMSTMimeType type : EMSTMimeType.values())
            lookup.put(type.getMimeType(), type);
    }

    @Nullable
    public static EMSTMimeType get(String mimeType) {
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

    private EMSTMimeType(String elementName, String mimeType, String idAbbr) {
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
