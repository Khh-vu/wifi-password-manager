package android.net.wifi;

import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;

import androidx.annotation.RequiresApi;

@SuppressWarnings("deprecation")
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
}