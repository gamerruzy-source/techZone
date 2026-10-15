package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private AppCompatButton btnLogin;
    private TextView tvForgotPassword, tvRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);

        // Auto-fill registered credentials if passed from RegisterActivity or SharedPreferences
        String regEmail = getIntent().getStringExtra("REGISTERED_EMAIL");
        String regPass = getIntent().getStringExtra("REGISTERED_PASS");

        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String savedEmail = pref.getString("saved_email", "");
        String savedPass = pref.getString("saved_password", "");

        if (regEmail != null && !regEmail.isEmpty()) {
            edtEmail.setText(regEmail);
        } else if (!savedEmail.isEmpty()) {
            edtEmail.setText(savedEmail);
        }

        if (regPass != null && !regPass.isEmpty()) {
            edtPassword.setText(regPass);
        } else if (!savedPass.isEmpty()) {
            edtPassword.setText(savedPass);
        }

        ImageView btnTogglePassword = findViewById(R.id.btnTogglePassword);
        if (btnTogglePassword != null) {
            btnTogglePassword.setOnClickListener(new View.OnClickListener() {
                private boolean isPasswordVisible = false;

                @Override
                public void onClick(View v) {
                    isPasswordVisible = !isPasswordVisible;
                    if (isPasswordVisible) {
                        edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
                    } else {
                        edtPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
                    }
                    if (edtPassword.getText() != null) {
                        edtPassword.setSelection(edtPassword.getText().length());
                    }
                }
            });
        }

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Đăng nhập Admin DUY NHẤT (chỉ tk admin với mk 123123)
            if (email.equalsIgnoreCase("admin") && password.equals("123123")) {
                saveLoginState(email, password, "admin", true);
                navigateToHome("admin");
                return;
            } else if ((email.equalsIgnoreCase("vuvantan") || email.equalsIgnoreCase("khachhang")) && password.equals("123123")) {
                saveLoginState(email, password, "user", true);
                navigateToHome("user");
                return;
            }

            // Đăng nhập Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            saveLoginState(email, password, "user", true);
                            navigateToHome("user");
                        } else {
                            Toast.makeText(LoginActivity.this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        tvForgotPassword.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập Email để khôi phục mật khẩu!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Hướng dẫn khôi phục đã được gửi vào Email của bạn!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Lỗi: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }



    private void saveLoginState(String email, String password, String role, boolean saveCredentials) {
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        
        // Quản lý quyền: Chỉ duy nhất tk "admin" có quyền admin, tất cả còn lại là user
        String finalRole = "admin".equalsIgnoreCase(email) ? "admin" : "user";
        editor.putBoolean("isLoggedIn", true);
        editor.putString("role", finalRole);
        
        // Cập nhật đúng Tên hiển thị tương ứng với từng tài khoản đăng nhập
        String displayName;
        if ("admin".equalsIgnoreCase(email)) {
            displayName = "Quản Trị Viên Admin";
        } else if ("vuvantan".equalsIgnoreCase(email)) {
            displayName = "Vũ Văn Tân";
        } else if ("khachhang".equalsIgnoreCase(email)) {
            displayName = "Khách Hàng TechZone";
        } else if (email.contains("@")) {
            displayName = email.split("@")[0];
        } else {
            displayName = email;
        }
        editor.putString("saved_name", displayName);

        if (saveCredentials) {
            editor.putString("saved_email", email);
            editor.putString("saved_password", password);
        } else {
            editor.remove("saved_email");
            editor.remove("saved_password");
        }
        editor.apply();
    }

    private void navigateToHome(String role) {
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }
}