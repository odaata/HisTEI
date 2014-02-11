package eu.emergingstandards.utils;

import net.sf.saxon.s9api.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static eu.emergingstandards.utils.EMSTUtils.nullToEmpty;

/**
 * Created by mike on 2/10/14.
 */
public class EMSTSaxonUtils {

    @NotNull
    public static List<XdmNode> getChildNodes(XdmNode parentNode, String childName, String namespace) {
        List<XdmNode> childNodes = new ArrayList<>();

        if (parentNode != null && childName != null) {
            QName childQName = new QName(nullToEmpty(namespace), childName);
            XdmSequenceIterator axisIterator = parentNode.axisIterator(Axis.CHILD, childQName);
            if (axisIterator.hasNext()) {
                XdmItem childItem = axisIterator.next();
                if (!childItem.isAtomicValue()) {
                    childNodes.add((XdmNode) childItem);
                }
            }
        }
        return childNodes;
    }

    @NotNull
    public static List<XdmNode> getChildNodes(XdmNode parentNode, String childName) {
        return getChildNodes(parentNode, childName, "");
    }

    @Nullable
    public static XdmNode getChildNode(XdmNode parentNode, String childName, String namespace) {
        List<XdmNode> childNodes = getChildNodes(parentNode, childName, namespace);

        if (!childNodes.isEmpty()) {
            return childNodes.get(0);
        } else {
            return null;
        }
    }

    @Nullable
    public static XdmNode getChildNode(XdmNode parentNode, String childName) {
        return getChildNode(parentNode, childName, "");
    }

    @Nullable
    public static String getChildText(XdmNode parentNode, String childName, String namespace) {
        XdmNode childNode = getChildNode(parentNode, childName, namespace);
        if (childNode != null) {
            return childNode.getStringValue();
        } else {
            return null;
        }
    }

    @Nullable
    public static String getChildText(XdmNode parentNode, String childName) {
        return getChildText(parentNode, childName, "");
    }

    private EMSTSaxonUtils() {

    }
}
