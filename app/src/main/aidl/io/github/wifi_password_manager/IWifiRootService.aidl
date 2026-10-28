package io.github.wifi_password_manager;

import android.net.wifi.IActionListener;
import io.github.wifi_password_manager.ipc.WifiNetworkParcel;
import io.github.wifi_password_manager.ipc.WifiInfoParcel;

interface IWifiRootService {
    List<WifiNetworkParcel> getPrivilegedConfiguredNetworks();

    int addOrUpdateNetworkPrivileged(in WifiNetworkParcel config);

    boolean removeNetwork(int netId);

    oneway void forget(int netId, in IActionListener listener);

    WifiInfoParcel getConnectionInfo();

    void persistEphemeralNetworks();

    boolean disconnect();

    oneway void connect(in WifiNetworkParcel config, int netId, in IActionListener listener);
}

