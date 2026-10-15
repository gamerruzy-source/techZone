package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Tự động chuyển màn hình sau 2 giây (Splash Transition)
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try {
                SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
                boolean isLoggedIn = pref.getBoolean("isLoggedIn", false);
                String savedEmail = pref.getString("saved_email", "");
                String savedPass = pref.getString("saved_password", "");

                // PHÂN QUYỀN NGHIÊM NGẶT: Chỉ duy nhất tài khoản admin / 123123 có quyền Admin
                String role = ("admin".equalsIgnoreCase(savedEmail) && "123123".equals(savedPass)) ? "admin" : "user";
                pref.edit().putString("role", role).apply();

                if (isLoggedIn) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    intent.putExtra("role", role);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
            finish();
        }, 2000);
    }
}