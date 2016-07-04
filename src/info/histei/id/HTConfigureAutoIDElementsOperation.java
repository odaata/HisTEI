package info.histei.id;

import info.histei.extensions.HTUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.commons.id.ConfigureAutoIDElementsOperation;
import ro.sync.ecss.extensions.commons.id.GenerateIDElementsInfo;

/**
 * Created by mike on 1/27/15.
 */
public class HTConfigureAutoIDElementsOperation extends ConfigureAutoIDElementsOperation {
    @Override
    protected GenerateIDElementsInfo getDefaultOptions(AuthorAccess authorAccess) {
        return HTUniqueAttributesRecognizer.GENERATE_ID_DEFAULTS;
    }

    @Override
    protected String getListMessage() {
        return "Element name";
    }

    @Override
    public String getDescription() {
        return "Configure the list of elements for which to generate automatic IDs";
    }
}
