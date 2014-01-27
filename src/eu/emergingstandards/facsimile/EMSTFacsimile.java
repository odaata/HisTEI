package eu.emergingstandards.facsimile;

import org.apache.tika.Tika;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.ecss.extensions.api.AuthorAccess;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 1/25/14.
 */
public class EMSTFacsimile {

    private File directory;
    private Map<String, String> files = new HashMap<>();

    private final Tika tika = new Tika();

    public EMSTFacsimile(File directory) {
        this.directory = directory;
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

        }

        return success;
    }

    @NotNull
    public Map<String, String> getFiles() {
        return files;
    }
}
