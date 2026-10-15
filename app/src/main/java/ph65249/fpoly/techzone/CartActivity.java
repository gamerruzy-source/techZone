package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference cartRef;
    private TextView tvTotalPrice;
    private RecyclerView rvCart;
    private CartAdapter adapter;
    private List<CartItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        String userId;
        try {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                userId = WishlistManager.getUserKey(this);
            }
        } catch (Exception e) {
            userId = WishlistManager.getUserKey(this);
        }

        try {
            cartRef = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
                    .getReference("Carts").child(userId);
        } catch (Exception e) {
            cartRef = null;
        }

        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        rvCart = findViewById(R.id.rvCart);
        if (rvCart != null) {
            rvCart.setLayoutManager(new LinearLayoutManager(this));
            rvCart.setHasFixedSize(true);
            rvCart.setItemViewCacheSize(20);
        }

        cartItems = new ArrayList<>();
        adapter = new CartAdapter(cartItems, new CartAdapter.OnQuantityChangeListener() {
            @Override
            public void onIncrease(CartItem item) {
                updateQuantity(item, 1);
            }

            @Override
            public void onDecrease(CartItem item) {
                if (item.getQuantity() > 1) {
                    updateQuantity(item, -1);
                } else {
                    removeItem(item);
                }
            }
        });
        rvCart.setAdapter(adapter);

        loadCartData();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
                CartItem first = cartItems.get(0);
                String name = first.getName();
                if (cartItems.size() > 1) {
                    name = name + " (+" + (cartItems.size() - 1) + " sp khác)";
                }
                intent.putExtra("productName", name);
                intent.putExtra("productPrice", first.getPrice());
                intent.putExtra("productImage", first.getImageRes());
                if (tvTotalPrice != null) {
                    intent.putExtra("totalPrice", tvTotalPrice.getText().toString());
                }
                startActivity(intent);
            }
        });

        // Tab Switchers: Giỏ hàng 🛒 vs Đơn hàng đã đặt 📦
        com.google.android.material.button.MaterialButton tabCart = findViewById(R.id.tabCart);
        com.google.android.material.button.MaterialButton tabOrders = findViewById(R.id.tabOrders);
        LinearLayout layoutOrdersList = findViewById(R.id.layoutOrdersList);
        View btnDeleteAll = findViewById(R.id.btnDeleteAll);
        View layoutBottomBar = findViewById(R.id.btnCheckout) != null ? (View) findViewById(R.id.btnCheckout).getParent().getParent().getParent() : null;

        final String activeUserId = userId;
        if (tabCart != null && tabOrders != null) {
            tabCart.setOnClickListener(v -> {
                tabCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF6B00")));
                tabCart.setTextColor(android.graphics.Color.WHITE);
                tabOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF1EC")));
                tabOrders.setTextColor(android.graphics.Color.parseColor("#C2410C"));

                if (rvCart != null) rvCart.setVisibility(View.VISIBLE);
                if (layoutOrdersList != null) layoutOrdersList.setVisibility(View.GONE);
                if (btnDeleteAll != null) btnDeleteAll.setVisibility(View.VISIBLE);
                if (layoutBottomBar != null) layoutBottomBar.setVisibility(View.VISIBLE);
            });

            tabOrders.setOnClickListener(v -> {
                tabOrders.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF6B00")));
                tabOrders.setTextColor(android.graphics.Color.WHITE);
                tabCart.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF1EC")));
                tabCart.setTextColor(android.graphics.Color.parseColor("#C2410C"));

                if (rvCart != null) rvCart.setVisibility(View.GONE);
                if (layoutOrdersList != null) layoutOrdersList.setVisibility(View.VISIBLE);
                if (btnDeleteAll != null) btnDeleteAll.setVisibility(View.GONE);
                if (layoutBottomBar != null) layoutBottomBar.setVisibility(View.GONE);

                loadPlacedOrdersHistory(activeUserId, layoutOrdersList);
            });

            String openTab = getIntent().getStringExtra("tab");
            if ("orders".equalsIgnoreCase(openTab)) {
                tabOrders.performClick();
            }
        }

        if (btnDeleteAll != null) {
            btnDeleteAll.setOnClickListener(v -> {
                if (cartItems.isEmpty()) {
                    Toast.makeText(CartActivity.this, "Giỏ hàng đang trống!", Toast.LENGTH_SHORT).show();
                    return;
                }
                new androidx.appcompat.app.AlertDialog.Builder(CartActivity.this)
                    .setTitle("Xóa toàn bộ giỏ hàng?")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ sản phẩm trong giỏ hàng không?")
                    .setPositiveButton("Xóa tất cả", (dialog, which) -> {
                        if (cartRef != null) {
                            cartRef.removeValue().addOnSuccessListener(aVoid -> {
                                cartItems.clear();
                                adapter.notifyDataSetChanged();
                                updateTotal(0);
                                Toast.makeText(CartActivity.this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            cartItems.clear();
                            adapter.notifyDataSetChanged();
                            updateTotal(0);
                            Toast.makeText(CartActivity.this, "Đã xóa toàn bộ giỏ hàng!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
            });
        }
    }

    private void loadCartData() {
        if (cartRef == null) {
            useLocalCart();
            return;
        }

        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartItems.clear();
                long total = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    CartItem item = data.getValue(CartItem.class);
                    if (item != null) {
                        item.setId(data.getKey());
                        cartItems.add(item);
                        try {
                            String priceStr = item.getPrice().replaceAll("[^0-9]", "");
                            total += Long.parseLong(priceStr) * item.getQuantity();
                        } catch (Exception ignored) {}
                    }
                }
                if (cartItems.isEmpty()) {
                    useLocalCart();
                } else {
                    adapter.notifyDataSetChanged();
                    updateTotal(total);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                useLocalCart();
            }
        });
    }

    private void useLocalCart() {
        cartItems.clear();
        if (adapter != null) adapter.notifyDataSetChanged();
        updateTotal(0);
    }

    private void loadPlacedOrdersHistory(String userId, LinearLayout layoutOrdersList) {
        if (layoutOrdersList == null) return;
        layoutOrdersList.removeAllViews();

        String userKey = WishlistManager.getUserKey(this);
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String ordersKey = "placed_orders_" + userKey;
        java.util.Set<String> savedOrders = pref.getStringSet(ordersKey, new java.util.HashSet<>());
        java.util.Set<String> localOrders = new java.util.HashSet<>(savedOrders != null ? savedOrders : new java.util.HashSet<>());

        if (!localOrders.isEmpty()) {
            for (String item : localOrders) {
                String[] parts = item.split("\\|\\|\\|");
                if (parts.length >= 5) {
                    String code = parts[0];
                    String name = parts[1];
                    String address = parts[2];
                    String timeStr = parts[3];
                    String status = parts[4];

                    long timestamp = 0;
                    try { timestamp = Long.parseLong(timeStr); } catch (Exception ignored) {}

                    View itemView = LayoutInflater.from(CartActivity.this).inflate(R.layout.item_placed_order, layoutOrdersList, false);

                    TextView tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
                    TextView tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
                    TextView tvRecipientInfo = itemView.findViewById(R.id.tvRecipientInfo);
                    TextView tvAddressInfo = itemView.findViewById(R.id.tvAddressInfo);
                    TextView tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
                    View btnTrackJourney = itemView.findViewById(R.id.btnTrackJourney);

                    if (tvOrderCode != null) tvOrderCode.setText("Đơn hàng #" + code);
                    if (tvOrderStatus != null) tvOrderStatus.setText(status.toUpperCase());
                    if (tvRecipientInfo != null) tvRecipientInfo.setText("Người nhận: " + name);
                    if (tvAddressInfo != null) tvAddressInfo.setText("Địa chỉ: " + address);
                    
                    if (tvOrderTime != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, dd/MM/yyyy", java.util.Locale.getDefault());
                        tvOrderTime.setText("Thời gian đặt: " + (timestamp > 0 ? sdf.format(new java.util.Date(timestamp)) : timeStr));
                    }

                    if (btnTrackJourney != null) {
                        final String finalCode = code;
                        btnTrackJourney.setOnClickListener(v -> {
                            Intent intent = new Intent(CartActivity.this, TrackOrderActivity.class);
                            intent.putExtra("ORDER_CODE", finalCode);
                            startActivity(intent);
                        });
                    }

                    layoutOrdersList.addView(itemView, 0);
                }
            }
        }

        FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
            .getReference("Orders").child(userKey)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        layoutOrdersList.removeAllViews();
                        for (DataSnapshot child : snapshot.getChildren()) {
                            String code = child.child("orderCode").getValue(String.class);
                            String status = child.child("status").getValue(String.class);
                            String name = child.child("customerName").getValue(String.class);
                            String address = child.child("address").getValue(String.class);
                            Long timestamp = child.child("timestamp").getValue(Long.class);

                            View itemView = LayoutInflater.from(CartActivity.this).inflate(R.layout.item_placed_order, layoutOrdersList, false);

                            TextView tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
                            TextView tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
                            TextView tvRecipientInfo = itemView.findViewById(R.id.tvRecipientInfo);
                            TextView tvAddressInfo = itemView.findViewById(R.id.tvAddressInfo);
                            TextView tvOrderTime = itemView.findViewById(R.id.tvOrderTime);
                            View btnTrackJourney = itemView.findViewById(R.id.btnTrackJourney);

                            if (tvOrderCode != null && code != null) tvOrderCode.setText("Đơn hàng #" + code);
                            if (tvOrderStatus != null && status != null) tvOrderStatus.setText(status.toUpperCase());
                            if (tvRecipientInfo != null && name != null) tvRecipientInfo.setText("Người nhận: " + name);
                            if (tvAddressInfo != null && address != null) tvAddressInfo.setText("Địa chỉ: " + address);
                            
                            if (tvOrderTime != null && timestamp != null) {
                                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, dd/MM/yyyy", java.util.Locale.getDefault());
                                tvOrderTime.setText("Thời gian đặt: " + sdf.format(new java.util.Date(timestamp)));
                            }

                            if (btnTrackJourney != null) {
                                final String finalCode = code;
                                btnTrackJourney.setOnClickListener(v -> {
                                    Intent intent = new Intent(CartActivity.this, TrackOrderActivity.class);
                                    intent.putExtra("ORDER_CODE", finalCode);
                                    startActivity(intent);
                                });
                            }

                            layoutOrdersList.addView(itemView, 0);
                        }
                    } else if (layoutOrdersList.getChildCount() == 0) {
                        TextView tvEmpty = new TextView(CartActivity.this);
                        tvEmpty.setText("📦 Bạn chưa có đơn hàng nào đã đặt!");
                        tvEmpty.setTextColor(android.graphics.Color.parseColor("#94A3B8"));
                        tvEmpty.setTextSize(14f);
                        tvEmpty.setGravity(android.view.Gravity.CENTER);
                        tvEmpty.setPadding(0, 80, 0, 80);
                        layoutOrdersList.addView(tvEmpty);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void updateTotal(long total) {
        if (tvTotalPrice != null) {
            tvTotalPrice.setText(String.format(java.util.Locale.US, "%,dđ", total).replace(",", "."));
        }
    }

    private void updateQuantity(CartItem item, int delta) {
        item.setQuantity(item.getQuantity() + delta);
        adapter.notifyDataSetChanged();
        recalculateTotal();
        if (cartRef != null && item.getId() != null) {
            cartRef.child(item.getId()).child("quantity").setValue(item.getQuantity());
        }
    }

    private void removeItem(CartItem item) {
        cartItems.remove(item);
        adapter.notifyDataSetChanged();
        recalculateTotal();
        if (cartRef != null && item.getId() != null) {
            cartRef.child(item.getId()).removeValue();
        }
    }

    private void recalculateTotal() {
        long total = 0;
        for (CartItem item : cartItems) {
            try {
                String priceStr = item.getPrice().replaceAll("[^0-9]", "");
                total += Long.parseLong(priceStr) * item.getQuantity();
            } catch (Exception ignored) {}
        }
        updateTotal(total);
    }
}
