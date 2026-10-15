package ph65249.fpoly.techzone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackOrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        TextView tvOrderCode = findViewById(R.id.tvTrackOrderCode);

        // Nhận mã đơn hàng từ màn hình trước
        String orderCode = getIntent().getStringExtra("ORDER_CODE");
        if (orderCode != null && !orderCode.isEmpty()) {
            tvOrderCode.setText("Mã đơn: #" + orderCode);
        } else {
            orderCode = "TZ2977755";
            tvOrderCode.setText("Mã đơn: #TZ2977755");
        }

        final String activeOrderCode = orderCode;
        String userKey = WishlistManager.getUserKey(this);

        updateTimelineTimestamps(System.currentTimeMillis());

        LinearLayout layoutOrderItemsList = findViewById(R.id.layoutOrderItemsList);

        FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
            .getReference("Orders").child(userKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String code = child.child("orderCode").getValue(String.class);
                            if (activeOrderCode.equalsIgnoreCase(code)) {
                                if (tvOrderCode != null && code != null) {
                                    tvOrderCode.setText("Mã đơn: #" + code);
                                }
                                Long ts = child.child("timestamp").getValue(Long.class);
                                if (ts != null && ts > 0) {
                                    updateTimelineTimestamps(ts);
                                }

                                DataSnapshot itemsSnap = child.child("items");
                                if (itemsSnap.exists() && layoutOrderItemsList != null) {
                                    layoutOrderItemsList.removeAllViews();
                                    for (DataSnapshot itemChild : itemsSnap.getChildren()) {
                                        String pName = itemChild.child("name").getValue(String.class);
                                        String pPrice = itemChild.child("price").getValue(String.class);
                                        Long pQtyLong = itemChild.child("quantity").getValue(Long.class);
                                        Long pImgLong = itemChild.child("imageRes").getValue(Long.class);

                                        int qty = pQtyLong != null ? pQtyLong.intValue() : 1;
                                        int rawRes = pImgLong != null ? pImgLong.intValue() : 0;
                                        int imgRes = CartAdapter.getProductDrawableByName(pName, rawRes);

                                        long itemPrice = 0;
                                        if (pPrice != null) {
                                            try { itemPrice = Long.parseLong(pPrice.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
                                        }
                                        long subtotal = itemPrice * qty;

                                        View itemView = android.view.LayoutInflater.from(TrackOrderActivity.this)
                                                .inflate(R.layout.item_checkout_product, layoutOrderItemsList, false);

                                        TextView tvName = itemView.findViewById(R.id.tvProductName);
                                        TextView tvPriceQty = itemView.findViewById(R.id.tvProductPriceQty);
                                        TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
                                        android.widget.ImageView imgProduct = itemView.findViewById(R.id.imgProduct);

                                        if (tvName != null && pName != null) tvName.setText(pName);
                                        if (tvPriceQty != null && pPrice != null) tvPriceQty.setText(pPrice + "  x  " + qty);
                                        if (tvItemTotal != null) {
                                            tvItemTotal.setText(String.format(java.util.Locale.US, "%,dđ", subtotal).replace(",", "."));
                                        }
                                        if (imgProduct != null && imgRes != 0) imgProduct.setImageResource(imgRes);

                                        layoutOrderItemsList.addView(itemView);
                                    }
                                }
                                break;
                            }
                        }
                    }

                    if (layoutOrderItemsList != null && layoutOrderItemsList.getChildCount() == 0) {
                        // Fallback sample product row
                        View itemView = android.view.LayoutInflater.from(TrackOrderActivity.this)
                                .inflate(R.layout.item_checkout_product, layoutOrderItemsList, false);

                        TextView tvName = itemView.findViewById(R.id.tvProductName);
                        TextView tvPriceQty = itemView.findViewById(R.id.tvProductPriceQty);
                        TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
                        android.widget.ImageView imgProduct = itemView.findViewById(R.id.imgProduct);

                        if (tvName != null) tvName.setText("Tai nghe Gaming RGB - Đen Đỏ");
                        if (tvPriceQty != null) tvPriceQty.setText("1.590.000đ  x  1");
                        if (tvItemTotal != null) tvItemTotal.setText("1.590.000đ");
                        if (imgProduct != null) imgProduct.setImageResource(R.drawable.tainghechuptaiden);

                        layoutOrderItemsList.addView(itemView);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        // Nút xem bản đồ trực tuyến
        View btnOpenMap = findViewById(R.id.btnOpenMap);
        if (btnOpenMap != null) {
            btnOpenMap.setOnClickListener(v -> {
                Uri gmmIntentUri = Uri.parse("geo:21.030653,105.801754?q=88+Đường+Cầu+Giấy,+Hà+Nội");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(mapIntent);
                } catch (Exception e) {
                    Toast.makeText(this, "📍 Đơn hàng đang di chuyển tại 88 Đường Cầu Giấy, Hà Nội", Toast.LENGTH_LONG).show();
                }
            });
        }

        // Nút liên hệ hỗ trợ
        View btnContactSupport = findViewById(R.id.btnContactSupport);
        if (btnContactSupport != null) {
            btnContactSupport.setOnClickListener(v -> {
                Toast.makeText(this, "📞 Tổng đài CSKH TechZone: 1900 8888 (Miễn phí)", Toast.LENGTH_LONG).show();
            });
        }

        // Nút hủy đơn hàng
        View btnCancelOrder = findViewById(R.id.btnCancelOrder);
        if (btnCancelOrder != null) {
            btnCancelOrder.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(TrackOrderActivity.this)
                    .setTitle("Hủy đơn hàng?")
                    .setMessage("Bạn có chắc chắn muốn hủy đơn hàng #" + activeOrderCode + " không?")
                    .setPositiveButton("Xác nhận Hủy", (dialog, which) -> {
                        cancelOrder(userKey, activeOrderCode);
                    })
                    .setNegativeButton("Hủy bỏ", null)
                    .show();
            });
        }
    }

    private void cancelOrder(String userKey, String orderCode) {
        // 1. Remove from Firebase database
        FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
            .getReference("Orders").child(userKey)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String code = child.child("orderCode").getValue(String.class);
                        if (orderCode.equalsIgnoreCase(code)) {
                            child.getRef().removeValue();
                            break;
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });

        // 2. Remove from SharedPreferences local storage
        android.content.SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String ordersKey = "placed_orders_" + userKey;
        java.util.Set<String> savedOrders = pref.getStringSet(ordersKey, new java.util.HashSet<>());
        if (savedOrders != null && !savedOrders.isEmpty()) {
            java.util.Set<String> updatedSet = new java.util.HashSet<>();
            for (String item : savedOrders) {
                if (!item.contains(orderCode)) {
                    updatedSet.add(item);
                }
            }
            pref.edit().putStringSet(ordersKey, updatedSet).apply();
        }

        CustomToast.show(this, "❌ Đã xóa và hủy đơn hàng #" + orderCode + "!");
        finish();
    }

    private void updateTimelineTimestamps(long baseTimestamp) {
        TextView tvTimeStep1 = findViewById(R.id.tvTimeStep1);
        TextView tvTimeStep2 = findViewById(R.id.tvTimeStep2);
        TextView tvTimeStep3 = findViewById(R.id.tvTimeStep3);
        TextView tvTimeStep4 = findViewById(R.id.tvTimeStep4);

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, 'ngày' dd/MM/yyyy", new java.util.Locale("vi", "VN"));
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        long t1 = baseTimestamp > 0 ? baseTimestamp : System.currentTimeMillis();
        long t2 = t1 + (15 * 60 * 1000L);
        long t3 = t1 + (45 * 60 * 1000L);
        long t4 = t1 + (2 * 3600 * 1000L);

        if (tvTimeStep1 != null) tvTimeStep1.setText(sdf.format(new java.util.Date(t1)));
        if (tvTimeStep2 != null) tvTimeStep2.setText(sdf.format(new java.util.Date(t2)));
        if (tvTimeStep3 != null) tvTimeStep3.setText(sdf.format(new java.util.Date(t3)));
        if (tvTimeStep4 != null) tvTimeStep4.setText("Dự kiến: " + sdf.format(new java.util.Date(t4)));
    }
}
