package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone, edtPassword, edtConfirmPassword;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());

        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirm = edtConfirmPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ Họ tên và Email!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(this, "Mật khẩu phải từ 6 ký tự trở lên!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!phone.isEmpty() && !phone.matches("^0[0-9]{9}$")) {
                Toast.makeText(this, "Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 0)!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserInfo(uid, name, email, password);
                    } else {
                        String fakeUid = "user_" + System.currentTimeMillis();
                        saveUserInfo(fakeUid, name, email, password);
                    }
                });
        });
    }

    private void saveUserInfo(String uid, String name, String email, String password) {
        // Tự động lưu thông tin đăng ký không cần hỏi lại
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("saved_name", name);
        editor.putString("saved_email", email);
        editor.putString("saved_password", password);
        editor.apply();

        Map<String, Object> user = new HashMap<>();
        user.put("uid", uid);
        user.put("name", name);
        user.put("email", email);
        
        mDatabase.child("Users").child(uid).setValue(user);

        CustomToast.show(this, "🎉 Đăng ký tài khoản " + name + " thành công!");

        // Tự động điền thông tin sang màn hình Đăng nhập
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        intent.putExtra("REGISTERED_EMAIL", email);
        intent.putExtra("REGISTERED_PASS", password);
        startActivity(intent);
        finish();
    }
}