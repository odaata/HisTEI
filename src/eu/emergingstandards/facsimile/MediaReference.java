package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static eu.emergingstandards.commons.TEINamespace.FACS_ATTRIB_NAME;
import static eu.emergingstandards.utils.OxygenUtils.getAttrValues;
import static eu.emergingstandards.utils.OxygenUtils.openURL;

/**
 * Created by mike on 2/7/14.
 */
public class MediaReference extends AbstractMediaElement {

    @Nullable
    public static MediaReference get(AuthorAccess authorAccess, AuthorElement authorElement) {
        MediaReference mediaReference = null;

        if (authorElement != null) {
            List<String> references = getAttrValues(authorElement.getAttribute(FACS_ATTRIB_NAME));

            if (!references.isEmpty()) {
                mediaReference = new MediaReference(authorAccess, authorElement, references);
            }
        }
        return mediaReference;
    }

    private List<String> references;

    protected MediaReference(AuthorAccess authorAccess, AuthorElement authorElement, List<String> references) {
        super(authorAccess, authorElement, MediaType.REFERENCE);

        this.references = references;
    }

    @Override
    public void open() throws EMSTFileMissingException {
        Facsimile facsimile = getFacsimile();
        if (facsimile != null && !references.isEmpty()) {
            Map<String, URL> mediaURLs = facsimile.getMediaURLs();
            for (String reference : references) {
                String id = !reference.startsWith("#") ? reference : StringUtils.substringAfter(reference, "#");
                openURL(authorAccess, mediaURLs.get(id));
            }
        }
    }
}
