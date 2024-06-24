package info.histei.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by mike on 2/5/14.
 */
public class MainUtils {

    private static final Logger logger = LogManager.getLogger(MainUtils.class.getName());

    @Nullable
    public static Path castURLToPath(URL url) {
        Path path = null;

        if (url != null) {
            try {
                path = Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                logger.error(e, e);
            }
        }
        return path;
    }

    @Nullable
    public static File castURLToFile(URL url) {
        File file = null;

        if (url != null) {
            try {
                file = new File(url.toURI());
            } catch (URISyntaxException e) {
                String fileString = castURLToFileString(url);
                if (fileString != null) {
                    file = new File(fileString);
                }
            }
        }
        return file;
    }

    @Nullable
    public static String castURLToFileString(URL url) {
        String fileString = null;

        if (url != null) {
            try {
                fileString = Paths.get(url.toURI()).toString();
            } catch (URISyntaxException e) {
                logger.error(e, e);
            }
        }
        return fileString;
    }

    @Nullable
    public static URL castPathToURL(Path path) {
        URL url = null;

        if (path != null) {
            try {
                url = path.toUri().toURL();
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @Nullable
    public static URL castFileToURL(File file) {
        URL url = null;

        if (file != null) {
            try {
                url = file.toURI().toURL();
            } catch (MalformedURLException e) {
                logger.error(e, e);
            }
        }
        return url;
    }

    @Nullable
    public static String decodeURL(String url) {
        String decodedURL = null;

        if (url != null) {
            try {
                decodedURL = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e, e);
            }
        }
        return decodedURL;
    }

    @Nullable
    public static String encodeURL(String url) {
        String encodedURL = null;

        if (url != null) {
            try {
                encodedURL = URLEncoder.encode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error(e, e);
            }
        }
        return encodedURL;
    }

    private MainUtils() {

    }
}
