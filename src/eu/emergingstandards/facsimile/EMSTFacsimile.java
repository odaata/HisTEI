package eu.emergingstandards.facsimile;

import eu.emergingstandards.utils.EMSTUtils;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private enum elementType {
        FACSIMILE, MEDIA, REFERENCES, NONE
    }

    private static final String FACSIMILE_ELEMENT_NAME = "facsimile";
    private static final List<String> MEDIA_ELEMENT_NAMES = new ArrayList<>(2);

    static {
        MEDIA_ELEMENT_NAMES.add("media");
        MEDIA_ELEMENT_NAMES.add("graphic");
    }

    private static final String URL_ATTRIB_NAME = "url";
    private static final String FACS_ATTRIB_NAME = "facs";

    private elementType currentType = elementType.NONE;
    private AuthorAccess authorAccess;
    private AuthorElement currentElement;
    private AuthorElement facsimileElement;
    private File directory;
    private Map<String, String> files = new HashMap<>();
    private Map<String, URL> media = new HashMap<>();
    private List<String> references;

    private final Tika tika = new Tika();

    public EMSTFacsimile(AuthorAccess authorAccess) {
        this.authorAccess = authorAccess;
        currentElement = EMSTUtils.getCurrentAuthorElement(authorAccess);

        if (currentElement != null) {
            String elementName = currentElement.getName();

            if (FACSIMILE_ELEMENT_NAME.equals(elementName)) {
                currentType = elementType.FACSIMILE;
            } else if (MEDIA_ELEMENT_NAMES.contains(elementName)) {
                currentType = elementType.MEDIA;
            } else {
                AttrValue facs = currentElement.getAttribute(FACS_ATTRIB_NAME);
                // Reference to <graphic>/<media> beneath <facsimile> - get reference URL from there
                if (facs != null) {
                    currentType = elementType.REFERENCES;
                    references = EMSTUtils.getAttribValues(facs.getValue());
                }
            }
        }
    }

    public AuthorElement getFacsimileElement() {
        if (facsimileElement == null) {
            if (currentType == elementType.FACSIMILE) {
                facsimileElement = currentElement;
            } else {
                facsimileElement = EMSTUtils.getAuthorElement("//facsimile[1]", authorAccess);
            }

            AttrValue base = currentElement.getAttribute("xml:base");
            if (base != null) {
                directory = new File(base.getValue());
            }
        }
        return facsimileElement;
    }

    @Nullable
    public File getDirectory() {
        return directory;
    }

    public void setDirectory(File newDirectory, AuthorAccess authorAccess) {
        files = new HashMap<>();
        this.directory = newDirectory;

        if (newDirectory != null) {
            File[] listFiles = newDirectory.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    try {
                        String mediaType = tika.detect(file);
//                        String mediaType = Files.probeContentType(file.toPath());
                        files.put(file.getName(), mediaType);
                    } catch (IOException e) {
                    }
                }
            }
        }

    }

    public boolean chooseDirectory(AuthorAccess authorAccess) {
        boolean success = false;

        AuthorNode currentNode = EMSTUtils.getCurrentAuthorNode(authorAccess);
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
//                    e.printStackTrace();
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
        }
        return success;
    }

    @NotNull
    public Map<String, String> getFiles() {
        return files;
    }

    @NotNull
    public List<AuthorElement> getMediaElements() {
        List<AuthorElement> elements = new ArrayList<>();

        AuthorElement facsimile = getFacsimileElement();
        if (facsimile != null) {
            for (AuthorNode node : facsimile.getContentNodes()) {
                AuthorElement element = EMSTUtils.castAuthorElement(node);
                if (element != null) {
                    elements.add(element);
                }
            }
        }
        return elements;
    }

    @NotNull
    public Map<String, URL> getMedia() {
        media = new HashMap<>();

        List<AuthorElement> elements = getMediaElements();
        for (AuthorElement element : elements) {
            AttrValue idAttr = element.getAttribute(EMSTUtils.XML_ID_ATTR_NAME);
            if (idAttr != null) {
                media.put(idAttr.getValue(), getURL(element));
            }
        }
        return media;
    }

    private URL getURL(AuthorElement authorElement) {
        URL url = null;

        AttrValue urlAttr = authorElement.getAttribute(URL_ATTRIB_NAME);
        if (urlAttr != null) {
            try {
                url = new URL(urlAttr.getValue());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return url;
    }

    @Nullable
    public URL getCurrentURL() {
        URL url = null;

        if (currentType == elementType.MEDIA && currentElement != null) {
            url = getURL(currentElement);
        }
        return url;
    }

    @NotNull
    public List<String> getReferences() {
        return references;
    }
}
