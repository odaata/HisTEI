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
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private static final String ELEMENT_NAME = "facsimile";
    private static final String URL_ATTRIB_NAME = "url";

    private AuthorAccess authorAccess;
    private File directory;
    private Map<String, String> files = new HashMap<>();

    private final Tika tika = new Tika();

    public EMSTFacsimile(File directory) {
        this.directory = directory;
    }

    public EMSTFacsimile(Path directory) {
        if (directory != null) this.directory = directory.toFile();
    }

    public EMSTFacsimile(AuthorAccess authorAccess) {
        if (authorAccess != null) {
            AuthorNode currentNode = EMSTUtils.getCurrentAuthorNode(authorAccess);
            if (currentNode != null) {

            }
        }
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
}
