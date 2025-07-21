package android.net.wifi;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.BitSet;

import dev.rikka.tools.refine.RefineAs;

@SuppressWarnings("deprecation")
@RefineAs(WifiConfiguration.class)
public class WifiConfigurationHidden implements Parcelable {

    /**
     * Recognized key management schemes.
     */
    public static class KeyMgmt {
        private KeyMgmt() {
        }

        @Retention(RetentionPolicy.SOURCE)
        @IntDef(value = {NONE, WPA_PSK, WPA_EAP, IEEE8021X, WPA2_PSK, OSEN, FT_PSK, FT_EAP, SAE, OWE, SUITE_B_192, WPA_PSK_SHA256, WPA_EAP_SHA256, WAPI_PSK, WAPI_CERT, FILS_SHA256, FILS_SHA384})
        public @interface KeyMgmtScheme {
        }

        /**
         * WPA is not used; plaintext or static WEP could be used.
         */
        public static final int NONE = 0;

        /**
         * WPA pre-shared key (requires {@code preSharedKey} to be specified).
         */
        public static final int WPA_PSK = 1;

        /**
         * WPA using EAP authentication. Generally used with an external authentication server.
         */
        public static final int WPA_EAP = 2;

        /**
         * IEEE 802.1X using EAP authentication and (optionally) dynamically
         * generated WEP keys.
         */
        public static final int IEEE8021X = 3;

        /**
         * WPA2 pre-shared key for use with soft access point
         * (requires {@code preSharedKey} to be specified).
         */
        public static final int WPA2_PSK = 4;

        /**
         * Hotspot 2.0 r2 OSEN:
         */
        public static final int OSEN = 5;

        /**
         * IEEE 802.11r Fast BSS Transition with PSK authentication.
         */
        public static final int FT_PSK = 6;

        /**
         * IEEE 802.11r Fast BSS Transition with EAP authentication.
         */
        public static final int FT_EAP = 7;

        /**
         * Simultaneous Authentication of Equals
         */
        public static final int SAE = 8;

        /**
         * Opportunististic Wireless Encryption
         */
        public static final int OWE = 9;

        /**
         * SUITE_B_192 192 bit level
         */
        public static final int SUITE_B_192 = 10;

        /**
         * WPA pre-shared key with stronger SHA256-based algorithms.
         */
        public static final int WPA_PSK_SHA256 = 11;

        /**
         * WPA using EAP authentication with stronger SHA256-based algorithms.
         */
        public static final int WPA_EAP_SHA256 = 12;

        /**
         * WAPI pre-shared key (requires {@code preSharedKey} to be specified).
         */
        public static final int WAPI_PSK = 13;

        /**
         * WAPI certificate to be specified.
         */
        public static final int WAPI_CERT = 14;

        /**
         * IEEE 802.11ai FILS SK with SHA256
         */
        public static final int FILS_SHA256 = 15;

        /**
         * IEEE 802.11ai FILS SK with SHA384:
         */
        public static final int FILS_SHA384 = 16;

        public static final String varName = "key_mgmt";
        public static final String[] strings = {"NONE", "WPA_PSK", "WPA_EAP", "IEEE8021X", "WPA2_PSK", "OSEN", "FT_PSK", "FT_EAP", "SAE", "OWE", "SUITE_B_192", "WPA_PSK_SHA256", "WPA_EAP_SHA256", "WAPI_PSK", "WAPI_CERT", "FILS_SHA256", "FILS_SHA384"};
    }

    /**
     * Recognized security protocols.
     */
    public static class Protocol {
        private Protocol() {
        }

        /**
         * WPA/IEEE 802.11i/D3.0
         *
         * @deprecated Due to security and performance limitations, use of WPA-1 networks
         * is discouraged. WPA-2 (RSN) should be used instead.
         */
        @Deprecated
        public static final int WPA = 0;

        /**
         * RSN WPA2/WPA3/IEEE 802.11i
         */
        public static final int RSN = 1;

        /**
         * HS2.0 r2 OSEN
         */
        public static final int OSEN = 2;

        /**
         * WAPI Protocol
         */
        public static final int WAPI = 3;

        public static final String varName = "proto";
        public static final String[] strings = {"WPA", "RSN", "OSEN", "WAPI"};
    }

    /**
     * Recognized IEEE 802.11 authentication algorithms.
     */
    public static class AuthAlgorithm {
        private AuthAlgorithm() {
        }

