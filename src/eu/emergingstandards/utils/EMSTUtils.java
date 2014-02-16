package eu.emergingstandards.utils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by mike on 2/5/14.
 */
public class EMSTUtils {

    private static final Logger logger = Logger.getLogger(EMSTUtils.class.getName());

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

    /*@Nullable
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
    }*/

    @Nullable
    public static String emptyToNull(String input) {
        String str = null;

        if (input != null) {
            str = input.trim();
            if (str.isEmpty()) str = null;
        }
        return str;
    }

    @NotNull
    public static String nullToEmpty(String input) {
        if (input == null) {
            return "";
        } else {
            return input.trim();
        }
    }

    private EMSTUtils() {

    }
}
