package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import eu.emergingstandards.utils.EMSTUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private static enum ElementType {
        FACSIMILE, MEDIA, REFERENCES, NONE
    }

    private static final Logger logger = Logger.getLogger(EMSTFacsimile.class.getName());

    private static final String FACSIMILE_ELEMENT_NAME = "facsimile";
    private static final List<String> MEDIA_ELEMENT_NAMES = new ArrayList<>(2);

    static {
        MEDIA_ELEMENT_NAMES.add("media");
        MEDIA_ELEMENT_NAMES.add("graphic");
    }

    private static final String URL_ATTRIB_NAME = "url";
    private static final String MEDIA_TYPE_ATTRIB_NAME = "mediaType";
    private static final String FACS_ATTRIB_NAME = "facs";

    private static final Map<String, String> MEDIA_TYPES = new HashMap<>();

    static {
        MEDIA_TYPES.put("application/pdf", "media");
        MEDIA_TYPES.put("image/jpeg", "graphic");
    }

    /* Instance members */

    private ElementType currentType = ElementType.NONE;
    private AuthorAccess authorAccess;
    private AuthorElement currentElement;
    private AuthorElement facsimileElement;
    private List<String> references = new ArrayList<>();

    private final Tika tika = new Tika();
    /*private final DirectoryStream.Filter<Path> tikaFilter =
            new DirectoryStream.Filter<Path>() {
                @Override
                public boolean accept(Path entry) throws IOException {
                    String mediaType = tika.detect(entry.toFile());
                    return MEDIA_TYPES.keySet().contains(mediaType);
                }
            };*/

    public EMSTFacsimile(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;
        currentElement = EMSTUtils.getCurrentAuthorElement(authorAccess);

        if (currentElement != null) {
            String elementName = currentElement.getName();

            if (FACSIMILE_ELEMENT_NAME.equals(elementName)) {
                currentType = ElementType.FACSIMILE;
            } else if (MEDIA_ELEMENT_NAMES.contains(elementName)) {
                currentType = ElementType.MEDIA;
            } else {
                references = EMSTUtils.getAttrValues(currentElement.getAttribute(FACS_ATTRIB_NAME));
                if (!references.isEmpty()) {
                    currentType = ElementType.REFERENCES;
                }
            }
        }
    }

    @Nullable
    public AuthorElement getFacsimileElement() {
        if (facsimileElement == null) {
            switch (currentType) {
                case FACSIMILE:
                    facsimileElement = currentElement;
                    break;
                case MEDIA:
                    facsimileElement = (AuthorElement) currentElement.getParentElement();
                    break;
                case REFERENCES:
                    facsimileElement = EMSTUtils.getAuthorElement("//facsimile[1]", authorAccess);
                    break;
            }
        }
        return facsimileElement;
    }

    @Nullable
    public Path getBaseDirectory() {
        Path directory = null;

        URL url = getBaseDirectoryURL();
        if (url != null) {
            directory = EMSTUtils.castURLToPath(url);
        }
        return directory;
    }

    @Nullable
    public URL getBaseDirectoryURL() {
        URL directory = null;

        AuthorElement facsimile = getFacsimileElement();
        if (facsimile != null) {
            directory = facsimile.getXMLBaseURL();
            Path path = EMSTUtils.castURLToPath(directory);
            if (path != null && !Files.isDirectory(path)) {
                directory = EMSTUtils.castPathToURL(path.getParent());
            }
        }
        return directory;
    }

    public void setBaseDirectoryURL(URL directory) {
        String relativePath = authorAccess.getUtilAccess().makeRelative(
                authorAccess.getEditorAccess().getEditorLocation(), directory);

        if (relativePath == null || relativePath.isEmpty() || relativePath.equals(".")) {
            relativePath = null;
        } else {
            relativePath = EMSTUtils.decodeURL(relativePath);
        }

        authorAccess.getDocumentController().beginCompoundEdit();

//      Update the xml:base attribute
        authorAccess.getDocumentController().setAttribute(
                EMSTUtils.XML_BASE_ATTRIB_NAME, new AttrValue(relativePath), currentElement);
        updateMediaElements();

        authorAccess.getDocumentController().endCompoundEdit();
    }

    public void setBaseDirectory(File directory) {
        setBaseDirectoryURL(EMSTUtils.castFileToURL(directory));
    }

    public void chooseBaseDirectory() {
        File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
        if (dir != null) {
            setBaseDirectory(dir);
        }

        /*AuthorNode currentNode = EMSTUtils.getCurrentAuthorNode(authorAccess);
        if (currentNode != null && "facsimile".equals(currentNode.getName())) {

            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                AuthorElement facsimile = (AuthorElement) currentNode;
                try {
                    String relativePath = authorAccess.getUtilAccess().makeRelative(
                            authorAccess.getEditorAccess().getEditorLocation(),
                            dir.toURI().toURL()
                    );
                    authorAccess.getDocumentController().setAttribute("xml:base", new AttrValue(relativePath), facsimile);
                } catch (MalformedURLException e) {
                    logger.error(e, e);
                }

                int choice = authorAccess.getWorkspaceAccess().showConfirmDialog("Update graphic/media elements",
                        "Would you like to update the graphic and media elements from this directory?",
                        new String[]{"Yes", "No"}, new int[]{0, 1}
                );
                switch (choice) {
                    case 0:
                        break;
                    default:
                }
            }
        }*/
    }

    private void updateMediaElements() {
        Map<String, String> files = getMediaFiles();
        List<AuthorElement> elements = getMediaElements();

    }

    @NotNull
    public Map<String, String> getMediaFiles() {
        Map<String, String> files = new HashMap<>();

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(getBaseDirectory())) {
            for (Path filePath : dirStream) {
                File file = filePath.toFile();
                String mediaType = tika.detect(file);
                if (MEDIA_TYPES.keySet().contains(mediaType)) {
                    files.put(file.getName(), mediaType);
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
        return files;
    }

    @NotNull
    public List<AuthorElement> getMediaElements() {
        return EMSTUtils.getContentElements(getFacsimileElement());
    }

    @NotNull
    public List<List<String>> getMediaInfo() {
        List<List<String>> media = new ArrayList<>();

        List<AuthorElement> elements = getMediaElements();
        for (AuthorElement element : elements) {
            String id = EMSTUtils.getAttrValue(element.getAttribute(EMSTUtils.XML_ID_ATTR_NAME));
            String url = EMSTUtils.getAttrValue(element.getAttribute(URL_ATTRIB_NAME));
            String mediaType = EMSTUtils.getAttrValue(element.getAttribute(MEDIA_TYPE_ATTRIB_NAME));

            if (id != null || url != null || mediaType != null) {
                final List<String> entry = new ArrayList<>();
                entry.add(id);
                entry.add(url);
                entry.add(mediaType);
            }
        }
        return media;
    }

    @NotNull
    public Map<String, URL> getMediaURLs() {
        Map<String, URL> media = new HashMap<>();

        List<AuthorElement> elements = getMediaElements();
        for (AuthorElement element : elements) {
            String value = EMSTUtils.getAttrValue(element.getAttribute(EMSTUtils.XML_ID_ATTR_NAME));
            if (value != null) {
                media.put(value, getURL(element));
            }
        }
        return media;
    }

    @Nullable
    public URL getCurrentMediaURL() {
        URL url = null;

        if (currentType == ElementType.MEDIA) {
            url = getURL(currentElement);
        }
        return url;
    }

    @Nullable
    private URL getURL(AuthorElement authorElement) {
        URL url = null;

        AttrValue urlAttr = authorElement.getAttribute(URL_ATTRIB_NAME);
        String value = EMSTUtils.getAttrValue(urlAttr);
        if (value != null && authorElement != null) {
            url = authorElement.getXMLBaseURL();
            try {
                url = new URL(url, value);
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @NotNull
    public List<String> getReferences() {
        return references;
    }

    public void openCurrentMedia() throws EMSTFileMissingException {
        switch (currentType) {
            case FACSIMILE:
                EMSTUtils.openURL(authorAccess, getBaseDirectoryURL());
                break;
            case MEDIA:
                EMSTUtils.openURL(authorAccess, getCurrentMediaURL());
                break;
            case REFERENCES:
                if (!references.isEmpty()) {
                    Map<String, URL> media = getMediaURLs();
                    for (String reference : references) {
                        String id = StringUtils.substringAfter(reference, "#");
                        EMSTUtils.openURL(authorAccess, media.get(id.isEmpty() ? reference : id));
                    }
                }
                break;
        }
    }
}
