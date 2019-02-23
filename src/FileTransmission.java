import config.Configuration;
import config.DefaultConfiguration;
import scan.BroadcastScan;
import scan.Scan;

import java.net.SocketException;

public class FileTransmission {

    private Scan broadcastScan;

    public FileTransmission() throws SocketException {
        this.broadcastScan = new BroadcastScan(new DefaultConfiguration());
    }

    public FileTransmission(Configuration configuration) throws SocketException {
        this.broadcastScan = new BroadcastScan(configuration);
    }

    public Scan getBroadcastScan() {
        return broadcastScan;
    }
}
