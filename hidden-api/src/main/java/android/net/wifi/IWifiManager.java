package android.net.wifi;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.RequiresApi;

public interface IWifiManager extends IInterface {

    abstract class Stub extends Binder implements IWifiManager {
        public static IWifiManager asInterface(IBinder obj) {
            throw new RuntimeException("Stub!");
        }
    }

    //Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    WifiManager.AddNetworkResult addOrUpdateNetworkPrivileged(WifiConfiguration config, String packageName);

    //Android 10+
    int addOrUpdateNetwork(WifiConfiguration config, String packageName);

    boolean removeNetwork(int netId, String packageName);

    boolean enableNetwork(int netId, boolean disableOthers, String packageName);

    boolean reconnect(String packageName);

    //Android 11+
    @RequiresApi(Build.VERSION_CODES.R)
    WifiInfo getConnectionInfo(String callingPackage, String callingFeatureId);

    //Android 10
    WifiInfo getConnectionInfo(String callingPackage);

    int getWifiEnabledState();

    boolean disconnect(String packageName);

    //Android 15+
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    void connect(WifiConfiguration config, int netId, IActionListener listener, String packageName, Bundle extras);

    //Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    void connect(WifiConfiguration config, int netId, IActionListener listener, String packageName);

    //Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    void connect(WifiConfiguration config, int netId, IActionListener listener);

    //Android 11
    @RequiresApi(Build.VERSION_CODES.R)
    void connect(WifiConfiguration config, int netId, IBinder binder, IActionListener listener, int callbackIdentifier);

    //Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    void save(WifiConfiguration config, IActionListener listener, String packageName);

    //Android 12
    @RequiresApi(Build.VERSION_CODES.S)
    void save(WifiConfiguration config, IActionListener listener);

    //Android 11
    @RequiresApi(Build.VERSION_CODES.R)
    void save(WifiConfiguration config, IBinder binder, IActionListener listener, int callbackIdentifier);

    //Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    void forget(int netId, IActionListener listener);

    //Android 11
    @RequiresApi(Build.VERSION_CODES.R)
    void forget(int netId, IBinder binder, IActionListener listener, int callbackIdentifier);
}