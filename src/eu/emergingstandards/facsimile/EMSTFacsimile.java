package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTFileMissingException;
import eu.emergingstandards.utils.EMSTNamespaceType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static eu.emergingstandards.utils.EMSTOxygenUtils.*;
import static eu.emergingstandards.utils.EMSTUtils.*;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private static enum ElementType {
        FACSIMILE, MEDIA, REFERENCES, NONE
    }

    private static final Logger logger = Logger.getLogger(EMSTFacsimile.class.getName());

    private static final String FACSIMILE_ELEMENT_NAME = "facsimile";
    private static final String FACSIMILE_DEFAULT_ELEMENT =
            "<" + FACSIMILE_ELEMENT_NAME + " xmlns='" + EMSTNamespaceType.TEI.getURLID() + "'/>";
    private static final List<String> MEDIA_ELEMENT_NAMES = new ArrayList<>(2);

    static {
        MEDIA_ELEMENT_NAMES.add("media");
        MEDIA_ELEMENT_NAMES.add("graphic");
    }

    private static final String URL_ATTRIB_NAME = "url";
    private static final String MEDIA_TYPE_ATTRIB_NAME = "mimeType";
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
                    String mimeType = tika.detect(entry.toFile());
                    return MEDIA_TYPES.keySet().contains(mimeType);
                }
            };*/

    public EMSTFacsimile(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;
        currentElement = getCurrentAuthorElement(authorAccess);

        if (currentElement != null) {
            String elementName = currentElement.getName();

            if (FACSIMILE_ELEMENT_NAME.equals(elementName)) {
                currentType = ElementType.FACSIMILE;
            } else if (MEDIA_ELEMENT_NAMES.contains(elementName)) {
                currentType = ElementType.MEDIA;
            } else {
                references = getAttrValues(currentElement.getAttribute(FACS_ATTRIB_NAME));
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
                    facsimileElement = getAuthorElement("//facsimile[1]", authorAccess);
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
            directory = castURLToPath(url);
        }
        return directory;
    }

    @Nullable
    public URL getBaseDirectoryURL() {
        URL directory = null;

        AuthorElement facsimile = getFacsimileElement();
        if (facsimile != null) {
            directory = facsimile.getXMLBaseURL();
            Path path = castURLToPath(directory);
            if (path != null && !Files.isDirectory(path)) {
                directory = castPathToURL(path.getParent());
            }
        }
        return directory;
    }

    public void setBaseDirectoryURL(URL newDirectory) {
        URL currentDirectory = getBaseDirectoryURL();
        String relativePath = makeRelative(authorAccess, newDirectory);

        if (relativePath == null || relativePath.equals(".")) {
            relativePath = null;
        } else {
            relativePath = decodeURL(relativePath);
        }

        AuthorElement facsimile = getFacsimileElement();
        AuthorDocumentController controller = authorAccess.getDocumentController();
        controller.beginCompoundEdit();

//          Create facsimile
        if (facsimile == null) {
            try {
                controller.insertXMLFragment(FACSIMILE_DEFAULT_ELEMENT, "//teiHeader[1]", AuthorConstants.POSITION_AFTER);
            } catch (AuthorOperationException e) {
                logger.error(e, e);
            }
        }
//          Update the xml:base attribute
        controller.setAttribute(XML_BASE_ATTRIB_NAME, new AttrValue(relativePath), facsimile);

        updateMediaElements();

        controller.endCompoundEdit();
    }

    public void setBaseDirectory(File newDirectory) {
        setBaseDirectoryURL(castFileToURL(newDirectory));
    }

    public void chooseBaseDirectory() {
        File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
        if (dir != null) {
            setBaseDirectory(dir);
        }

        /*AuthorNode currentNode = EMSTOxygenUtils.getCurrentAuthorNode(authorAccess);
        if (currentNode != null && "facsimile".equals(currentNode.getName())) {

            File dir = authorAccess.getWorkspaceAccess().chooseDirectory();
            if (dir != null) {
                AuthorElement facsimile = (AuthorElement) currentNode;
                try {
                    String relativePath = authorAccess.getUtilAccess().makeRelative(
                            authorAccess.getEditorAccess().getEditorLocation(),
                            EMSTUtils.castFileToURL(dir)
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
        SortedMap<Path, String> files = getFiles();
        SortedMap<Path, EMSTMediaElement> mediaPaths = getMediaPaths();

    }

    @NotNull
    public SortedMap<Path, String> getFiles() {
        SortedMap<Path, String> files = new TreeMap<>();

        try (DirectoryStream<Path> dirStream =
                     Files.newDirectoryStream(getBaseDirectory())) {

            for (Path filePath : dirStream) {
                File file = filePath.toFile();
                String mimeType = tika.detect(file);
                if (MEDIA_TYPES.keySet().contains(mimeType)) {
                    files.put(filePath, mimeType);
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
        return files;
    }

    @NotNull
    public List<EMSTMediaElement> getMediaElements() {
        List<EMSTMediaElement> mediaElements = new ArrayList<>();

        List<AuthorElement> contentElements = getContentElements(getFacsimileElement());
        for (AuthorElement element : contentElements) {
            EMSTMediaElement mediaElement = EMSTMediaElement.get(element);
            if (mediaElement != null) {
                mediaElements.add(mediaElement);
            }
        }
        return mediaElements;
    }

    @NotNull
    public SortedMap<Path, EMSTMediaElement> getMediaPaths() {
        SortedMap<Path, EMSTMediaElement> mediaPaths = new TreeMap<>();

        List<EMSTMediaElement> mediaElements = getMediaElements();
        for (EMSTMediaElement mediaElement : mediaElements) {
            Path path = mediaElement.getPath();
            if (path != null) {
                mediaPaths.put(path, mediaElement);
            }
        }
        return mediaPaths;
    }

    @NotNull
    public Map<String, URL> getMediaURLs() {
        Map<String, URL> mediaURLs = new HashMap<>();

        List<EMSTMediaElement> mediaElements = getMediaElements();
        for (EMSTMediaElement mediaElement : mediaElements) {
            String id = mediaElement.getID();
            URL url = mediaElement.getURL();
            if (id != null && url != null) {
                mediaURLs.put(id, url);
            }
        }
        return mediaURLs;
    }

    @Nullable
    public URL getCurrentMediaURL() {
        URL url = null;

        if (currentType == ElementType.MEDIA) {
            EMSTMediaElement mediaElement = EMSTMediaElement.get(currentElement);
            if (mediaElement != null) {
                url = mediaElement.getURL();
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
                openURL(authorAccess, getBaseDirectoryURL());
                break;
            case MEDIA:
                openURL(authorAccess, getCurrentMediaURL());
                break;
            case REFERENCES:
                if (!references.isEmpty()) {
                    Map<String, URL> mediaURLs = getMediaURLs();
                    for (String reference : references) {
                        String id = StringUtils.substringAfter(reference, "#");
                        openURL(authorAccess, mediaURLs.get(id.isEmpty() ? reference : id));
                    }
                }
                break;
        }
    }
}
