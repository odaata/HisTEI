package eu.emergingstandards.facsimile;

/**
 * Created by mike on 2/7/14.
 */
public enum EMSTMediaType {
    FACSIMILE(EMSTFacsimile.class),
    MEDIA(EMSTMedia.class),
    REFERENCE(EMSTMediaReference.class);

    private Class<? extends EMSTMediaElement> mediaElement;

    private EMSTMediaType(Class<? extends EMSTMediaElement> mediaElement) {
        this.mediaElement = mediaElement;
    }

    public Class<? extends EMSTMediaElement> getMediaElement() {
        return mediaElement;
    }
}
