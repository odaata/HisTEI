package info.histei.facsimile;

/**
 * Created by mike on 2/7/14.
 */
public enum MediaType {
    FACSIMILE(Facsimile.class),
    MEDIA(Media.class),
    REFERENCE(MediaReference.class);

    private Class<? extends MediaElement> mediaElement;

    private MediaType(Class<? extends MediaElement> mediaElement) {
        this.mediaElement = mediaElement;
    }

    public Class<? extends MediaElement> getMediaElement() {
        return mediaElement;
    }
}
