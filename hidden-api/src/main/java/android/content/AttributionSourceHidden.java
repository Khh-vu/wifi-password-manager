package android.content;

import androidx.annotation.Nullable;

import java.util.Set;

import dev.rikka.tools.refine.RefineAs;

@RefineAs(AttributionSource.class)
public final class AttributionSourceHidden {
    public AttributionSourceHidden(int uid, @Nullable String packageName, @Nullable String attributionTag, @Nullable Set<String> renouncedPermissions, @Nullable AttributionSource next) {
        throw new RuntimeException("Stub!");
    }
}
