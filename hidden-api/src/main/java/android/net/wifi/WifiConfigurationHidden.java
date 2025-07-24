package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.BitSet;

import dev.rikka.tools.refine.RefineAs;

@SuppressWarnings("deprecation")
@RefineAs(WifiConfiguration.class)
public class WifiConfigurationHidden implements Parcelable {

    public WifiConfigurationHidden() {
        throw new RuntimeException("Stub!");
    }

    /**
     * The ID number that the supplicant uses to identify this
     * network configuration entry. This must be passed as an argument
     * to most calls into the supplicant.
     */
    public int networkId;

    /**
     * The network's SSID. Can either be a UTF-8 string,
     * which must be enclosed in double quotation marks
     * (e.g., {@code "MyNetwork"}), or a string of
     * hex digits, which are not enclosed in quotes
     * (e.g., {@code 01a243f405}).
     */
    public String SSID;

    /**
     * When set, this network configuration entry should only be used when
     * associating with the AP having the specified BSSID. The value is
     * a string in the format of an Ethernet MAC address, e.g.,
     * <code>XX:XX:XX:XX:XX:XX</code> where each <code>X</code> is a hex digit.
     */
    public String BSSID;

    /**
     * Pre-shared key for use with WPA-PSK. Either an ASCII string enclosed in
     * double quotation marks (e.g., {@code "abcdefghij"} for PSK passphrase or
     * a string of 64 hex digits for raw PSK.
     * <p/>
     * When the value of this key is read, the actual key is
     * not returned, just a "*" if the key has a value, or the null
     * string otherwise.
     */
    public String preSharedKey;

    /**
     * Four WEP keys. For each of the four values, provide either an ASCII
     * string enclosed in double quotation marks (e.g., {@code "abcdef"}),
     * a string of hex digits (e.g., {@code 0102030405}), or an empty string
     * (e.g., {@code ""}).
     * <p/>
     * When the value of one of these keys is read, the actual key is
     * not returned, just a "*" if the key has a value, or the null
     * string otherwise.
     *
     * @deprecated Due to security and performance limitations, use of WEP networks
     * is discouraged.
     */
    @Deprecated
    public String[] wepKeys;

    /**
     * Default WEP key index, ranging from 0 to 3.
     *
     * @deprecated Due to security and performance limitations, use of WEP networks
     * is discouraged.
     */
    @Deprecated
    public int wepTxKeyIndex;

    /**
     * This is a network that does not broadcast its SSID, so an
     * SSID-specific probe request must be used for scans.
     */
    public boolean hiddenSSID;

    /**
     * The set of key management protocols supported by this configuration.
     * See {@link WifiConfiguration.KeyMgmt} for descriptions of the values.
     * Defaults to WPA-PSK WPA-EAP.
     */
    @NonNull
    public BitSet allowedKeyManagement;

    /**
     * Auto-join is allowed by user for this network.
     * Default true.
     */
    public boolean allowAutojoin = true;

    public String getPrintableSsid() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Return a String that can be used to uniquely identify this WifiConfiguration.
     * <br />
     * Note: Do not persist this value! This value is not guaranteed to remain backwards compatible.
     */
    @NonNull
    public String getKey() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Implement the Parcelable interface
     */
    public static final @NonNull Creator<WifiConfigurationHidden> CREATOR = new Creator<>() {
        @Override
        public WifiConfigurationHidden createFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public WifiConfigurationHidden[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };

    /**
     * Implement the Parcelable interface
     */
    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Implement the Parcelable interface
     */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        throw new RuntimeException("Stub!");
    }
}
