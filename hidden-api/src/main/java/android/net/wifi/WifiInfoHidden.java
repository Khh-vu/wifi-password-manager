package android.net.wifi;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(WifiInfo.class)
public class WifiInfoHidden {
    public void setSSID(WifiSsid wifiSsid) {
        throw new RuntimeException("Stub!");
    }

    public void setRssi(int rssi) {
        throw new RuntimeException("Stub!");
    }
}
