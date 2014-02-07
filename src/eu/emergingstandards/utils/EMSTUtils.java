package eu.emergingstandards.utils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by mike on 2/5/14.
 */
public class EMSTUtils {

    private static final Logger logger = Logger.getLogger(EMSTUtils.class.getName());

    @Nullable
    public static Path castURLToPath(URL url) {
        return Paths.get(decodeURL(url));
    }

    @Nullable
    public static URL castPathToURL(Path path) {
        URL url = null;

        try {
            url = path.toUri().toURL();
//            path.toUri().toURL();
        } catch (MalformedURLException e) {
            logger.error(e, e);
        }
        return url;
    }

    @Nullable
    public static URL castFileToURL(File file) {
        URL url = null;

        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            logger.error(e, e);
        }
        return url;
    }

    @Nullable
    public static String decodeURL(URL url) {
        return decodeURL(url.toString());
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
    public static String encodeURL(URL url) {
        return encodeURL(url.toString());
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

    /*@Nullable
    public static String addSlash(String directory) {
        if (directory != null && !directory.endsWith("/")) {
            return directory.substring(0, directory.lastIndexOf("/") + 1);
        } else {
            return directory;
        }
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

    private EMSTUtils() {

    }
}
