package com.android.wifi.x.com.android.modules.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * @noinspection rawtypes
 */
public class ParceledListSlice<T extends Parcelable> extends BaseParceledListSlice<T> {

    public ParceledListSlice(List<T> list) {
        super(list);
    }

    private ParceledListSlice(Parcel in, ClassLoader loader) {
        super(in, loader);
    }

    public static <T extends Parcelable> ParceledListSlice<T> emptyList() {
        throw new RuntimeException("Stub!");
    }

    @Override
    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected void writeElement(T parcelable, Parcel dest, int callFlags) {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected void writeParcelableCreator(T parcelable, Parcel dest) {
        throw new RuntimeException("Stub!");
    }

    @Override
    protected Creator<?> readParcelableCreator(Parcel from, ClassLoader loader) {
        throw new RuntimeException("Stub!");
    }

    public static final ClassLoaderCreator<ParceledListSlice> CREATOR = new ClassLoaderCreator<>() {

        public ParceledListSlice createFromParcel(Parcel in) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ParceledListSlice createFromParcel(Parcel in, ClassLoader loader) {
            throw new RuntimeException("Stub!");
        }

        @Override
        public ParceledListSlice[] newArray(int size) {
            throw new RuntimeException("Stub!");
        }
    };
}