package eu.emergingstandards.facsimile;

import eu.emergingstandards.utils.EMSTOxygenUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static eu.emergingstandards.utils.EMSTUtils.castURLToPath;

/**
 * Created by mike on 2/4/14.
 */
public class EMSTMediaElement {

    private static final Logger logger = Logger.getLogger(EMSTMediaElement.class.getName());

    private static final List<String> MEDIA_ELEMENT_NAMES = new ArrayList<>(2);

    static {
        MEDIA_ELEMENT_NAMES.add("media");
        MEDIA_ELEMENT_NAMES.add("graphic");
    }

    private static final String URL_ATTRIB_NAME = "url";
    private static final String MIME_TYPE_ATTRIB_NAME = "mimeType";

    @Nullable
    public static EMSTMediaElement get(AuthorElement authorElement) {
        EMSTMediaElement mediaElement = null;

        if (authorElement != null) {
            if (MEDIA_ELEMENT_NAMES.contains(authorElement.getName())) {
                mediaElement = new EMSTMediaElement(authorElement);
            }
        }
        return mediaElement;
    }

    @Nullable
    public static EMSTMediaElement get(AuthorNode authorNode) {
        return get(EMSTOxygenUtils.castAuthorElement(authorNode));
    }

    @Nullable
    public static EMSTMediaElement get(AuthorAccess authorAccess) {
        return get(EMSTOxygenUtils.getCurrentAuthorElement(authorAccess));
    }

    AuthorElement authorElement;

    protected EMSTMediaElement(AuthorElement authorElement) {
        this.authorElement = authorElement;
    }

    @NotNull
    public AuthorElement getAuthorElement() {
        return authorElement;
    }

    @Nullable
    public String getID() {
        return EMSTOxygenUtils.getAttrValue(authorElement.getAttribute(EMSTOxygenUtils.XML_ID_ATTRIB_NAME));
    }

    @Nullable
    public Path getPath() {
        return castURLToPath(getURL());
    }

    @Nullable
    public URL getURL() {
        URL url = null;

        String value = EMSTOxygenUtils.getAttrValue(authorElement.getAttribute(URL_ATTRIB_NAME));
        if (value != null) {
            url = authorElement.getXMLBaseURL();
            try {
                url = new URL(url, value);
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @Nullable
    public String getMimeType() {
        return EMSTOxygenUtils.getAttrValue(authorElement.getAttribute(MIME_TYPE_ATTRIB_NAME));
    }
}
