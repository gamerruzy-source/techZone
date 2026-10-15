package ph65249.fpoly.techzone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {
    private EditText edtName, edtPrice, edtDesc;
    private Spinner spinnerCategory;
    private ImageView imgSelected;
    private DatabaseReference mDatabase;
    
    private Uri imageUri = null;
    private int selectedImageRes = R.drawable.iphonepromaxbac; // Mặc định nếu không chọn tệp

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    imgSelected.setImageURI(imageUri);
                    Toast.makeText(this, "Đã chọn ảnh từ thiết bị!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference();

        edtName = findViewById(R.id.edtProductName);
        edtPrice = findViewById(R.id.edtProductPrice);
        edtDesc = findViewById(R.id.edtProductDesc);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imgSelected = findViewById(R.id.imgSelected);

        // Danh mục
        String[] categories = {"iPhone", "MacBook", "Audio (Tai nghe)", "Watch (Đồng hồ)", "Phụ kiện"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Chọn tệp ảnh thật từ thư viện máy
        findViewById(R.id.btnSelectImage).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String price = edtPrice.getText().toString().trim();
            String desc = edtDesc.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();

            if (name.isEmpty() || price.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên và giá!", Toast.LENGTH_SHORT).show();
                return;
            }

            String productId = mDatabase.child("Products").push().getKey();
            Map<String, Object> product = new HashMap<>();
            product.put("id", productId);
            product.put("name", name);
            product.put("price", price.contains("đ") ? price : price + "đ");
            product.put("desc", desc);
            product.put("category", category);

            // Lưu đường dẫn URI tệp ảnh nếu có chọn, ngược lại dùng ảnh mặc định
            if (imageUri != null) {
                product.put("imageUri", imageUri.toString());
            } else {
                product.put("imageRes", selectedImageRes);
            }

            if (productId != null) {
                mDatabase.child("Products").child(productId).setValue(product)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Thêm sản phẩm thành công!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}