package android.net.wifi;

import android.os.Binder;
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
    @RequiresApi(31)
    com.android.wifi.x.com.android.modules.utils.ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks(String packageName, String featureId, Bundle extras);

    //Android 11
    android.content.pm.ParceledListSlice<WifiConfiguration> getPrivilegedConfiguredNetworks(String packageName, String featureId);

    //Android 11+
    boolean removeNetwork(int netId, String packageName);
}