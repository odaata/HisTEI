package info.histei.facsimile;

import info.histei.exceptions.HTFileMissingException;
import info.histei.utils.XMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;

import static info.histei.commons.TEINamespace.*;
import static info.histei.utils.MainUtils.castFileToURL;
import static info.histei.utils.MainUtils.castURLToFile;
import static info.histei.utils.OxygenUtils.*;
import static info.histei.utils.XMLUtils.XML_ID_ATTRIB_NAME;

/**
 * Created by mike on 2/4/14.
 */
public class Media extends AbstractMediaElement {

    private static final Logger logger = Logger.getLogger(Media.class.getName());

    public static final List<String> MEDIA_ELEMENT_NAMES = new ArrayList<>(2);

    static {
        MEDIA_ELEMENT_NAMES.add(MEDIA_ELEMENT_NAME);
        MEDIA_ELEMENT_NAMES.add(GRAPHIC_ELEMENT_NAME);
    }

    @Nullable
    public static Media get(AuthorAccess authorAccess, AuthorElement authorElement) {
        Media media = null;

        if (authorAccess != null && authorElement != null) {
            if (MEDIA_ELEMENT_NAMES.contains(authorElement.getName())) {
                media = new Media(authorAccess, authorElement);
            }
        }
        return media;
    }

    @Nullable
    public static Media get(AuthorAccess authorAccess) {
        return get(authorAccess, getCurrentAuthorElement(authorAccess));
    }

    @NotNull
    public static List<String> createForDirectory(File directory) {
        List<String> newMedia = new ArrayList<>();

        if (directory != null) {
            File[] listFiles = directory.listFiles();
            if (listFiles != null) {
                Arrays.sort(listFiles);
                for (int i = 0; i < listFiles.length; i++) {
                    File file = listFiles[i];
                    String mediaElement = Media.create(file, i + 1);
                    if (mediaElement != null) {
                        newMedia.add(mediaElement);
                    }
                }
            }
        }
        return newMedia;
    }

    @Nullable
    public static String create(File file, int counter) {
        if (file == null) return null;

        String mimeType;
        try {
            mimeType = Files.probeContentType(file.toPath());
        } catch (IOException e) {
            logger.error(e, e);
            return null;
        }

        MimeType type = MimeType.get(mimeType);
        if (type != null) {
            if (counter < 1) counter = 1;
            String id = type.getIdAbbr() + "_" + String.format("%03d", counter);

            String fileName = null;
            URL fileURL = castFileToURL(file);
            if (fileURL != null) {
                fileName = StringUtils.substringAfterLast(fileURL.toExternalForm(), "/");
            }

            if (fileName != null) {
                return create(mimeType, id, fileName);
            }
        }
        return null;
    }

    @Nullable
    public static String create(String mimeType, String id, String url) {
        String newElement = null;

        MimeType type = MimeType.get(mimeType);
        if (type != null) {
            Map<String, String> attributes = new HashMap<>(3);
            attributes.put(XML_ID_ATTRIB_NAME, id);
            attributes.put(URL_ATTRIB_NAME, url);
            attributes.put(MIME_TYPE_ATTRIB_NAME, mimeType);

            newElement = XMLUtils.createElement(type.getElementName(), attributes);
        }
        return newElement;
    }

    protected Media(AuthorAccess authorAccess, AuthorElement authorElement) {
        super(authorAccess, authorElement, MediaType.MEDIA);
    }

    @Override
    @Nullable
    public Facsimile getFacsimile() {
        if (facsimile == null) {
            facsimile = Facsimile.get(
                    authorAccess, (AuthorElement) authorElement.getParentElement());
        }
        return facsimile;
    }

    @Override
    public void open() throws HTFileMissingException {
        openURL(authorAccess, getURL());
    }

    @Nullable
    public String getID() {
        return getAttrValue(authorElement.getAttribute(XML_ID_ATTRIB_NAME));
    }

    @Nullable
    public File getFile() {
        return castURLToFile(getURL());
    }

    @Nullable
    public String getURLValue() {
        return getAttrValue(authorElement.getAttribute(URL_ATTRIB_NAME));
    }

    @Nullable
    public URL getURL() {
        URL url = null;

        String value = getURLValue();
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
        return getAttrValue(authorElement.getAttribute(MIME_TYPE_ATTRIB_NAME));
    }
}
