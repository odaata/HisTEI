package eu.emergingstandards.monitor;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;

/**
 * Created by mike on 2/1/14.
 */
public class EMSTFileListenerAdaptor implements FileListener {
    /**
     * Called when a file is created.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {

    }

    /**
     * Called when a file is deleted.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {

    }

    /**
     * Called when a file is changed.<br />
     * This will only happen if you monitor the file using {@link org.apache.commons.vfs2.FileMonitor}.
     *
     * @param event The FileChangeEvent.
     * @throws Exception if an error occurs.
     */
    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {

    }
}
