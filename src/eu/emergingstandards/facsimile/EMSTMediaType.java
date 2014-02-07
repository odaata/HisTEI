package eu.emergingstandards.facsimile;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 2/6/14.
 */
public enum EMSTMediaType {
    JPG(EMSTMediaElement.GRAPHIC_ELEMENT_NAME, "image/jpeg", "image"),
    PNG(EMSTMediaElement.GRAPHIC_ELEMENT_NAME, "image/png", "image"),
    PDF(EMSTMediaElement.MEDIA_ELEMENT_NAME, "application/pdf", "pdf");

    // Lookup table
    private static final Map<String, EMSTMediaType> lookup = new HashMap<>();

    static {
        for (EMSTMediaType type : EMSTMediaType.values())
            lookup.put(type.getMimeType(), type);
    }

    public static boolean isAllowed(String mimeType) {
        return lookup.keySet().contains(mimeType);
    }

    @Nullable
    public static EMSTMediaType get(String mimeType) {
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

    private EMSTMediaType(String elementName, String mimeType, String idAbbr) {
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
