package io.github.wifi_password_manager.ipc

import android.content.pm.ParceledListSlice
import android.os.BadParcelableException
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class WifiNetworkListParcel(val networks: List<WifiNetworkParcel>) : Parcelable {
    private companion object : Parceler<WifiNetworkListParcel> {
        override fun WifiNetworkListParcel.write(parcel: Parcel, flags: Int) {
            parcel.writeInt(networks.size)
            parcel.writeValue(ParceledListSlice(networks))
        }

        @Suppress("UNCHECKED_CAST")
        override fun create(parcel: Parcel): WifiNetworkListParcel {
            val size = parcel.readInt()
            val slice =
                parcel.readValue(WifiNetworkListParcel::class.java.classLoader)
                    as ParceledListSlice<WifiNetworkParcel>
            if (slice.list.size != size) {
                throw BadParcelableException("Received ${slice.list.size} of $size networks")
            }
            return WifiNetworkListParcel(slice.list)
        }
    }
}