        /**
         * Open System authentication (required for WPA/WPA2)
         */
        public static final int OPEN = 0;

        /**
         * Shared Key authentication (requires static WEP keys)
         *
         * @deprecated Due to security and performance limitations, use of WEP networks
         * is discouraged.
         */
        @Deprecated
        public static final int SHARED = 1;

        /**
         * LEAP/Network EAP (only used with LEAP)
         */
        public static final int LEAP = 2;

        /**
         * SAE (Used only for WPA3-Personal)
         */
        public static final int SAE = 3;

        public static final String varName = "auth_alg";
        public static final String[] strings = {"OPEN", "SHARED", "LEAP", "SAE"};
    }

    /**
     * Recognized pairwise ciphers for WPA.
     */
    public static class PairwiseCipher {
        private PairwiseCipher() {
        }

        /**
         * Use only Group keys (deprecated)
         */
        public static final int NONE = 0;

        /**
         * Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
         *
         * @deprecated Due to security and performance limitations, use of WPA-1 networks
         * is discouraged. WPA-2 (RSN) should be used instead.
         */
        @Deprecated
        public static final int TKIP = 1;

        /**
         * AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
         */
        public static final int CCMP = 2;

        /**
         * AES in Galois/Counter Mode
         */
        public static final int GCMP_256 = 3;

        /**
         * SMS4 cipher for WAPI
         */
        public static final int SMS4 = 4;

        public static final String varName = "pairwise";
        public static final String[] strings = {"NONE", "TKIP", "CCMP", "GCMP_256", "SMS4"};
    }

    /**
     * Recognized group ciphers.
     * <pre>
     * CCMP = AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
     * TKIP = Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
     * WEP104 = WEP (Wired Equivalent Privacy) with 104-bit key
     * WEP40 = WEP (Wired Equivalent Privacy) with 40-bit key (original 802.11)
     * GCMP_256 = AES in Galois/Counter Mode
     * </pre>
     */
    public static class GroupCipher {
        private GroupCipher() {
        }

        /**
         * WEP40 = WEP (Wired Equivalent Privacy) with 40-bit key (original 802.11)
         *
         * @deprecated Due to security and performance limitations, use of WEP networks
         * is discouraged.
         */
        @Deprecated
        public static final int WEP40 = 0;

        /**
         * WEP104 = WEP (Wired Equivalent Privacy) with 104-bit key
         *
         * @deprecated Due to security and performance limitations, use of WEP networks
         * is discouraged.
         */
        @Deprecated
        public static final int WEP104 = 1;

        /**
         * Temporal Key Integrity Protocol [IEEE 802.11i/D7.0]
         */
        public static final int TKIP = 2;

        /**
         * AES in Counter mode with CBC-MAC [RFC 3610, IEEE 802.11i/D7.0]
         */
        public static final int CCMP = 3;

        /**
         * Hotspot 2.0 r2 OSEN
         */
        public static final int GTK_NOT_USED = 4;

        /**
         * AES in Galois/Counter Mode
         */
        public static final int GCMP_256 = 5;

        /**
         * SMS4 cipher for WAPI
         */
        public static final int SMS4 = 6;

        public static final String varName = "group";
        public static final String[] strings = { /* deprecated */ "WEP40", /* deprecated */ "WEP104", "TKIP", "CCMP", "GTK_NOT_USED", "GCMP_256", "SMS4"};
    }

    /**
     * Recognized group management ciphers.
     * <pre>
     * BIP_CMAC_256 = Cipher-based Message Authentication Code 256 bits
     * BIP_GMAC_128 = Galois Message Authentication Code 128 bits
     * BIP_GMAC_256 = Galois Message Authentication Code 256 bits
     * </pre>
     */
    public static class GroupMgmtCipher {
        private GroupMgmtCipher() {
        }

        /**
         * CMAC-256 = Cipher-based Message Authentication Code
         */
        public static final int BIP_CMAC_256 = 0;

        /**
         * GMAC-128 = Galois Message Authentication Code
         */
        public static final int BIP_GMAC_128 = 1;

        /**
         * GMAC-256 = Galois Message Authentication Code
         */
        public static final int BIP_GMAC_256 = 2;

        private static final String varName = "groupMgmt";
        private static final String[] strings = {"BIP_CMAC_256", "BIP_GMAC_128", "BIP_GMAC_256"};
    }

    /**
     * Recognized suiteB ciphers.
     * <pre>
     * ECDHE_ECDSA
     * ECDHE_RSA
     * </pre>
     */
    public static class SuiteBCipher {
        private SuiteBCipher() {
        }

