package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.net.URL;
import java.util.List;
import java.util.Map;

import static eu.emergingstandards.commons.EMSTTEINamespace.FACS_ATTRIB_NAME;
import static eu.emergingstandards.utils.EMSTOxygenUtils.getAttrValues;
import static eu.emergingstandards.utils.EMSTOxygenUtils.openURL;

/**
 * Created by mike on 2/7/14.
 */
public class EMSTMediaReference extends EMSTAbstractMediaElement {

    @Nullable
    public static EMSTMediaReference get(AuthorAccess authorAccess, AuthorElement authorElement) {
        EMSTMediaReference mediaReference = null;

        if (authorElement != null) {
            List<String> references = getAttrValues(authorElement.getAttribute(FACS_ATTRIB_NAME));

            if (!references.isEmpty()) {
                mediaReference = new EMSTMediaReference(authorAccess, authorElement, references);
            }
        }
        return mediaReference;
    }

    private List<String> references;

    protected EMSTMediaReference(AuthorAccess authorAccess, AuthorElement authorElement, List<String> references) {
        super(authorAccess, authorElement, EMSTMediaType.REFERENCE);

        this.references = references;
    }

    @Override
    public void open() throws EMSTFileMissingException {
        EMSTFacsimile facsimile = getFacsimile();
        if (facsimile != null && !references.isEmpty()) {
            Map<String, URL> mediaURLs = facsimile.getMediaURLs();
            for (String reference : references) {
                String id = !reference.startsWith("#") ? reference : StringUtils.substringAfter(reference, "#");
                openURL(authorAccess, mediaURLs.get(id));
            }
        }
    }
}
