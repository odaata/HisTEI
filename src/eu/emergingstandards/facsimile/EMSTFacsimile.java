package eu.emergingstandards.facsimile;

import eu.emergingstandards.utils.EMSTUtils;
import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.node.AuthorNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private static final String ELEMENT_NAME = "facsimile";
    private static final String URL_ATTRIB_NAME = "url";

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
            if (currentNode != null && currentNode.getName().equals(ELEMENT_NAME)) {
//                ((AuthorElement) currentNode).getAttribute()
            }
        }
    }

    @Nullable
    public File getDirectory() {
        return directory;
    }

    @NotNull
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
                        continue;
                    }
                }
            }
        }

    }

    @NotNull
    public boolean chooseDirectory(AuthorAccess authorAccess) {
        boolean success = false;
        File dir = authorAccess.getWorkspaceAccess().chooseDirectory();

        if (dir != null && !directory.equals(dir)) {
            authorAccess.getWorkspaceAccess().showInformationMessage(
                    "This directory has already been set. Would you like to update the graphic and media elements?");
        }

        return success;
    }

    @NotNull
    public Map<String, String> getFiles() {
        return files;
    }
}
