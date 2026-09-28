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

    //Android 11
    int addOrUpdateNetwork(WifiConfiguration config, String packageName);

    //Android 11+
    boolean removeNetwork(int netId, String packageName);

    WifiInfo getConnectionInfo(String callingPackage, String callingFeatureId);

    int getWifiEnabledState();

    boolean disconnect(String packageName);

    //Android 14+
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    void connect(WifiConfiguration config, int netId, IActionListener listener, String packageName, Bundle extras);

    //Android 13
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    void connect(WifiConfiguration config, int netId, IActionListener listener, String packageName);

    //Android 12 & 12L
    @RequiresApi(Build.VERSION_CODES.S)
    void connect(WifiConfiguration config, int netId, IActionListener listener);

    //Android 11
    void connect(WifiConfiguration config, int netId, IBinder binder, IActionListener listener, int callbackIdentifier);

    //Android 13+
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    void save(WifiConfiguration config, IActionListener listener, String packageName);

    //Android 12
    @RequiresApi(Build.VERSION_CODES.S)
    void save(WifiConfiguration config, IActionListener listener);

    //Android 11
    void save(WifiConfiguration config, IBinder binder, IActionListener listener, int callbackIdentifier);

    //Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    void forget(int netId, IActionListener listener);

    //Android 11
    void forget(int netId, IBinder binder, IActionListener listener, int callbackIdentifier);
}