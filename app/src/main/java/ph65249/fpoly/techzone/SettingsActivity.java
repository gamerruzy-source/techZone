package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private ImageView imgUserAvatar;
    private EditText edtUserName, edtUserAddress;
    private String selectedAvatarName = "anhnam1";

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference();

        MaterialCardView btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        imgUserAvatar = findViewById(R.id.imgUserAvatar);
        edtUserName = findViewById(R.id.edtUserName);
        edtUserAddress = findViewById(R.id.edtUserAddress);

        // Load saved profile data per user account key
        String userKey = WishlistManager.getUserKey(this);
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String name = pref.getString("saved_name_" + userKey, pref.getString("saved_name", "Vũ Văn Tân"));
        String address = pref.getString("saved_address_" + userKey, "Cầu Giấy, Hà Nội");
        selectedAvatarName = pref.getString("saved_avatar_" + userKey, "anhnam1");

        if (edtUserName != null) edtUserName.setText(name);
        if (edtUserAddress != null) edtUserAddress.setText(address);
        updateAvatarImageView(selectedAvatarName);

        // Avatar change handler
        View btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> showAvatarPickerDialog());
        }

        // Liked Products Item (Sản phẩm yêu thích)
        View btnLikedProducts = findViewById(R.id.btnLikedProducts);
        if (btnLikedProducts != null) {
            btnLikedProducts.setOnClickListener(v -> {
                Intent intent = new Intent(SettingsActivity.this, ProductListActivity.class);
                intent.putExtra("category", "liked");
                startActivity(intent);
            });
        }



        // Automatic GPS Location / Map button handler
        MaterialButton btnGetLocation = findViewById(R.id.btnGetLocation);
        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> {
                String currentLocation = "88 Đường Cầu Giấy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội (Vị trí GPS hiện tại 📍)";
                if (edtUserAddress != null) {
                    edtUserAddress.setText(currentLocation);
                }
                Toast.makeText(this, "📍 Đã định vị thành công vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            });
        }

        // Save Profile button handler
        MaterialButton btnSaveProfile = findViewById(R.id.btnSaveProfile);
        if (btnSaveProfile != null) {
            btnSaveProfile.setOnClickListener(v -> saveProfileInfo());
        }

        // Logout button handler
        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                mAuth.signOut();
                
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Toast.makeText(this, "Đã đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                
                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLikedCountBadge();
    }

    private void updateLikedCountBadge() {
        android.widget.TextView tvLikedCountBadge = findViewById(R.id.tvLikedCountBadge);
        if (tvLikedCountBadge != null) {
            int count = WishlistManager.getLikedProductIds(this).size();
            tvLikedCountBadge.setText(count + " sản phẩm");
        }
    }

    private void updateAvatarImageView(String avatarName) {
        if (imgUserAvatar == null) return;
        int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
        if (resId != 0) {
            imgUserAvatar.setImageResource(resId);
        } else {
            imgUserAvatar.setImageResource(R.drawable.anhnam1);
        }
    }

    private void showAvatarPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_avatar, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        int[] avatarIds = {
            R.id.avatarNam1, R.id.avatarNam2, R.id.avatarNam3, R.id.avatarNam4, R.id.avatarNam5,
            R.id.avatarNu1, R.id.avatarNu2, R.id.avatarNu3, R.id.avatarNu4, R.id.avatarNu5
        };

        String[] avatarNames = {
            "anhnam1", "anhnam2", "anhnam3", "anhnam4", "anhnam5",
            "anhnu1", "anhnu2", "anhnu3", "anhnu4", "anhnu5"
        };

        for (int i = 0; i < avatarIds.length; i++) {
            ImageView img = dialogView.findViewById(avatarIds[i]);
            final String aName = avatarNames[i];
            if (img != null) {
                img.setOnClickListener(v -> {
                    selectedAvatarName = aName;
                    updateAvatarImageView(selectedAvatarName);
                    
                    SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
                    pref.edit().putString("saved_avatar", selectedAvatarName).apply();

                    Toast.makeText(this, "Đã chọn " + aName + "!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
        }

        dialog.show();
    }

    private void saveProfileInfo() {
        String name = edtUserName.getText().toString().trim();
        String address = edtUserAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên cá nhân!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userKey = WishlistManager.getUserKey(this);
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        pref.edit()
            .putString("saved_name", name)
            .putString("saved_name_" + userKey, name)
            .putString("saved_address_" + userKey, address)
            .putString("saved_avatar_" + userKey, selectedAvatarName)
            .apply();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("name", name);
            updateData.put("address", address);
            updateData.put("avatar", selectedAvatarName);

            mDatabase.child("Users").child(uid).updateChildren(updateData);
        }

        Toast.makeText(this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
