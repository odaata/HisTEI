package info.histei.facsimile;

import info.histei.exceptions.HTException;
import info.histei.exceptions.HTFileMissingException;
import info.histei.utils.OxygenUtils;
import info.histei.utils.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.histei.commons.TEINamespace.FACSIMILE_ELEMENT_NAME;
import static info.histei.utils.MainUtils.castFileToURL;
import static info.histei.utils.MainUtils.castURLToFile;
import static info.histei.utils.OxygenUtils.*;
import static info.histei.utils.XMLUtils.XML_BASE_ATTRIB_NAME;

/**
 * Created by mike on 1/25/14.
 */
public class Facsimile extends AbstractMediaElement {

    private static final Logger logger = Logger.getLogger(Facsimile.class.getName());

    private static final String FACSIMILE_ELEMENT = XMLUtils.createElement(FACSIMILE_ELEMENT_NAME);

    @Nullable
    public static MediaElement getMediaElement(AuthorAccess authorAccess, AuthorElement authorElement) {
        if (authorAccess != null && authorElement != null) {

            Media media = Media.get(authorAccess, authorElement);
            if (media != null) {
                return media;
            }

            MediaReference mediaReference = MediaReference.get(authorAccess, authorElement);
            if (mediaReference != null) {
                return mediaReference;
            }

            if (FACSIMILE_ELEMENT_NAME.equals(authorElement.getName())) {
                return new Facsimile(authorAccess, authorElement);
            }
        }
        return null;
    }

    @Nullable
    public static MediaElement getMediaElement(AuthorAccess authorAccess) {
        return getMediaElement(authorAccess, getCurrentAuthorElement(authorAccess));
    }

    @Nullable
    public static Facsimile get(AuthorAccess authorAccess, AuthorElement authorElement) {
        Facsimile facsimile = null;
        AuthorElement facsimileElement;

        if (authorAccess != null) {
            if (authorElement == null) {
                facsimileElement = OxygenUtils.getAuthorElement("//facsimile[1]", authorAccess);
            } else {
                facsimileElement = authorElement;
            }

            if (facsimileElement != null && FACSIMILE_ELEMENT_NAME.equals(facsimileElement.getName())) {
                facsimile = new Facsimile(authorAccess, facsimileElement);
            }
        }
        return facsimile;
    }

    @Nullable
    public static Facsimile get(AuthorAccess authorAccess) {
        return get(authorAccess, null);
    }

    @Nullable
    public static Facsimile createFacsimileElement(AuthorAccess authorAccess) throws HTException {
        Facsimile facsimile = get(authorAccess);

        if (facsimile == null) {
            try {
                authorAccess.getDocumentController().insertXMLFragment(
                        FACSIMILE_ELEMENT, "//teiHeader[1]", AuthorConstants.POSITION_AFTER);

                facsimile = get(authorAccess);

            } catch (AuthorOperationException e) {
                logger.error(e, e);
                throw new HTException("The <facsimile> element could not be created!", e);
            }
        }
        return facsimile;
    }

    /* Instance members */

    private File baseDirectory;

    protected Facsimile(AuthorAccess authorAccess, AuthorElement authorElement) {
        super(authorAccess, authorElement, MediaType.FACSIMILE);
    }

    @Nullable
    @Override
    public Facsimile getFacsimile() {
        return this;
    }

    @Override
    public void open() throws HTFileMissingException {
        openURL(authorAccess, getBaseDirectoryURL());
    }

    @Nullable
    public String getXMLBase() {
        return getAttrValue(authorElement.getAttribute(XML_BASE_ATTRIB_NAME));
    }

    @Nullable
    public File getBaseDirectory() {
        if (baseDirectory == null) {
            baseDirectory = castURLToFile(getBaseDirectoryURL());
        }
        return baseDirectory;
    }

    @Nullable
    public URL getBaseDirectoryURL() {
        URL directory = authorElement.getXMLBaseURL();
        String dir = directory.toString();

        if (!dir.endsWith("/")) {
            try {
                directory = new URL(dir.substring(0, dir.lastIndexOf("/") + 1));
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return directory;
    }

    public void setBaseDirectory(File newDirectory) throws HTException {
        setBaseDirectoryURL(castFileToURL(newDirectory));
        baseDirectory = newDirectory;
    }

    public void setBaseDirectoryURL(URL newDirectory) throws HTException {
        URL currentDirectory = getBaseDirectoryURL();
//          Do the uglies
        if (currentDirectory != null && !currentDirectory.equals(newDirectory)) {
//          It's all relative after all...
            String relativePath = makeRelative(authorAccess, authorElement.getParent().getXMLBaseURL(), newDirectory);
//          Update the xml:base attribute
            authorAccess.getDocumentController().setAttribute(
                    XML_BASE_ATTRIB_NAME, new AttrValue(relativePath), authorElement);
        }
    }

    public void updateMediaElements() throws HTException {
        List<String> newMedia = Media.createForDirectory(getBaseDirectory());

        if (newMedia.isEmpty()) throw new HTException("There are no files to add!");

        AuthorDocumentController controller = authorAccess.getDocumentController();
        controller.beginCompoundEdit();
//      Remove all existing elements first
        removeAllMediaElements();
//      Now bundle all that shiznit up into a big ol' string and get ready to insert it
        String mediaFragment = StringUtils.join(newMedia, "\n");

        try {  // Cuz, baby ya got to keep on trying...
            controller.insertXMLFragment(mediaFragment, authorElement, AuthorConstants.POSITION_INSIDE_FIRST);
        } catch (AuthorOperationException e) {
//              Yo! Shit blew up, yo! This should never, ever happen
            controller.cancelCompoundEdit();
            logger.error(e, e);  // Yes, tell the officer what the mean controller did to you
            throw new HTException("An error occurred while inserting references to the new files!", e);
        }
        controller.endCompoundEdit();
    }

    private void removeAllMediaElements() {
        AuthorDocumentController controller = authorAccess.getDocumentController();

        for (Media mediaElement : getMediaElements()) {
            controller.deleteNode(mediaElement.getAuthorElement());
        }
    }

    @NotNull
    public List<Media> getMediaElements() {
        List<Media> mediaElements = new ArrayList<>();

        List<AuthorElement> contentElements = getContentElements(authorElement);
        for (AuthorElement element : contentElements) {
            Media mediaElement = Media.get(authorAccess, element);
            if (mediaElement != null) {
                mediaElements.add(mediaElement);
            }
        }
        return mediaElements;
    }

    @NotNull
    public Map<String, URL> getMediaURLs() {
        Map<String, URL> mediaURLs = new HashMap<>();

        List<Media> mediaElements = getMediaElements();
        for (Media mediaElement : mediaElements) {
            String id = mediaElement.getID();
            URL url = mediaElement.getURL();
            if (id != null && url != null) {
                mediaURLs.put(id, url);
            }
        }
        return mediaURLs;
    }

}
