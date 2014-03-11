package eu.emergingstandards.commons;

/**
 * Created by mike on 2/5/14.
 */
public enum EMSTNamespaceType {
    XML("xml", "http://www.w3.org/XML/1998/namespace"),
    TEI("tei", "http://www.tei-c.org/ns/1.0");

    /* Instance members */

    private String abbr;
    private String urlID;

    private EMSTNamespaceType(String abbr, String urlID) {
        this.abbr = abbr;
        this.urlID = urlID;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getURLID() {
        return urlID;
    }
}
