package ph65249.fpoly.techzone;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import java.util.HashSet;
import java.util.Set;

public class WishlistManager {
    private static final String PREF_NAME = "TechZoneWishlistPref";
    private static final String KEY_PREFIX = "liked_product_ids_";

    public static String getUserKey(Context context) {
        if (context == null) return "guest";
        try {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                String uid = mAuth.getCurrentUser().getUid();
                if (!uid.trim().isEmpty()) {
                    return "fb_" + uid.trim();
                }
            }
        } catch (Exception ignored) {}

        SharedPreferences pref = context.getSharedPreferences("TechZonePref", Context.MODE_PRIVATE);
        String savedEmail = pref.getString("saved_email", "");
        String savedName = pref.getString("saved_name", "");
        
        if (savedEmail != null && !savedEmail.trim().isEmpty()) {
            return savedEmail.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
        } else if (savedName != null && !savedName.trim().isEmpty()) {
            return savedName.trim().toLowerCase().replaceAll("[^a-z0-9]", "_");
        }
        return "guest";
    }

    private static String getWishlistPrefKey(Context context) {
        return KEY_PREFIX + getUserKey(context);
    }

    public static boolean isLiked(Context context, String productId) {
        if (context == null || productId == null) return false;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> set = pref.getStringSet(getWishlistPrefKey(context), new HashSet<>());
        return set != null && set.contains(productId);
    }

    public static boolean toggleLike(Context context, String productId) {
        if (context == null || productId == null) return false;
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String key = getWishlistPrefKey(context);
        Set<String> savedSet = pref.getStringSet(key, new HashSet<>());
        Set<String> set = new HashSet<>(savedSet != null ? savedSet : new HashSet<>());
        boolean nowLiked;
        if (set.contains(productId)) {
            set.remove(productId);
            nowLiked = false;
        } else {
            set.add(productId);
            nowLiked = true;
        }
        pref.edit().putStringSet(key, set).apply();
        return nowLiked;
    }

    public static Set<String> getLikedProductIds(Context context) {
        if (context == null) return new HashSet<>();
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> savedSet = pref.getStringSet(getWishlistPrefKey(context), new HashSet<>());
        return new HashSet<>(savedSet != null ? savedSet : new HashSet<>());
    }
}
