package info.histei.commons;

import info.histei.utils.MainUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by mike on 5/9/14.
 */
public abstract class AbstractUniqueAttribute<C> implements UniqueAttribute<C> {

    protected final String attributeName;
    protected final String elementName;
    protected final String parentElementName;

    public AbstractUniqueAttribute(String attributeName, String elementName, String parentElementName) {
        this.attributeName = MainUtils.nullToEmpty(attributeName);
        this.elementName = elementName;
        this.parentElementName = parentElementName;
    }

    @Override
    @NotNull
    public String getAttributeName() {
        return attributeName;
    }

    @Override
    @Nullable
    public String getElementName() {
        return elementName;
    }

    @Override
    @Nullable
    public String getParentElementName() {
        return parentElementName;
    }

    @Override
    public abstract boolean matches(C context);

//    Helper method so different implementations can use the same comparison method
/*    protected boolean matches(String attr, String elem, String parentElem) {
        if (!attributeName.equals(attr)) return false;

        if (elementName != null) {
            if (!elementName.equals(elem)) return false;

            if (parentElementName != null) {
                if (!parentElementName.equals(parentElem)) return false;
            }
        }
        return true;
    }*/

    /* Override equals() and hashcode() so they can be used in Lists and as keys in HashMaps */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractUniqueAttribute that = (AbstractUniqueAttribute) o;

        if (!attributeName.equals(that.attributeName)) return false;
        if (elementName != null ? !elementName.equals(that.elementName) : that.elementName != null) return false;
        if (parentElementName != null ? !parentElementName.equals(that.parentElementName) : that.parentElementName != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = attributeName.hashCode();
        result = 31 * result + (elementName != null ? elementName.hashCode() : 0);
        result = 31 * result + (parentElementName != null ? parentElementName.hashCode() : 0);
        return result;
    }

}
