package eu.emergingstandards.utils;

import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
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
                String urlPath = URLDecoder.decode(url.getPath(), "UTF-8");
                path = Paths.get(urlPath);
            } catch (UnsupportedEncodingException e) {
                logger.error(e, e);
            }
        }
        return path;
    }

    @Nullable
    public static URL castPathToURL(Path path) {
        URL url = null;

        try {
            url = path.toUri().toURL();
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
        String decodedURL = null;

        if (url != null) {
            decodedURL = decodeURL(url.getPath());
        }
        return decodedURL;
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
}
