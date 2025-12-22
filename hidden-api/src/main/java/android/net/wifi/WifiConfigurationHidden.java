package android.net.wifi;

import android.content.pm.PackageManager;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import java.util.BitSet;

import dev.rikka.tools.refine.RefineAs;

@SuppressWarnings("deprecation")
@RefineAs(WifiConfiguration.class)
public class WifiConfigurationHidden {

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
     */
    public String[] wepKeys;

    /**
     * Default WEP key index, ranging from 0 to 3.
     * Due to security and performance limitations, use of WEP networks
     * is discouraged.
     */
    @IntRange(from = 0, to = 3)
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
     * The set of security protocols supported by this configuration.
     * See {@link WifiConfiguration.Protocol} for descriptions of the values.
     * Defaults to WPA RSN.
     */
    @NonNull
    public BitSet allowedProtocols;

    /**
     * The set of authentication protocols supported by this configuration.
     * See {@link WifiConfiguration.AuthAlgorithm} for descriptions of the values.
     * Defaults to automatic selection.
     */
    @NonNull
    public BitSet allowedAuthAlgorithms;

    /**
     * The set of pairwise ciphers for WPA supported by this configuration.
     * See {@link WifiConfiguration.PairwiseCipher} for descriptions of the values.
     * Defaults to CCMP TKIP.
     */
    @NonNull
    public BitSet allowedPairwiseCiphers;

    /**
     * The set of group ciphers supported by this configuration.
     * See {@link WifiConfiguration.GroupCipher} for descriptions of the values.
     * Defaults to CCMP TKIP WEP104 WEP40.
     */
    @NonNull
    public BitSet allowedGroupCiphers;

    /**
     * The set of group management ciphers supported by this configuration.
     * See {@link WifiConfiguration.GroupMgmtCipher} for descriptions of the values.
     */
    @NonNull
    public BitSet allowedGroupManagementCiphers;

    /**
     * The set of SuiteB ciphers supported by this configuration.
     * To be used for WPA3-Enterprise mode. Set automatically by the framework based on the
     * certificate type that is used in this configuration.
     */
    @NonNull
    public BitSet allowedSuiteBCiphers;

    /**
     * True if this network configuration is visible to and usable by other users on the
     * same device, false otherwise.
     */
    public boolean shared;

    /**
     * Universal name for app creating the configuration
     * see {@link PackageManager#getNameForUid(int)}
     */
    public String creatorName;

    /**
     * Universal name for app updating the configuration
     * see {@link PackageManager#getNameForUid(int)}
     */
    public String lastUpdateName;

    /**
     * Auto-join is allowed by user for this network.
     * Default true.
     */
    public boolean allowAutojoin;

    /**
     * Last time the system was connected to this configuration represented as the difference,
     * measured in milliseconds, between the last connected time and midnight, January 1, 1970 UTC.
     * <p>
     * Note that this information is only in memory will be cleared (reset to 0) for all
     * WifiConfiguration(s) after a reboot.
     */
    public long lastConnected;

    /**
     * Last time the system was disconnected to this configuration.
     */
    public long lastDisconnected;

    /**
     * Indicate that a WifiConfiguration is temporary and should not be saved
     * nor considered by AutoJoin.
     */
    public boolean ephemeral;

    /**
     * Indicate that a WifiConfiguration is temporary and should not be saved
     * nor considered by AutoJoin.
     */
    public boolean isEphemeral() {
        throw new RuntimeException("Stub!");
    }

    /**
     * True if this Wifi configuration is created from a {@link WifiNetworkSuggestion},
     * false otherwise.
     */
    public boolean fromWifiNetworkSuggestion;

    /**
     * True if this Wifi configuration is created from a {@link WifiNetworkSpecifier},
     * false otherwise.
     */
    public boolean fromWifiNetworkSpecifier;

    /**
     * Get the SSID in a human-readable format, with all additional formatting removed
     * e.g. quotation marks around the SSID, "P" prefix
     */
    @NonNull
    public String getPrintableSsid() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Get the authentication type of the network.
     *
     * @return One of the {@link WifiConfiguration.KeyMgmt} constants. e.g. {@link WifiConfiguration.KeyMgmt#WPA2_PSK}.
     */
    public int getAuthType() {
        throw new RuntimeException("Stub!");
    }
}
