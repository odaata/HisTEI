package eu.emergingstandards.commons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ro.sync.util.editorvars.EditorVariables;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static eu.emergingstandards.utils.EMSTOxygenUtils.expandOxygenPath;
import static eu.emergingstandards.utils.EMSTOxygenUtils.getCurrentAuthorAccess;

/**
 * Created by mike on 2/9/14.
 */
public class EMSTIcon {

//    private static final Logger logger = Logger.getLogger(EMSTIcon.class.getName());

    public static final List<Integer> SIZES = Arrays.asList(16, 20);
    public static final Integer DEFAULT_SIZE = 16;

    private static final String NAME_SIZE_SEPARATOR = "-";
    private static final String IMAGE_EXTENSION = ".png";
    private static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        /* Only return the default 16x16 png files,
            since each icon has multiple files and this is for a unique list */
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(DEFAULT_SIZE.toString() + IMAGE_EXTENSION);
        }
    };

    private static final String IMAGES_BASE_PATH = EditorVariables.FRAMEWORK_DIRECTORY + "/images/";

    @Nullable
    public static Path getImagesDirectory() {
        return expandOxygenPath(IMAGES_BASE_PATH, getCurrentAuthorAccess());
    }

    private static final Map<String, EMSTIcon> icons = new HashMap<>();

    @NotNull
    public static Map<String, EMSTIcon> getIcons() {
        if (icons.isEmpty()) {
            Path imagesDirectory = getImagesDirectory();
            if (imagesDirectory != null) {
                File[] listFiles = imagesDirectory.toFile().listFiles(IMAGE_FILTER);
                if (listFiles != null) {
                    synchronized (icons) {
                        for (File file : listFiles) {
                            String fileName = file.getName();
                            String name = fileName.substring(0, fileName.indexOf(NAME_SIZE_SEPARATOR));
                            icons.put(name, new EMSTIcon(name));
                        }
                    }
                }
            }
        }
        return icons;
    }

    @Nullable
    public static EMSTIcon get(String name) {
        return getIcons().get(name);
    }

    /* Instance Members */

    private String name;
    private Integer size = DEFAULT_SIZE;

    protected EMSTIcon(String name) {
        this.name = name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) throws IllegalArgumentException {
        if (SIZES.contains(size)) {
            this.size = size;
        } else {
            throw new IllegalArgumentException("Invalid size! Valid sizes are: " + SIZES.toString());
        }
    }

    @NotNull
    public String getFileName() {
        return name + NAME_SIZE_SEPARATOR + size.toString() + IMAGE_EXTENSION;
    }

    @NotNull
    public String getPath() {
        return IMAGES_BASE_PATH + getFileName();
    }

}
