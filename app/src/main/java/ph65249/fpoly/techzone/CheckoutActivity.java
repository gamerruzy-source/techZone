package ph65249.fpoly.techzone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CheckoutActivity extends AppCompatActivity {

    private EditText edtName, edtAddress;
    private RadioGroup rgShipping, rgPayment;
    private ImageView imgStep1, imgStep2, imgStep3;
    private TextView tvStep1, tvStep2, tvStep3, tvTotalPrice;
    private android.view.View lineStep1, lineStep2;
    private AppCompatButton btnPlaceOrder;

    private List<Map<String, Object>> checkoutItemsList = new ArrayList<>();
    private long originalGrandTotal = 0;
    private long voucherDiscountAmount = 0;

    private int orangeColor, grayColor;
    private FirebaseDatabase database;
    private DatabaseReference ordersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Khởi tạo Firebase
        try {
            mAuth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com");
            ordersRef = database.getReference("Orders");
        } catch (Exception e) {
            e.printStackTrace();
        }

        orangeColor = ContextCompat.getColor(this, R.color.orange_primary);
        grayColor = ContextCompat.getColor(this, R.color.gray_text);

        initViews();
        setupListeners();
        updateProgress();

        // Yêu cầu quyền thông báo cho Android 13+
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        edtName = findViewById(R.id.edtName);
        edtAddress = findViewById(R.id.edtAddress);

        String userKey = WishlistManager.getUserKey(this);
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String name = pref.getString("saved_name_" + userKey, pref.getString("saved_name", "Vũ Văn Tân"));
        String address = pref.getString("saved_address_" + userKey, pref.getString("saved_address", "Cầu Giấy, Hà Nội"));
        if (edtName != null && name != null && !name.isEmpty()) edtName.setText(name);
        if (edtAddress != null && address != null && !address.isEmpty()) edtAddress.setText(address);

        // Tải toàn bộ danh sách sản phẩm trong giỏ hàng và tính tổng tiền
        android.widget.LinearLayout layoutCheckoutItemsList = findViewById(R.id.layoutCheckoutItemsList);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);

        if (database != null && layoutCheckoutItemsList != null) {
            database.getReference("Carts").child(userKey).addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    layoutCheckoutItemsList.removeAllViews();
                    checkoutItemsList.clear();
                    long grandTotal = 0;

                    if (snapshot.exists() && snapshot.hasChildren()) {
                        for (com.google.firebase.database.DataSnapshot child : snapshot.getChildren()) {
                            String name = child.child("name").getValue(String.class);
                            String priceStr = child.child("price").getValue(String.class);
                            Long qLong = child.child("quantity").getValue(Long.class);
                            Long imgLong = child.child("imageRes").getValue(Long.class);

                            int quantity = qLong != null ? qLong.intValue() : 1;
                            int rawImg = imgLong != null ? imgLong.intValue() : 0;
                            int imageRes = CartAdapter.getProductDrawableByName(name, rawImg);

                            Map<String, Object> map = new HashMap<>();
                            map.put("name", name);
                            map.put("price", priceStr);
                            map.put("quantity", quantity);
                            map.put("imageRes", imageRes);
                            checkoutItemsList.add(map);

                            long itemPrice = 0;
                            if (priceStr != null) {
                                try {
                                    itemPrice = Long.parseLong(priceStr.replaceAll("[^0-9]", ""));
                                } catch (Exception ignored) {}
                            }
                            long subtotal = itemPrice * quantity;
                            grandTotal += subtotal;

                            android.view.View itemView = android.view.LayoutInflater.from(CheckoutActivity.this)
                                    .inflate(R.layout.item_checkout_product, layoutCheckoutItemsList, false);

                            TextView tvName = itemView.findViewById(R.id.tvProductName);
                            TextView tvPriceQty = itemView.findViewById(R.id.tvProductPriceQty);
                            TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
                            ImageView imgProduct = itemView.findViewById(R.id.imgProduct);

                            if (tvName != null && name != null) tvName.setText(name);
                            if (tvPriceQty != null && priceStr != null) {
                                tvPriceQty.setText(priceStr + "  x  " + quantity);
                            }
                            if (tvItemTotal != null) {
                                tvItemTotal.setText(String.format(java.util.Locale.US, "%,dđ", subtotal).replace(",", "."));
                            }
                            if (imgProduct != null && imageRes != 0) {
                                imgProduct.setImageResource(imageRes);
                            }

                            layoutCheckoutItemsList.addView(itemView);
                        }
                    } else {
                        // Fallback single item from intent if cart node is empty
                        String intentName = getIntent().getStringExtra("productName");
                        String intentPrice = getIntent().getStringExtra("productPrice");
                        int intentImage = getIntent().getIntExtra("productImage", R.drawable.iphonepromaxbac);

                        if (intentName != null && !intentName.isEmpty()) {
                            android.view.View itemView = android.view.LayoutInflater.from(CheckoutActivity.this)
                                    .inflate(R.layout.item_checkout_product, layoutCheckoutItemsList, false);

                            TextView tvName = itemView.findViewById(R.id.tvProductName);
                            TextView tvPriceQty = itemView.findViewById(R.id.tvProductPriceQty);
                            TextView tvItemTotal = itemView.findViewById(R.id.tvItemTotal);
                            ImageView imgProduct = itemView.findViewById(R.id.imgProduct);

                            if (tvName != null) tvName.setText(intentName);
                            if (tvPriceQty != null && intentPrice != null) tvPriceQty.setText(intentPrice + "  x  1");
                            if (tvItemTotal != null && intentPrice != null) tvItemTotal.setText(intentPrice);
                            if (imgProduct != null && intentImage != 0) imgProduct.setImageResource(intentImage);

                            layoutCheckoutItemsList.addView(itemView);

                            String intentTotal = getIntent().getStringExtra("totalPrice");
                            if (tvTotalPrice != null && (intentTotal != null || intentPrice != null)) {
                                tvTotalPrice.setText(intentTotal != null ? intentTotal : intentPrice);
                            }
                        }
                    }

                    if (tvTotalPrice != null && grandTotal > 0) {
                        originalGrandTotal = grandTotal;
                        long finalTotal = Math.max(0, originalGrandTotal - voucherDiscountAmount);
                        tvTotalPrice.setText(String.format(java.util.Locale.US, "%,dđ", finalTotal).replace(",", "."));
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {}
            });
        }

        rgShipping = findViewById(R.id.rgShipping);
        rgPayment = findViewById(R.id.rgPayment);

        imgStep1 = findViewById(R.id.imgStep1);
        imgStep2 = findViewById(R.id.imgStep2);
        imgStep3 = findViewById(R.id.imgStep3);

        tvStep1 = findViewById(R.id.tvStep1);
        tvStep2 = findViewById(R.id.tvStep2);
        tvStep3 = findViewById(R.id.tvStep3);

        lineStep1 = findViewById(R.id.lineStep1);
        lineStep2 = findViewById(R.id.lineStep2);

        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
    }

    private void setupListeners() {
        TextWatcher addressWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                updateProgress();
            }
        };

        if (edtName != null) edtName.addTextChangedListener(addressWatcher);
        if (edtAddress != null) edtAddress.addTextChangedListener(addressWatcher);

        // Radio Button Mutual Exclusion
        RadioButton rbFast = findViewById(R.id.rbFast);
        RadioButton rbStandard = findViewById(R.id.rbStandard);

        if (rbFast != null && rbStandard != null) {
            rbFast.setOnClickListener(v -> {
                rbFast.setChecked(true);
                rbStandard.setChecked(false);
                updateProgress();
            });
            rbStandard.setOnClickListener(v -> {
                rbStandard.setChecked(true);
                rbFast.setChecked(false);
                updateProgress();
            });
        }

        RadioButton rbMomo = findViewById(R.id.rbMomo);
        RadioButton rbCod = findViewById(R.id.rbCod);

        if (rbMomo != null && rbCod != null) {
            rbMomo.setOnClickListener(v -> {
                rbMomo.setChecked(true);
                rbCod.setChecked(false);
                updateProgress();
            });
            rbCod.setOnClickListener(v -> {
                rbCod.setChecked(true);
                rbMomo.setChecked(false);
                updateProgress();
            });
        }

        // Voucher Picker Listeners
        android.view.View cardVoucherPicker = findViewById(R.id.cardVoucherPicker);
        android.view.View btnSelectVoucher = findViewById(R.id.btnSelectVoucher);
        TextView tvSelectedVoucherText = findViewById(R.id.tvSelectedVoucherText);

        android.view.View.OnClickListener openVoucherPicker = v -> showVoucherPickerDialog();

        if (btnSelectVoucher != null) btnSelectVoucher.setOnClickListener(openVoucherPicker);
        if (tvSelectedVoucherText != null) tvSelectedVoucherText.setOnClickListener(openVoucherPicker);

        if (btnPlaceOrder != null) {
            btnPlaceOrder.setOnClickListener(v -> saveOrderToFirebase());
        }
    }

    private void showVoucherPickerDialog() {
        android.view.View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.layout_dialog_voucher_picker, null);
        android.widget.LinearLayout layoutVoucherItems = dialogView.findViewById(R.id.layoutVoucherItems);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        String[] codes = {"TECHZONE10", "GIAM50K", "GIAM100K", "FREESHIP"};
        String[] descs = {
            "Giảm 10% tổng giá trị đơn hàng",
            "Giảm ngay 50.000đ trực tiếp vào đơn hàng",
            "Giảm ngay 100.000đ trực tiếp vào đơn hàng",
            "Miễn phí vận chuyển hỏa tốc (Giảm 30.000đ)"
        };

        if (layoutVoucherItems != null) {
            for (int i = 0; i < codes.length; i++) {
                final String code = codes[i];
                String desc = descs[i];

                android.view.View voucherCard = android.view.LayoutInflater.from(this).inflate(R.layout.item_voucher_card, layoutVoucherItems, false);

                TextView tvCode = voucherCard.findViewById(R.id.tvVoucherCode);
                TextView tvDesc = voucherCard.findViewById(R.id.tvVoucherDesc);
                android.view.View btnApply = voucherCard.findViewById(R.id.btnApplyThisVoucher);

                if (tvCode != null) tvCode.setText(code);
                if (tvDesc != null) tvDesc.setText(desc);

                android.view.View.OnClickListener applyClick = v -> {
                    applyVoucherCode(code);
                    dialog.dismiss();
                };

                voucherCard.setOnClickListener(applyClick);
                if (btnApply != null) btnApply.setOnClickListener(applyClick);

                layoutVoucherItems.addView(voucherCard);
            }
        }

        dialog.show();
    }

    private void applyVoucherCode(String code) {
        if (code == null || code.isEmpty()) return;

        TextView tvSelectedVoucherText = findViewById(R.id.tvSelectedVoucherText);

        long discount = 0;
        String discountText = "";

        if ("TECHZONE10".equalsIgnoreCase(code)) {
            discount = (long) (originalGrandTotal * 0.10);
            discountText = "10%";
        } else if ("GIAM50K".equalsIgnoreCase(code) || "TZGIAM50K".equalsIgnoreCase(code)) {
            discount = 50000;
            discountText = "50.000đ";
        } else if ("GIAM100K".equalsIgnoreCase(code)) {
            discount = 100000;
            discountText = "100.000đ";
        } else if ("FREESHIP".equalsIgnoreCase(code)) {
            discount = 30000;
            discountText = "30.000đ (Freeship)";
        }

        voucherDiscountAmount = discount;
        long finalTotal = Math.max(0, originalGrandTotal - voucherDiscountAmount);

        if (tvTotalPrice != null) {
            tvTotalPrice.setText(String.format(java.util.Locale.US, "%,dđ", finalTotal).replace(",", "."));
        }

        if (tvSelectedVoucherText != null) {
            tvSelectedVoucherText.setText("Mã: " + code + " (Giảm " + discountText + ")");
            tvSelectedVoucherText.setTextColor(android.graphics.Color.parseColor("#16A34A"));
        }

        CustomToast.show(this, "🎉 Chọn & áp dụng Voucher " + code + " thành công!");
    }

    private void saveOrderToFirebase() {
        String userId;
        try {
            if (mAuth != null && mAuth.getCurrentUser() != null) {
                userId = mAuth.getCurrentUser().getUid();
            } else {
                SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
                String savedName = pref.getString("saved_name", "user");
                if (savedName == null) savedName = "user";
                userId = "user_" + Math.abs(savedName.hashCode());
            }
        } catch (Exception e) {
            SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
            String savedName = pref.getString("saved_name", "user");
            if (savedName == null) savedName = "user";
            userId = "user_" + Math.abs(savedName.hashCode());
        }

        String name = edtName != null ? edtName.getText().toString().trim() : "";
        String address = edtAddress != null ? edtAddress.getText().toString().trim() : "";

        if (name.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Tên và Địa chỉ giao hàng!", Toast.LENGTH_SHORT).show();
            return;
        }

        int shippingId = rgShipping.getCheckedRadioButtonId();
        RadioButton rbShipping = findViewById(shippingId);
        String shippingMethod = (rbShipping != null) ? rbShipping.getText().toString() : "Giao hàng hỏa tốc 2h";

        int paymentId = rgPayment.getCheckedRadioButtonId();
        RadioButton rbPayment = findViewById(paymentId);
        String paymentMethod = (rbPayment != null) ? rbPayment.getText().toString() : "Thanh toán khi nhận hàng (COD)";

        String orderCode = generateOrderCode();

        // Tạo đối tượng dữ liệu đơn hàng
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderCode", orderCode);
        orderData.put("userId", userId);
        orderData.put("customerName", name);
        orderData.put("address", address);
        orderData.put("shippingMethod", shippingMethod);
        orderData.put("paymentMethod", paymentMethod);
        orderData.put("status", "Đang giao hàng");
        orderData.put("timestamp", System.currentTimeMillis());

        // Lưu đơn hàng vào SharedPreferences để hiển thị tức thì ở Tab 2 Đơn Hàng
        String userKey = WishlistManager.getUserKey(this);
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String ordersKey = "placed_orders_" + userKey;
        java.util.Set<String> savedOrders = pref.getStringSet(ordersKey, new java.util.HashSet<>());
        java.util.Set<String> set = new java.util.HashSet<>(savedOrders != null ? savedOrders : new java.util.HashSet<>());
        String orderString = orderCode + "|||" + name + "|||" + address + "|||" + System.currentTimeMillis() + "|||Đang giao hàng";
        set.add(orderString);
        pref.edit().putStringSet(ordersKey, set).apply();

        if (checkoutItemsList.isEmpty()) {
            String pName = getIntent().getStringExtra("productName");
            String pPrice = getIntent().getStringExtra("productPrice");
            int pImg = getIntent().getIntExtra("productImage", R.drawable.iphonepromaxbac);
            if (pName != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", pName);
                map.put("price", pPrice != null ? pPrice : "2.490.000đ");
                map.put("quantity", 1);
                map.put("imageRes", pImg);
                checkoutItemsList.add(map);
            }
        }
        orderData.put("items", checkoutItemsList);

        if (ordersRef != null) {
            ordersRef.child(userId).child(orderCode).setValue(orderData);
            ordersRef.child(userKey).child(orderCode).setValue(orderData);
        }
        completeOrder(orderCode);
    }

    private void completeOrder(String orderCode) {
        showSuccessNotification(orderCode);
        CustomToast.show(this, "🎉 Đặt hàng thành công! Mã đơn: #" + orderCode);
        Intent intent = new Intent(CheckoutActivity.this, OrderSuccessActivity.class);
        intent.putExtra("ORDER_CODE", orderCode);
        startActivity(intent);
        finish();
    }

    private void showSuccessNotification(String orderCode) {
        String channelId = "order_success_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Thông báo đặt hàng",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi đơn hàng được đặt thành công");

            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        Intent intent = new Intent(this, TrackOrderActivity.class);
        intent.putExtra("ORDER_CODE", orderCode);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Đặt hàng thành công")
                .setContentText("Sản phẩm đơn hàng #" + orderCode + " đã được xác nhận")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (notificationManager != null) {
            notificationManager.notify(new Random().nextInt(), builder.build());
        }
    }

    private String generateOrderCode() {
        long timestamp = System.currentTimeMillis();
        String timePart = String.valueOf(timestamp).substring(9);
        int randomPart = new Random().nextInt(900) + 100;
        return "TZ" + timePart + randomPart;
    }

    private void updateProgress() {
        if (imgStep1 != null) imgStep1.setImageResource(R.drawable.ic_step_address);
        if (imgStep2 != null) imgStep2.setImageResource(R.drawable.ic_step_shipping);
        if (imgStep3 != null) imgStep3.setImageResource(R.drawable.ic_step_payment);

        if (tvStep1 != null) tvStep1.setTextColor(orangeColor);
        if (tvStep2 != null) tvStep2.setTextColor(orangeColor);
        if (tvStep3 != null) tvStep3.setTextColor(orangeColor);

        if (lineStep1 != null) lineStep1.setBackgroundColor(orangeColor);
        if (lineStep2 != null) lineStep2.setBackgroundColor(orangeColor);

        if (btnPlaceOrder != null) {
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setAlpha(1.0f);
        }
    }

    private void updateStepUI(boolean isDone, ImageView icon, TextView text) {
        if (icon == null || text == null) return;
        if (isDone) {
            icon.setImageResource(android.R.drawable.checkbox_on_background);
            icon.setImageTintList(ColorStateList.valueOf(orangeColor));
            text.setTextColor(orangeColor);
        } else {
            icon.setImageResource(android.R.drawable.btn_radio);
            icon.setImageTintList(ColorStateList.valueOf(orangeColor));
            text.setTextColor(orangeColor);
        }
    }

    private void resetStepUI(ImageView icon, TextView text) {
        if (icon == null || text == null) return;
        icon.setImageResource(android.R.drawable.btn_radio);
        icon.setImageTintList(ColorStateList.valueOf(grayColor));
        text.setTextColor(grayColor);
    }
}
