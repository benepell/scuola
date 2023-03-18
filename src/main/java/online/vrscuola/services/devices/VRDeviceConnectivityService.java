package online.vrscuola.services.devices;

import online.vrscuola.utilities.Utilities;

public interface VRDeviceConnectivityService {

    String viewConnect(Utilities utilities, String macAddress, String note);

    boolean valid(String macAddress, String code);

    void connect(boolean updating,Utilities utilities, String macAddress, String username, String note);
}
