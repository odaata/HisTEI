package info.histei.id;

import info.histei.extensions.HTUniqueAttributesRecognizer;
import ro.sync.ecss.extensions.api.UniqueAttributesRecognizer;
import ro.sync.ecss.extensions.commons.id.GenerateIDsOperation;

/**
 * Created by mike on 1/27/15.
 */
public class GenerateIDsHTOperation extends GenerateIDsOperation {
    @Override
    protected UniqueAttributesRecognizer getUniqueAttributesRecognizer() {
        return new HTUniqueAttributesRecognizer();
    }
}
