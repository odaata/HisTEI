package eu.emergingstandards.facsimile;

import eu.emergingstandards.exceptions.EMSTException;
import eu.emergingstandards.exceptions.EMSTFileMissingException;
import eu.emergingstandards.utils.EMSTXMLUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorConstants;
import ro.sync.ecss.extensions.api.AuthorDocumentController;
import ro.sync.ecss.extensions.api.AuthorOperationException;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static eu.emergingstandards.utils.EMSTOxygenUtils.*;
import static eu.emergingstandards.utils.EMSTUtils.*;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private static enum ElementType {
        FACSIMILE, MEDIA, REFERENCES
    }

    private static final Logger logger = Logger.getLogger(EMSTFacsimile.class.getName());

    private static final String FACSIMILE_ELEMENT_NAME = "facsimile";
    private static final String FACS_ATTRIB_NAME = "facs";

    @Nullable
    public static EMSTFacsimile get(AuthorAccess authorAccess) {
        EMSTFacsimile facsimile = null;

        AuthorElement currentElement = getCurrentAuthorElement(authorAccess);

        if (currentElement != null) {
            String elementName = currentElement.getName();
            ElementType currentType = null;
            List<String> references = new ArrayList<>();

            if (FACSIMILE_ELEMENT_NAME.equals(elementName)) {
                currentType = ElementType.FACSIMILE;
            } else if (EMSTMediaElement.MEDIA_ELEMENT_NAMES.contains(elementName)) {
                currentType = ElementType.MEDIA;
            } else {
                references = getAttrValues(currentElement.getAttribute(FACS_ATTRIB_NAME));
                if (!references.isEmpty()) {
                    currentType = ElementType.REFERENCES;
                }
            }
            if (currentType != null) {
                facsimile = new EMSTFacsimile(
                        authorAccess, currentElement, currentType, references);
            }
        }
        return facsimile;
    }

    @Nullable
    public static Element create() {
        return EMSTXMLUtils.createElement(FACSIMILE_ELEMENT_NAME);
    }

    @Nullable
    public static Element create(String xmlBase) {
        Element element = create();

        if (element != null) {
            element.setAttribute(EMSTXMLUtils.XML_BASE_ATTRIB_NAME, xmlBase);
        }
        return element;
    }

    /* Instance members */

    private ElementType currentType;
    private AuthorAccess authorAccess;
    private AuthorElement currentElement;
    private AuthorElement facsimileElement;
    private List<String> references = new ArrayList<>();
    private File baseDirectory;

    private final Tika tika = new Tika();

    protected EMSTFacsimile(AuthorAccess authorAccess, AuthorElement currentElement,
                            ElementType currentType, List<String> references) {
        this.authorAccess = authorAccess;
        this.currentElement = currentElement;
        this.currentType = currentType;
        if (references != null) this.references = references;
    }

    /*public EMSTFacsimile(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;
        currentElement = getCurrentAuthorElement(authorAccess);

        if (currentElement != null) {
            String elementName = currentElement.getName();

            if (FACSIMILE_ELEMENT_NAME.equals(elementName)) {
                currentType = ElementType.FACSIMILE;
            } else if (EMSTMediaElement.MEDIA_ELEMENT_NAMES.contains(elementName)) {
                currentType = ElementType.MEDIA;
            } else {
                references = getAttrValues(currentElement.getAttribute(FACS_ATTRIB_NAME));
                if (!references.isEmpty()) {
                    currentType = ElementType.REFERENCES;
                }
            }
        }
    }*/

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
    public AuthorElement createFacsimileElement() throws EMSTException {
        AuthorElement facsimile = getFacsimileElement();

        if (facsimile == null) {
            Element newElement = create();
            if (newElement != null) {
                try {
                    authorAccess.getDocumentController().insertXMLFragment(
                            newElement.toString(), "//teiHeader[1]", AuthorConstants.POSITION_AFTER);

                    facsimileElement = getFacsimileElement();
                } catch (AuthorOperationException e) {
                    logger.error(e, e);
                    throw new EMSTException("The <facsimile> element could not be created!", e);
                }
            }
        }
        return facsimileElement;
    }

    @Nullable
    public String getXMLBase() {
        String xmlBase = null;

        AuthorElement facsimile = getFacsimileElement();
        if (facsimile != null) {
            xmlBase = getAttrValue(facsimile.getAttribute(EMSTXMLUtils.XML_BASE_ATTRIB_NAME));
        }
        return xmlBase;
    }

    @Nullable
    public File getBaseDirectoryFile() {
        return baseDirectory;
    }

    @Nullable
    public Path getBaseDirectory() {
        Path directory = null;

        URL url = getBaseDirectoryURL();
        if (url != null) {
            directory = castURLToPath(url);
            if (directory != null) {
                String dir = directory.toString();
                if (dir != null && !dir.endsWith("/")) {
                    directory = Paths.get(dir + "/");
                }
            }
        }
        return directory;
    }

    @Nullable
    public URL getBaseDirectoryURL() {
        URL directory = null;

        AuthorElement facsimile = getFacsimileElement();
        if (facsimile != null) {
            directory = facsimile.getXMLBaseURL();
            String dir = directory.toString();

            if (!dir.endsWith("/")) {
                try {
                    directory = new URL(dir.substring(0, dir.lastIndexOf("/") + 1));
                } catch (MalformedURLException e) {
                    logger.error(e, e);
                }
            }
            /*Path path = castURLToPath(directory);
            if (path != null && !Files.isDirectory(path)) {
                directory = castPathToURL(path.getParent());
            }*/
        }
        return directory;
    }

    public void setBaseDirectory(File newDirectory) throws EMSTException {
        baseDirectory = newDirectory;
        setBaseDirectoryURL(castFileToURL(newDirectory));
    }

    public void setBaseDirectoryURL(URL newDirectory) throws EMSTException {
        AuthorDocumentController controller = authorAccess.getDocumentController();
        controller.beginCompoundEdit();

//      Create facsimile element if it doesn't exist
        AuthorElement facsimile;
        try {
            facsimile = createFacsimileElement();
        } catch (EMSTException e) {
            controller.cancelCompoundEdit();
            logger.error(e, e);
            throw new EMSTException(e);
        }

        if (facsimile != null) {
            URL currentDirectory = getBaseDirectoryURL();
//          Do the uglies
            if (currentDirectory != null && currentDirectory.equals(newDirectory)) {
                controller.cancelCompoundEdit();
                throw new EMSTException("The chosen directory is already selected.");
            }
//          It's all relative after all...
            String relativePath = makeRelative(authorAccess, facsimile.getParent().getXMLBaseURL(), newDirectory);
//          Update the xml:base attribute
            controller.setAttribute(EMSTXMLUtils.XML_BASE_ATTRIB_NAME, new AttrValue(relativePath), facsimile);
//          Put a stop to all them changes!
            controller.endCompoundEdit();
        } else {
            controller.cancelCompoundEdit();
            throw new EMSTException("The <facsimile> element could not be located!");
        }
    }

    public void updateMediaElements() throws EMSTException {
        AuthorElement facsimile = getFacsimileElement();
        if (facsimile == null) throw new EMSTException("The <facsimile> element could not be located!");
//      Talk to the hand!
        TreeMap<Path, String> files = getFiles();
        if (files.isEmpty()) throw new EMSTException("There are no files to be added!");

//      Create new media elements using existing files
        List<String> newElements = new ArrayList<>(files.size());
        int counter = 1;
//      Jack through the file paths and spit up some gamey Elements
        for (Path path : files.keySet()) {
//          Get together everything we'll need
            String relativePath = makeRelative(authorAccess, facsimile.getXMLBaseURL(), castPathToURL(path));
            String fileName = path.getFileName().toString();
            String mimeType = files.get(path);
            EMSTMediaType mediaType = EMSTMediaType.get(mimeType);
//          Make sure we've got everything
            if (relativePath != null && mimeType != null && mediaType != null) {
                String id = mediaType.getIdAbbr() + "_" + String.format("%03d", counter);
//              Now, we create a pretty little element over there, yeah, that's nice
                Element newElement = EMSTMediaElement.create(mimeType, id, fileName);
                if (newElement != null) {
                    newElements.add(newElement.toString());
                    counter++;
                }
            }
        }
        if (!newElements.isEmpty()) {
            AuthorDocumentController controller = authorAccess.getDocumentController();
            controller.beginCompoundEdit();
//          Remove all existing elements first
            removeAllMediaElements();
//          Now bundle all that shiznit up into a big ol' string and get ready to insert it
            String mediaFragment = StringUtils.join(newElements, "\n");

            try {  // Cuz, baby ya got to keep on trying...
                controller.insertXMLFragment(mediaFragment, facsimile, AuthorConstants.POSITION_INSIDE_FIRST);
            } catch (AuthorOperationException e) {
//              Yo! Shit blew up, yo! This should never, ever happen
                controller.cancelCompoundEdit();
                logger.error(e, e);  // Yes, tell the officer what the mean controller did to you
                throw new EMSTException("An error occurred while inserting references to the new files!", e);
            }
            controller.endCompoundEdit();
        } else {
            throw new EMSTException("There are no files to be added!");
        }
    }

    private void removeAllMediaElements() {
        AuthorDocumentController controller = authorAccess.getDocumentController();

        for (EMSTMediaElement mediaElement : getMediaElements()) {
            controller.deleteNode(mediaElement.getAuthorElement());
        }
    }

    @NotNull
    public TreeMap<Path, String> getFiles() {
        TreeMap<Path, String> filesMap = new TreeMap<>();

        File directory = getBaseDirectoryFile();
        if (directory != null) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    try {
                        String mimeType = tika.detect(file);
                        if (EMSTMediaType.isAllowed(mimeType)) {
                            filesMap.put(file.toPath(), mimeType);
                        }
                    } catch (IOException e) {
                        logger.error(e, e);
                    }
                }
            }
        }

        /*try (DirectoryStream<Path> dirStream =
                     Files.newDirectoryStream(getBaseDirectory())) {

            for (Path filePath : dirStream) {
                File file = filePath.toFile();
                String mimeType = tika.detect(file);

                if (EMSTMediaType.isAllowed(mimeType)) {
                    filesMap.put(filePath, mimeType);
                }
            }
        } catch (IOException e) {
            logger.error(e, e);
        }*/
        return filesMap;
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
