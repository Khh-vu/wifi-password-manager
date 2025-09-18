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
     * Set the various security params to correspond to the provided security type.
     * This is accomplished by setting the various BitSets exposed in WifiConfiguration.
     */
    public void setSecurityParams(int securityType) {
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
     */
    public String[] wepKeys;

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
     * True if this network configuration is visible to and usable by other users on the
     * same device, false otherwise.
     */
    public boolean shared;

    /**
     * Auto-join is allowed by user for this network.
     * Default true.
     */
    public boolean allowAutojoin;

    /**
     * Get the SSID in a human-readable format, with all additional formatting removed
     * e.g. quotation marks around the SSID, "P" prefix
     */
    @NonNull
    public String getPrintableSsid() {
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
