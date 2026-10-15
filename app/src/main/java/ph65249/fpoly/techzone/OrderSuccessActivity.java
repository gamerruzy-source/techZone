package ph65249.fpoly.techzone;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class OrderSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_success);

        // Ánh xạ các thành phần từ layout
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        TextView tvOrderCode = findViewById(R.id.tvOrderCode);
        AppCompatButton btnTrackOrder = findViewById(R.id.btnTrackOrder);
        AppCompatButton btnGoHome = findViewById(R.id.btnGoHome);

        // Nhận mã đơn hàng từ Intent
        final String orderCode = getIntent().getStringExtra("ORDER_CODE");
        if (orderCode != null) {
            tvOrderCode.setText("Mã đơn hàng: " + orderCode);
        }

        // Quay lại (thường là về Home)
        // (Đã xử lý ở trên)

        // Chuyển sang màn hình Theo dõi đơn hàng
        btnTrackOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, TrackOrderActivity.class);
            intent.putExtra("ORDER_CODE", orderCode);
            startActivity(intent);
        });

        // Về trang chủ
        btnGoHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSuccessActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}