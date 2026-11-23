package io.github.wifi_password_manager;

import android.net.wifi.IActionListener;
import io.github.wifi_password_manager.ipc.WifiNetworkListParcel;
import io.github.wifi_password_manager.ipc.WifiNetworkParcel;
import io.github.wifi_password_manager.ipc.WifiInfoParcel;

interface IWifiRootService {
    WifiNetworkListParcel getPrivilegedConfiguredNetworks();

    int addOrUpdateNetworkPrivileged(in WifiNetworkParcel config);

    boolean removeNetwork(int netId);

    boolean enableNetwork(int netId);

    boolean reconnect();

    void forget(int netId, in IActionListener listener);

    WifiInfoParcel getConnectionInfo();

    void persistEphemeralNetworks();

    boolean disconnect();

    void connect(in WifiNetworkParcel config, int netId, in IActionListener listener);
}