        /**
         * Diffie-Hellman with Elliptic Curve_ECDSA signature
         */
        public static final int ECDHE_ECDSA = 0;

        /**
         * Diffie-Hellman with_RSA signature
         */
        public static final int ECDHE_RSA = 1;

        private static final String varName = "SuiteB";
        private static final String[] strings = {"ECDHE_ECDSA", "ECDHE_RSA"};
    }

    /**
     * Security type for an open network.
     */
    public static final int SECURITY_TYPE_OPEN = 0;

    /**
     * Security type for a WEP network.
     */
    public static final int SECURITY_TYPE_WEP = 1;

    /**
     * Security type for a PSK network.
     */
    public static final int SECURITY_TYPE_PSK = 2;

    /**
     * Security type for an EAP network.
     */
    public static final int SECURITY_TYPE_EAP = 3;

    /**
     * Security type for an SAE network.
     */
    public static final int SECURITY_TYPE_SAE = 4;

    /**
     * Security type for an EAP Suite B network.
     */
    public static final int SECURITY_TYPE_EAP_SUITE_B = 5;

    /**
     * Security type for an OWE network.
     */
    public static final int SECURITY_TYPE_OWE = 6;

    /**
     * Security type for a WAPI PSK network.
     */
    public static final int SECURITY_TYPE_WAPI_PSK = 7;

    /**
     * Security type for a WAPI Certificate network.
     */
    public static final int SECURITY_TYPE_WAPI_CERT = 8;

    public WifiConfigurationHidden() {
        throw new RuntimeException("Stub!");
    }

    /**
     * Set the various security params to correspond to the provided security type.
     * This is accomplished by setting the various BitSets exposed in WifiConfiguration.
     *
     * @param securityType One of the following security types:
     *                     {@link #SECURITY_TYPE_OPEN},
     *                     {@link #SECURITY_TYPE_WEP},
     *                     {@link #SECURITY_TYPE_PSK},
     *                     {@link #SECURITY_TYPE_EAP},
     *                     {@link #SECURITY_TYPE_SAE},
     *                     {@link #SECURITY_TYPE_EAP_SUITE_B},
     *                     {@link #SECURITY_TYPE_OWE},
     *                     {@link #SECURITY_TYPE_WAPI_PSK}, or
     *                     {@link #SECURITY_TYPE_WAPI_CERT}
     */
    public void setSecurityParams(int securityType) {
        throw new RuntimeException("Stub!");
    }

    public static final int UNKNOWN_UID = -1;

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
     * See {@link KeyMgmt} for descriptions of the values.
     * Defaults to WPA-PSK WPA-EAP.
     */
    @NonNull
    public BitSet allowedKeyManagement;

    /**
     * The set of security protocols supported by this configuration.
     * See {@link Protocol} for descriptions of the values.
     * Defaults to WPA RSN.
     */
    @NonNull
    public BitSet allowedProtocols;

    /**
     * The set of authentication protocols supported by this configuration.
     * See {@link AuthAlgorithm} for descriptions of the values.
     * Defaults to automatic selection.
     */
    @NonNull
    public BitSet allowedAuthAlgorithms;

    /**
     * The set of pairwise ciphers for WPA supported by this configuration.
     * See {@link PairwiseCipher} for descriptions of the values.
     * Defaults to CCMP TKIP.
     */
    @NonNull
    public BitSet allowedPairwiseCiphers;

    /**
     * The set of group ciphers supported by this configuration.
     * See {@link GroupCipher} for descriptions of the values.
     * Defaults to CCMP TKIP WEP104 WEP40.
     */
    @NonNull
    public BitSet allowedGroupCiphers;
    /**
     * The set of group management ciphers supported by this configuration.
     * See {@link GroupMgmtCipher} for descriptions of the values.
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
     * Auto-join is allowed by user for this network.
     * Default true.
     */
    public boolean allowAutojoin = true;

    public String getPrintableSsid() {
        throw new RuntimeException("Stub!");
    }

    public int getAuthType() {
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
     * Get a unique key which represent this Wi-Fi network. If two profiles are for
     * the same Wi-Fi network, but from different provider, they would have the same key.
     */
    public String getNetworkKey() {
        throw new RuntimeException("Stub!");
    }

    /**
     * return the SSID + security type in String format.
     */
    public String getSsidAndSecurityTypeString() {
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
