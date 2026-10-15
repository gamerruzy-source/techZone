package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    private RecyclerView rvFlashSale, rvForYou;
    private FlashSaleAdapter flashSaleAdapter;
    private ProductAdapter forYouAdapter;
    private List<Product> flashSaleList, forYouList;
    private List<Product> fullForYouList = new ArrayList<>();
    private android.os.CountDownTimer flashSaleTimer;
    private android.os.Handler autoScrollHandler;
    private Runnable autoScrollRunnable;
    private int currentFlashSalePos = 0;

    private androidx.viewpager2.widget.ViewPager2 vpHomeBanner;
    private BannerAdapter bannerAdapter;
    private android.os.Handler bannerHandler;
    private Runnable bannerRunnable;
    private int currentBannerPos = 0;
    private final int[] bannerImages = {R.drawable.banner1, R.drawable.banner2, R.drawable.banner3};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference();
            seedProducts();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final String role = getIntent().getStringExtra("role") != null ? getIntent().getStringExtra("role") : "user";

        // Profile / Avatar click handler
        View btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                intent.putExtra("role", role);
                startActivity(intent);
            });
        }

        // 1. Chuyển sang danh mục sản phẩm (Phone, Laptop, Audio & Watch)
        LinearLayout btnCategoryPhone = findViewById(R.id.btnCategoryPhone);
        LinearLayout btnCategoryLaptop = findViewById(R.id.btnCategoryLaptop);
        LinearLayout btnCategoryAudio = findViewById(R.id.btnCategoryAudio);
        LinearLayout btnCategoryWatch = findViewById(R.id.btnCategoryWatch);

        if (btnCategoryPhone != null) {
            btnCategoryPhone.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                intent.putExtra("category", "iPhone");
                startActivity(intent);
            });
        }

        if (btnCategoryLaptop != null) {
            btnCategoryLaptop.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                intent.putExtra("category", "Laptop");
                startActivity(intent);
            });
        }

        if (btnCategoryAudio != null) {
            btnCategoryAudio.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                intent.putExtra("category", "audio");
                startActivity(intent);
            });
        }

        if (btnCategoryWatch != null) {
            btnCategoryWatch.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                intent.putExtra("category", "watch");
                startActivity(intent);
            });
        }

        // 2. Chuyển sang giỏ hàng (Nút Giỏ Hàng btnOpenCart)
        View btnOpenCart = findViewById(R.id.btnOpenCart);
        if (btnOpenCart != null) {
            btnOpenCart.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, CartActivity.class)));
        }
        
        // Nút + Tạo/Quản lý sản phẩm (Chỉ DUY NHẤT hiển thị cho tài khoản admin / 123123)
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String savedEmail = pref.getString("saved_email", "");
        String savedPass = pref.getString("saved_password", "");
        String roleIntent = getIntent().getStringExtra("role");

        boolean isAdmin = ("admin".equalsIgnoreCase(roleIntent) || ("admin".equalsIgnoreCase(savedEmail) && "123123".equals(savedPass)));

        View btnCreateProduct = findViewById(R.id.btnCreateProduct);
        if (btnCreateProduct != null) {
            if (isAdmin) {
                btnCreateProduct.setVisibility(View.VISIBLE);
                btnCreateProduct.setOnClickListener(v -> startActivity(new Intent(HomeActivity.this, AdminProductsActivity.class)));
            } else {
                btnCreateProduct.setVisibility(View.GONE);
            }
        }

        // 3. Set up Search, Banner Carousel, Flash Sale & Dành Riêng Cho Bạn Sections
        setupSearchAndFilter();
        setupBannerCarousel();
        setupFlashSaleSection();
        setupForYouSection();

        // Scroll to top on start
        ScrollView scrollViewHome = findViewById(R.id.scrollViewHome);
        if (scrollViewHome != null) {
            scrollViewHome.post(() -> scrollViewHome.fullScroll(ScrollView.FOCUS_UP));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateHeaderUserInfo();
        updateCartBadgeCount();
        startBannerAutoScroll();
        startFlashSaleAutoScroll();
        ScrollView scrollViewHome = findViewById(R.id.scrollViewHome);
        if (scrollViewHome != null) {
            scrollViewHome.post(() -> scrollViewHome.fullScroll(ScrollView.FOCUS_UP));
        }
    }

    private void updateCartBadgeCount() {
        TextView tvCartBadgeCount = findViewById(R.id.tvCartBadgeCount);
        if (tvCartBadgeCount == null) return;

        String userKey = WishlistManager.getUserKey(this);
        FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
            .getReference("Carts").child(userKey)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    long totalItems = 0;
                    if (snapshot.exists() && snapshot.hasChildren()) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            Long qty = child.child("quantity").getValue(Long.class);
                            totalItems += (qty != null ? qty : 1);
                        }
                    }
                    if (tvCartBadgeCount != null) {
                        tvCartBadgeCount.setText(String.valueOf(totalItems));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
    }

    private void updateHeaderUserInfo() {
        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String name = pref.getString("saved_name", "TechZone Member");
        String avatarName = pref.getString("saved_avatar", "anhnam1");

        TextView tvHomeUserName = findViewById(R.id.tvHomeUserName);
        if (tvHomeUserName != null) {
            tvHomeUserName.setText(name);
        }

        // Dynamic Time-of-Day Greeting (Clean text without emojis)
        TextView tvHomeGreeting = findViewById(R.id.tvHomeGreeting);
        if (tvHomeGreeting != null) {
            int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
            String greeting;
            if (hour >= 5 && hour < 12) {
                greeting = "Chào buổi sáng";
            } else if (hour >= 12 && hour < 14) {
                greeting = "Chào buổi trưa";
            } else if (hour >= 14 && hour < 18) {
                greeting = "Chào buổi chiều";
            } else {
                greeting = "Chào buổi tối";
            }
            tvHomeGreeting.setText(greeting);
        }

        ImageView btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            int resId = getResources().getIdentifier(avatarName, "drawable", getPackageName());
            if (resId != 0) {
                btnSettings.setImageResource(resId);
            } else {
                btnSettings.setImageResource(R.drawable.anhnam1);
            }
        }
    }

    private void setupFlashSaleSection() {
        rvFlashSale = findViewById(R.id.rvFlashSale);
        if (rvFlashSale != null) {
            rvFlashSale.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rvFlashSale.setHasFixedSize(true);
            rvFlashSale.setItemViewCacheSize(6);
            flashSaleList = new ArrayList<>();
            flashSaleAdapter = new FlashSaleAdapter(flashSaleList, new FlashSaleAdapter.OnFlashSaleClickListener() {
                @Override
                public void onClick(Product product) {
                    openDetailProduct(product);
                }

                @Override
                public void onAddToCart(Product product) {
                    addToCart(product.getName(), product.getPrice(), product.getImageRes());
                }
            });
            rvFlashSale.setAdapter(flashSaleAdapter);
        }

        startFlashSaleCountdown();
        startFlashSaleAutoScroll();
        loadFlashSaleProducts();
    }

    private void startFlashSaleAutoScroll() {
        if (autoScrollHandler == null) {
            autoScrollHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        stopFlashSaleAutoScroll();

        autoScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (rvFlashSale != null && flashSaleAdapter != null && flashSaleAdapter.getItemCount() > 0) {
                    currentFlashSalePos = (currentFlashSalePos + 1) % flashSaleAdapter.getItemCount();
                    rvFlashSale.smoothScrollToPosition(currentFlashSalePos);
                }
                if (autoScrollHandler != null) {
                    autoScrollHandler.postDelayed(this, 3000);
                }
            }
        };

        autoScrollHandler.postDelayed(autoScrollRunnable, 3000);
    }

    private void stopFlashSaleAutoScroll() {
        if (autoScrollHandler != null && autoScrollRunnable != null) {
            autoScrollHandler.removeCallbacks(autoScrollRunnable);
        }
    }

    private void startFlashSaleCountdown() {
        TextView tvTimer = findViewById(R.id.tvFlashSaleTimer);
        if (tvTimer == null) return;

        if (flashSaleTimer != null) {
            flashSaleTimer.cancel();
        }

        long duration = (3 * 3600 + 45 * 60 + 12) * 1000L; // 3 hours 45 mins 12 secs
        flashSaleTimer = new android.os.CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long hours = (millisUntilFinished / (1000 * 60 * 60)) % 24;
                long minutes = (millisUntilFinished / (1000 * 60)) % 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                tvTimer.setText(String.format(java.util.Locale.US, "%02d:%02d:%02d", hours, minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText("00:00:00");
            }
        }.start();
    }

    private void setupBannerCarousel() {
        vpHomeBanner = findViewById(R.id.vpHomeBanner);
        if (vpHomeBanner == null) return;

        bannerAdapter = new BannerAdapter(bannerImages);
        vpHomeBanner.setAdapter(bannerAdapter);

        setupBannerDots(0);

        vpHomeBanner.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentBannerPos = position;
                setupBannerDots(position);
            }
        });

        startBannerAutoScroll();
    }

    private void setupBannerDots(int activePosition) {
        LinearLayout layoutBannerDots = findViewById(R.id.layoutBannerDots);
        if (layoutBannerDots == null) return;

        layoutBannerDots.removeAllViews();
        for (int i = 0; i < bannerImages.length; i++) {
            TextView dot = new TextView(this);
            dot.setText(" • ");
            dot.setTextSize(22f);
            if (i == activePosition) {
                dot.setTextColor(android.graphics.Color.parseColor("#FF6B00"));
            } else {
                dot.setTextColor(android.graphics.Color.parseColor("#CBD5E1"));
            }
            layoutBannerDots.addView(dot);
        }
    }

    private void startBannerAutoScroll() {
        if (bannerHandler == null) {
            bannerHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        }
        stopBannerAutoScroll();

        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                if (vpHomeBanner != null && bannerImages.length > 0) {
                    currentBannerPos = (currentBannerPos + 1) % bannerImages.length;
                    vpHomeBanner.setCurrentItem(currentBannerPos, true);
                }
                if (bannerHandler != null) {
                    bannerHandler.postDelayed(this, 3500);
                }
            }
        };

        bannerHandler.postDelayed(bannerRunnable, 3500);
    }

    private void stopBannerAutoScroll() {
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    @Override
    protected void onPause() {
        stopBannerAutoScroll();
        stopFlashSaleAutoScroll();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopBannerAutoScroll();
        stopFlashSaleAutoScroll();
        if (flashSaleTimer != null) {
            flashSaleTimer.cancel();
        }
        super.onDestroy();
    }

    private List<Product> masterForYouList = new ArrayList<>();
    private int loadedChunkCount = 8;

    private void renderForYouChunk() {
        if (forYouList == null || forYouAdapter == null) return;
        forYouList.clear();
        int max = Math.min(loadedChunkCount, masterForYouList.size());
        for (int i = 0; i < max; i++) {
            forYouList.add(masterForYouList.get(i));
        }
        fullForYouList.clear();
        fullForYouList.addAll(forYouList);
        forYouAdapter.notifyDataSetChanged();
    }

    private void loadNextChunkIfNeeded() {
        if (loadedChunkCount < masterForYouList.size()) {
            loadedChunkCount += 6;
            renderForYouChunk();
        }
    }

    private void setupForYouSection() {
        rvForYou = findViewById(R.id.rvForYou);
        if (rvForYou != null) {
            rvForYou.setLayoutManager(new GridLayoutManager(this, 2));
            rvForYou.setHasFixedSize(true);
            rvForYou.setItemViewCacheSize(6);
            forYouList = new ArrayList<>();
            forYouAdapter = new ProductAdapter(forYouList, new ProductAdapter.OnProductClickListener() {
                @Override
                public void onClick(Product product) {
                    openDetailProduct(product);
                }

                @Override
                public void onAddToCart(Product product) {
                    addToCart(product.getName(), product.getPrice(), product.getImageRes());
                }
            });
            rvForYou.setAdapter(forYouAdapter);
        }

        android.widget.ScrollView scrollViewHome = findViewById(R.id.scrollViewHome);
        if (scrollViewHome != null) {
            scrollViewHome.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                View child = scrollViewHome.getChildAt(0);
                if (child != null) {
                    int diff = (child.getBottom() - (scrollViewHome.getHeight() + scrollY));
                    if (diff <= 300) {
                        loadNextChunkIfNeeded();
                    }
                }
            });
        }

        loadForYouProducts();
    }

    private void loadFlashSaleProducts() {
        if (mDatabase == null) {
            useFallbackProducts();
            return;
        }

        mDatabase.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (flashSaleList != null) {
                    flashSaleList.clear();
                    int count = 0;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Product p = data.getValue(Product.class);
                        if (p != null) {
                            p.setId(data.getKey());
                            flashSaleList.add(p);
                            count++;
                            if (count >= 5) break;
                        }
                    }
                    if (flashSaleList.isEmpty()) {
                        useFallbackFlashSale();
                    } else if (flashSaleAdapter != null) {
                        flashSaleAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                useFallbackFlashSale();
            }
        });
    }

    private void loadForYouProducts() {
        if (mDatabase == null) {
            useFallbackProducts();
            return;
        }

        mDatabase.child("Products").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                masterForYouList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product p = data.getValue(Product.class);
                    if (p != null) {
                        p.setId(data.getKey());
                        masterForYouList.add(p);
                    }
                }
                if (masterForYouList.isEmpty()) {
                    useFallbackProducts();
                } else {
                    renderForYouChunk();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                useFallbackProducts();
            }
        });
    }

    private void useFallbackProducts() {
        masterForYouList.clear();
        masterForYouList.addAll(getFallbackProducts());
        renderForYouChunk();
    }

    private void setupSearchAndFilter() {
        android.widget.EditText edtHomeSearch = findViewById(R.id.edtHomeSearch);
        View btnHomeFilter = findViewById(R.id.btnHomeFilter);

        if (btnHomeFilter != null) {
            btnHomeFilter.setOnClickListener(v -> {
                String query = edtHomeSearch != null ? edtHomeSearch.getText().toString().trim() : "";
                Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                if (!query.isEmpty()) {
                    intent.putExtra("searchQuery", query);
                } else {
                    intent.putExtra("category", "All");
                }
                startActivity(intent);
            });
        }

        if (edtHomeSearch != null) {
            edtHomeSearch.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == android.view.KeyEvent.KEYCODE_ENTER)) {
                    String query = edtHomeSearch.getText().toString().trim();
                    if (!query.isEmpty()) {
                        Intent intent = new Intent(HomeActivity.this, ProductListActivity.class);
                        intent.putExtra("searchQuery", query);
                        startActivity(intent);
                        return true;
                    }
                }
                return false;
            });

            edtHomeSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterHomeProducts(s.toString().trim());
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }
    }

    private void filterHomeProducts(String query) {
        if (forYouList == null || forYouAdapter == null) return;
        forYouList.clear();
        if (query.isEmpty()) {
            forYouList.addAll(fullForYouList);
        } else {
            String q = query.toLowerCase();
            for (Product p : fullForYouList) {
                if ((p.getName() != null && p.getName().toLowerCase().contains(q)) ||
                    (p.getCategory() != null && p.getCategory().toLowerCase().contains(q))) {
                    forYouList.add(p);
                }
            }
        }
        forYouAdapter.notifyDataSetChanged();
    }

    private void useFallbackFlashSale() {
        if (flashSaleList != null) {
            flashSaleList.clear();
            List<Product> all = getFallbackProducts();
            for (int i = 0; i < Math.min(5, all.size()); i++) {
                flashSaleList.add(all.get(i));
            }
            if (flashSaleAdapter != null) flashSaleAdapter.notifyDataSetChanged();
        }
    }



    private List<Product> getFallbackProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("ip1", "iPhone Pro Max - Graphite", "Graphite (Xám Đen)", "28.990.000đ", "32.990.000đ", "256GB", "Mới 100%", 15, "12 tháng", "Thiết kế mờ sang trọng", "iPhone Pro Max phiên bản Graphite mang đến vẻ đẹp cổ điển lịch lãm. Màn hình Super Retina XDR 120Hz mượt mà, khung Titanium siêu bền nhẹ, hệ thống camera 48MP zoom quang 5x ấn tượng.", "iPhone", R.drawable.iphonepromaxgraphite));
        list.add(new Product("ip2", "iPhone Pro Max - Bạc", "Bạc (Silver)", "29.490.000đ", "33.490.000đ", "256GB", "Mới 100%", 20, "12 tháng", "Màu Bạc tinh tế", "iPhone Pro Max màu Bạc tôn lên sự hiện đại đẳng cấp. Trang bị viên pin dung lượng lớn cho thời lượng sử dụng cả ngày dài, cổng sạc USB-C truyền dữ liệu tốc độ cao.", "iPhone", R.drawable.iphonepromaxbac));
        list.add(new Product("ip3", "iPhone Pro Max - Xanh", "Xanh (Blue Titanium)", "29.990.000đ", "34.000.000đ", "512GB", "Mới 100%", 10, "12 tháng", "Sắc xanh Titanium", "Phiên bản màu Xanh Titanium cá tính, bộ nhớ 512GB thoải mái lưu trữ video 4K ProRes. Nút Action Button tuỳ chỉnh tiện lợi cho trải nghiệm cá nhân hoá vượt trội.", "iPhone", R.drawable.iphonepromaxxanh));
        list.add(new Product("ip4", "iPhone Pro Max - Vàng", "Vàng (Gold)", "31.990.000đ", "35.990.000đ", "512GB", "Mới 100%", 8, "12 tháng", "Màu Vàng tự nhiên", "iPhone Pro Max phiên bản Vàng tự nhiên quý phái, màn hình luôn bật Always-On Display đỉnh cao. Khả năng chiến game nặng mượt mà không lo giật lag.", "iPhone", R.drawable.iphonepromaxvang));
        list.add(new Product("ip5", "iPhone Pro Max - Đỏ", "Đỏ (Product RED)", "33.990.000đ", "37.990.000đ", "1TB", "Mới 100%", 5, "12 tháng", "Phiên bản Đỏ quyến rũ", "Bản đặc biệt Product RED với dung lượng tối đa 1TB dành riêng cho người dùng chuyên nghiệp. Khả năng quay chụp điện ảnh chuyên nghiệp đỉnh nhất thế giới smartphone.", "iPhone", R.drawable.iphonpromaxdo));

        list.add(new Product("lt1", "Laptop Gaming - Đen", "Đen", "25.990.000đ", "28.990.000đ", "", "Mới 100%", 10, "24 tháng", "Intel Core i7 | 16GB | 1TB | RTX 4060", "Laptop gaming hiệu năng cao...", "Laptop", R.drawable.laptopgamingden, "Intel Core i7", "16GB", "1TB", "RTX 4060", "15.6 inch 144Hz", "CPU: Intel Core i7\nRAM: 16GB"));
        list.add(new Product("lt2", "Laptop Slim - Bạc", "Bạc", "18.990.000đ", "21.990.000đ", "", "Mới 100%", 12, "24 tháng", "Intel Core i5 | 16GB | 512GB", "Laptop mỏng nhẹ...", "Laptop", R.drawable.laptopslimbac, "Intel Core i5", "16GB", "512GB", "Intel Iris Xe", "14 inch Full HD", "CPU: Intel Core i5\nRAM: 16GB"));
        list.add(new Product("lt3", "Laptop Văn Phòng - Xám", "Xám", "13.990.000đ", "15.990.000đ", "", "Mới 100%", 15, "12 tháng", "Intel Core i5 | 8GB | 512GB", "Laptop văn phòng...", "Laptop", R.drawable.laptopvanphongxam, "Intel Core i5", "8GB", "512GB", "Intel UHD", "15.6 inch Full HD", "CPU: Intel Core i5\nRAM: 8GB"));
        list.add(new Product("lt4", "Laptop Pastel - Trắng", "Trắng", "20.990.000đ", "23.990.000đ", "", "Mới 100%", 8, "24 tháng", "Intel Core Ultra 5 | 16GB | 512GB", "Laptop thiết kế cao cấp...", "Laptop", R.drawable.laptoppasteltrang, "Intel Core Ultra 5", "16GB", "512GB", "Intel Arc", "14 inch 2K", "CPU: Intel Core Ultra 5\nRAM: 16GB"));
        list.add(new Product("lt5", "Laptop Gập - Xanh", "Xanh", "23.990.000đ", "26.990.000đ", "", "Mới 100%", 7, "24 tháng", "Intel Core Ultra 7 | 16GB | 1TB", "Laptop 2 trong 1...", "Laptop", R.drawable.laptopgapxanh, "Intel Core Ultra 7", "16GB", "1TB", "Intel Arc", "14 inch Cảm ứng", "CPU: Intel Core Ultra 7\nRAM: 16GB"));

        list.add(new Product("au1", "Tai nghe chụp tai Premium - Đen", "Đen", "2.490.000đ", "2.990.000đ", "", "Mới 100%", 15, "12 tháng", "Over-ear | Bluetooth 5.3 | Pin 40h | ANC", "Tai nghe chụp tai cao cấp...", "audio", R.drawable.tainghechuptaiden));
        list.add(new Product("au2", "Tai nghe True Wireless - Trắng", "Trắng", "1.990.000đ", "2.290.000đ", "", "Mới 100%", 20, "12 tháng", "True Wireless | Bluetooth 5.3 | Pin 30h", "Tai nghe không dây nhỏ gọn...", "audio", R.drawable.tainghetrang));
        list.add(new Product("au3", "Tai nghe Gaming RGB - Đen Đỏ", "Đen Đỏ", "1.590.000đ", "1.890.000đ", "", "Mới 100%", 18, "12 tháng", "Gaming Headset | USB / 3.5mm | RGB", "Tai nghe gaming...", "audio", R.drawable.tainghegamingdendo));
        list.add(new Product("au4", "Tai nghe Wireless Premium - Kem", "Kem", "2.790.000đ", "3.190.000đ", "", "Mới 100%", 12, "18 tháng", "Over-ear Wireless | Bluetooth 5.3", "Tai nghe không dây...", "audio", R.drawable.tainghekem));
        list.add(new Product("au5", "Tai nghe Thể Thao - Xanh", "Xanh", "1.290.000đ", "1.590.000đ", "", "Mới 100%", 25, "12 tháng", "In-ear Thể thao | Bluetooth 5.3 | IPX5", "Tai nghe thể thao...", "audio", R.drawable.tainghethethaoxanh));

        list.add(new Product("wt1", "Watch Sport - Đen", "Đen", "3.490.000đ", "3.990.000đ", "", "Mới 100%", 15, "12 tháng", "Smart Watch | AMOLED 1.9\" | GPS", "Đồng hồ thông minh thể thao...", "watch", R.drawable.watchdenden));
        list.add(new Product("wt2", "Watch Classic - Nâu", "Nâu", "4.290.000đ", "4.790.000đ", "", "Mới 100%", 12, "18 tháng", "Classic | AMOLED 1.43\" Dây da", "Đồng hồ cổ điển...", "watch", R.drawable.watchnaunau));
        list.add(new Product("wt3", "Watch Outdoor - Cam", "Cam", "5.490.000đ", "5.990.000đ", "", "Mới 100%", 10, "18 tháng", "Outdoor | AMOLED 1.5\" | GPS | 10ATM", "Đồng hồ outdoor...", "watch", R.drawable.watchcam));
        list.add(new Product("wt4", "Watch Fit - Xanh", "Xanh", "1.690.000đ", "1.990.000đ", "", "Mới 100%", 20, "12 tháng", "Fitness Watch | AMOLED 1.6\"", "Đồng hồ fitness...", "watch", R.drawable.watchxanh));
        list.add(new Product("wt5", "Watch Luxury - Vàng Kem", "Vàng Kem", "4.990.000đ", "5.490.000đ", "", "Mới 100%", 8, "24 tháng", "Premium Smart Watch | AMOLED 1.9\"", "Đồng hồ cao cấp...", "watch", R.drawable.watchvangkem));

        return list;
    }

    private void openDetailProduct(Product product) {
        Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
        intent.putExtra("productId", product.getId());
        intent.putExtra("productName", product.getName());
        intent.putExtra("productPrice", product.getPrice());
        intent.putExtra("productOldPrice", product.getOldPrice());
        intent.putExtra("productImage", product.getImageRes());
        intent.putExtra("productCategory", product.getCategory());
        intent.putExtra("productColor", product.getColor());
        
        intent.putExtra("productCpu", product.getCpu());
        intent.putExtra("productRam", product.getRam());
        intent.putExtra("productSsd", product.getSsd());
        intent.putExtra("productGpu", product.getGpu());
        intent.putExtra("productScreen", product.getScreen());
        
        intent.putExtra("productType", product.getType());
        intent.putExtra("productConnection", product.getConnection());
        intent.putExtra("productBattery", product.getBattery());
        intent.putExtra("productAnc", product.getAnc());
        intent.putExtra("productMic", product.getMic());

        intent.putExtra("productWatchType", product.getWatchType());
        intent.putExtra("productGps", product.getGps());
        intent.putExtra("productHealth", product.getHealth());
        intent.putExtra("productWaterproof", product.getWaterproof());

        intent.putExtra("productStatus", product.getStatus());
        intent.putExtra("productStock", product.getStock());
        intent.putExtra("productWarranty", product.getWarranty());
        intent.putExtra("productDesc", product.getDesc());
        intent.putExtra("productSpecs", product.getSpecs());
        startActivity(intent);
    }

    private void seedProducts() {
        if (mDatabase == null) return;
        try {
            // Seed iPhone Products
            addIPhoneProduct("ip1", "iPhone Pro Max", "Graphite (Xám Đen)", "28.990.000đ", "32.990.000đ", "256GB", "Mới 100% Nguyên Seal", 15, "12 Tháng Chính Hãng Apple", "Thiết kế mờ sang trọng, chip A17 Pro mạnh mẽ", "iPhone Pro Max phiên bản Graphite mang đến vẻ đẹp cổ điển lịch lãm. Màn hình Super Retina XDR 120Hz mượt mà, khung Titanium siêu bền nhẹ, hệ thống camera 48MP zoom quang 5x ấn tượng.", "iPhone", R.drawable.iphonepromaxgraphite);
            addIPhoneProduct("ip2", "iPhone Pro Max", "Bạc (Silver)", "29.490.000đ", "33.490.000đ", "256GB", "Mới 100% Nguyên Seal", 20, "12 Tháng Chính Hãng Apple", "Màu Bạc tinh tế, viền Titanium sáng bóng", "iPhone Pro Max màu Bạc tôn lên sự hiện đại đẳng cấp. Trang bị viên pin dung lượng lớn cho thời lượng sử dụng cả ngày dài, cổng sạc USB-C truyền dữ liệu tốc độ cao.", "iPhone", R.drawable.iphonepromaxbac);
            addIPhoneProduct("ip3", "iPhone Pro Max", "Xanh (Blue Titanium)", "29.990.000đ", "34.000.000đ", "512GB", "Mới 100% Nguyên Seal", 10, "12 Tháng Chính Hãng Apple", "Sắc xanh Titanium ấn tượng và độc đáo", "Phiên bản màu Xanh Titanium cá tính, bộ nhớ 512GB thoải mái lưu trữ video 4K ProRes. Nút Action Button tuỳ chỉnh tiện lợi cho trải nghiệm cá nhân hoá vượt trội.", "iPhone", R.drawable.iphonepromaxxanh);
            addIPhoneProduct("ip4", "iPhone Pro Max", "Vàng (Gold)", "31.990.000đ", "35.990.000đ", "512GB", "Mới 100% Nguyên Seal", 8, "12 Tháng Chính Hãng Apple", "Màu Vàng tự nhiên đẳng cấp Hoàng gia", "iPhone Pro Max phiên bản Vàng tự nhiên quý phái, màn hình luôn bật Always-On Display đỉnh cao. Khả năng chiến game nặng mượt mà không lo giật lag.", "iPhone", R.drawable.iphonepromaxvang);
            addIPhoneProduct("ip5", "iPhone Pro Max", "Đỏ (Product RED)", "33.990.000đ", "37.990.000đ", "1TB", "Mới 100% Nguyên Seal", 5, "12 Tháng Chính Hãng Apple", "Phiên bản Đỏ quyến rũ, dung lượng 1TB cực khủng", "Bản đặc biệt Product RED với dung lượng tối đa 1TB dành riêng cho người dùng chuyên nghiệp. Khả năng quay chụp điện ảnh chuyên nghiệp đỉnh nhất thế giới smartphone.", "iPhone", R.drawable.iphonpromaxdo);

            // Seed 5 Laptop Products
            addLaptopProduct("lt1", "Laptop Gaming - Đen", "Đen", "25.990.000đ", "28.990.000đ",
                    "Intel Core i7", "16GB", "1TB", "RTX 4060", "15.6 inch 144Hz",
                    "Mới 100%", 10, "24 tháng",
                    "Intel Core i7 | RAM 16GB | SSD 1TB | RTX 4060 | 15.6\" 144Hz",
                    "Laptop gaming hiệu năng cao, phù hợp chơi game, lập trình, đồ họa và xử lý các tác vụ nặng. Thiết kế mạnh mẽ, bàn phím RGB, hệ thống tản nhiệt tốt.",
                    "CPU: Intel Core i7\nRAM: 16GB DDR5\nSSD: 1TB NVMe PCIe 4.0\nGPU: NVIDIA GeForce RTX 4060 8GB\nMàn hình: 15.6 inch Full HD (1920x1080) 144Hz IPS\nPin: 4-cell 90Whr\nTrọng lượng: 2.2 kg",
                    R.drawable.laptopgamingden);

            addLaptopProduct("lt2", "Laptop Slim - Bạc", "Bạc", "18.990.000đ", "21.990.000đ",
                    "Intel Core i5", "16GB", "512GB", "Intel Iris Xe", "14 inch Full HD",
                    "Mới 100%", 12, "24 tháng",
                    "Intel Core i5 | RAM 16GB | SSD 512GB | 14\" Full HD",
                    "Laptop mỏng nhẹ, thiết kế hiện đại, phù hợp sinh viên, nhân viên văn phòng, học tập và làm việc hàng ngày.",
                    "CPU: Intel Core i5\nRAM: 16GB LPDDR5\nSSD: 512GB NVMe PCIe\nGPU: Intel Iris Xe Graphics\nMàn hình: 14 inch Full HD (1920x1080) IPS Anti-glare\nPin: 3-cell 50Whr\nTrọng lượng: 1.3 kg",
                    R.drawable.laptopslimbac);

            addLaptopProduct("lt3", "Laptop Văn Phòng - Xám", "Xám", "13.990.000đ", "15.990.000đ",
                    "Intel Core i5", "8GB", "512GB", "Intel UHD Graphics", "15.6 inch Full HD",
                    "Mới 100%", 15, "12 tháng",
                    "Intel Core i5 | RAM 8GB | SSD 512GB | 15.6\" Full HD",
                    "Laptop văn phòng có hiệu năng ổn định, bàn phím rộng, màn hình lớn và thời lượng pin tốt. Phù hợp Word, Excel, học online và công việc văn phòng.",
                    "CPU: Intel Core i5\nRAM: 8GB DDR4\nSSD: 512GB NVMe\nGPU: Intel UHD Graphics\nMàn hình: 15.6 inch Full HD (1920x1080)\nPin: 3-cell 42Whr\nTrọng lượng: 1.7 kg",
                    R.drawable.laptopvanphongxam);

            addLaptopProduct("lt4", "Laptop Pastel - Trắng", "Trắng", "20.990.000đ", "23.990.000đ",
                    "Intel Core Ultra 5", "16GB", "512GB", "Intel Arc Graphics", "14 inch 2K",
                    "Mới 100%", 8, "24 tháng",
                    "Intel Core Ultra 5 | RAM 16GB | SSD 512GB | 14\" 2K",
                    "Laptop thiết kế cao cấp, màu sáng hiện đại, màn hình đẹp, phù hợp làm việc, thiết kế nhẹ và giải trí.",
                    "CPU: Intel Core Ultra 5\nRAM: 16GB LPDDR5X\nSSD: 512GB PCIe 4.0\nGPU: Intel Arc Graphics\nMàn hình: 14 inch 2K (2240x1400) IPS 100% sRGB\nPin: 4-cell 65Whr\nTrọng lượng: 1.25 kg",
                    R.drawable.laptoppasteltrang);

            addLaptopProduct("lt5", "Laptop Gập - Xanh", "Xanh", "23.990.000đ", "26.990.000đ",
                    "Intel Core Ultra 7", "16GB", "1TB", "Intel Arc Graphics", "14 inch Cảm ứng",
                    "Mới 100%", 7, "24 tháng",
                    "Intel Core Ultra 7 | RAM 16GB | SSD 1TB | 14\" Cảm ứng",
                    "Laptop 2 trong 1 có thể gập linh hoạt, hỗ trợ màn hình cảm ứng, phù hợp học tập, làm việc, thuyết trình và sử dụng như máy tính bảng.",
                    "CPU: Intel Core Ultra 7\nRAM: 16GB LPDDR5X\nSSD: 1TB NVMe PCIe 4.0\nGPU: Intel Arc Graphics\nMàn hình: 14 inch FHD+ (1920x1200) Touchscreen xoay gập 360 độ\nPin: 4-cell 70Whr\nTrọng lượng: 1.4 kg",
                    R.drawable.laptopgapxanh);

            // Seed 5 Audio Products
            addAudioProduct("au1", "Tai nghe chụp tai Premium - Đen", "Đen", "2.490.000đ", "2.990.000đ",
                    "Over-ear", "Bluetooth 5.3 / USB-C", "40 giờ", "ANC (Chống ồn chủ động)", "Có đàm thoại HD",
                    "Mới 100%", 15, "12 tháng",
                    "Over-ear | Bluetooth 5.3 | Pin 40h | ANC | Mic",
                    "Tai nghe chụp tai cao cấp với đệm tai mềm, thiết kế sang trọng và khả năng cách âm tốt. Hỗ trợ chống ồn chủ động ANC, âm thanh chi tiết, bass mạnh và thời lượng pin dài. Phù hợp nghe nhạc, xem phim, học tập và làm việc.",
                    "Loại: Over-ear\nKết nối: Bluetooth 5.3 / USB-C\nPin: 40 giờ\nChống ồn: ANC\nMicro: Có\nMàu sắc: Đen\nBảo hành: 12 tháng",
                    R.drawable.tainghechuptaiden);

            addAudioProduct("au2", "Tai nghe True Wireless - Trắng", "Trắng", "1.990.000đ", "2.290.000đ",
                    "True Wireless", "Bluetooth 5.3", "6h tai nghe + 24h hộp (Tổng 30h)", "ANC (Chống ồn chủ động)", "Có",
                    "Mới 100%", 20, "12 tháng",
                    "True Wireless | Bluetooth 5.3 | Pin 30h | ANC | Mic",
                    "Tai nghe không dây nhỏ gọn đi kèm hộp sạc. Thiết kế in-ear ôm tai, hỗ trợ chống ồn, micro đàm thoại rõ ràng và kết nối Bluetooth ổn định.",
                    "Loại: True Wireless In-ear\nKết nối: Bluetooth 5.3\nPin: 6 giờ tai nghe + 24 giờ hộp sạc (Tổng 30h)\nChống ồn: ANC\nMicro: Có\nMàu sắc: Trắng\nBảo hành: 12 tháng",
                    R.drawable.tainghetrang);

            addAudioProduct("au3", "Tai nghe Gaming RGB - Đen Đỏ", "Đen Đỏ", "1.590.000đ", "1.890.000đ",
                    "Gaming Headset", "USB / 3.5mm", "Dây cáp / LED RGB", "Chống ồn thụ động", "Micro cần xoay 360°",
                    "Mới 100%", 18, "12 tháng",
                    "Gaming Headset | USB / 3.5mm | Đèn RGB | Micro cần | Surround",
                    "Tai nghe gaming có thiết kế mạnh mẽ, đèn RGB, đệm tai lớn và micro chuyên dụng. Phù hợp chơi game, livestream và voice chat.",
                    "Loại: Gaming Headset Over-ear\nKết nối: USB / Jack 3.5mm\nĐèn LED: RGB đổi màu\nÂm thanh: Surround 7.1\nMicro: Micro cần lọc tiếng ồn\nMàu sắc: Đen Đỏ\nBảo hành: 12 tháng",
                    R.drawable.tainghegamingdendo);

            addAudioProduct("au4", "Tai nghe Wireless Premium - Kem", "Kem", "2.790.000đ", "3.190.000đ",
                    "Over-ear Wireless", "Bluetooth 5.3", "45 giờ", "ANC (Chống ồn chủ động)", "Có đàm thoại kép",
                    "Mới 100%", 12, "18 tháng",
                    "Over-ear Wireless | Bluetooth 5.3 | Pin 45h | ANC | Mic",
                    "Tai nghe không dây thiết kế tối giản, cao cấp, đệm tai mềm và trọng lượng nhẹ. Hỗ trợ chống ồn chủ động ANC, âm thanh trung thực và thời lượng pin vượt trội.",
                    "Loại: Over-ear Wireless\nKết nối: Bluetooth 5.3 / Jack 3.5mm\nPin: 45 giờ\nChống ồn: ANC thông minh\nMicro: Kép đàm thoại khử ồn\nMàu sắc: Kem\nBảo hành: 18 tháng",
                    R.drawable.tainghekem);

            addAudioProduct("au5", "Tai nghe Thể Thao - Xanh", "Xanh", "1.290.000đ", "1.590.000đ",
                    "In-ear Thể thao", "Bluetooth 5.3", "18 giờ", "Kháng nước IPX5", "Có",
                    "Mới 100%", 25, "12 tháng",
                    "In-ear Thể thao | Bluetooth 5.3 | Pin 18h | IPX5 Kháng nước | Mic",
                    "Tai nghe thể thao chống nước IPX5, ôm sát vành tai không rơi khi vận động mạnh. Âm bass mạnh mẽ tạo động lực tập luyện.",
                    "Loại: In-ear Thể thao Neckband\nKết nối: Bluetooth 5.3\nPin: 18 giờ\nChống nước: Standard IPX5\nMicro: Có\nMàu sắc: Xanh\nBảo hành: 12 tháng",
                    R.drawable.tainghethethaoxanh);

            // Seed 5 Watch Products
            addWatchProduct("wt1", "Watch Sport - Đen", "Đen", "3.490.000đ", "3.990.000đ",
                    "Smart Watch", "AMOLED 1.9 inch", "Bluetooth 5.3", "Có", "Đo nhịp tim, Giấc ngủ, Đếm bước chân, Calo",
                    "5ATM", "36 giờ", "Mới 100%", 15, "12 tháng",
                    "Smart Watch | AMOLED 1.9\" | GPS | Đo nhịp tim, Giấc ngủ | 5ATM | Pin 36h",
                    "Đồng hồ thông minh thiết kế thể thao, màn hình lớn, hiển thị sắc nét và hỗ trợ nhiều chế độ luyện tập. Theo dõi nhịp tim, bước chân, lượng calo tiêu thụ và giấc ngủ. Phù hợp sử dụng hàng ngày, tập gym và chạy bộ.",
                    "Loại: Smart Watch\nMàn hình: AMOLED 1.9 inch\nKết nối: Bluetooth 5.3\nGPS: Có\nTheo dõi sức khỏe: Nhịp tim, Giấc ngủ, Bước chân, Calo\nChống nước: 5ATM\nPin: 36 giờ\nMàu sắc: Đen\nBảo hành: 12 tháng",
                    R.drawable.watchdenden);

            addWatchProduct("wt2", "Watch Classic - Nâu", "Nâu", "4.290.000đ", "4.790.000đ",
                    "Smart Watch Classic", "AMOLED 1.43 inch (Dây da)", "Bluetooth 5.3", "Có", "Nhịp tim, Giấc ngủ, Vận động",
                    "5ATM", "5 ngày", "Mới 100%", 12, "18 tháng",
                    "Classic | AMOLED 1.43\" Dây da | GPS | Đa tiện ích sức khỏe | Pin 5 ngày",
                    "Đồng hồ thông minh phong cách cổ điển với mặt tròn và dây da nâu. Thiết kế sang trọng, phù hợp đi làm, gặp khách hàng hoặc sử dụng hàng ngày. Hỗ trợ đo nhịp tim, theo dõi vận động, giấc ngủ và thông báo từ điện thoại.",
                    "Loại: Smart Watch Classic (Dây da)\nMàn hình: AMOLED 1.43 inch\nKết nối: Bluetooth 5.3\nGPS: Có\nTheo dõi sức khỏe: Đo nhịp tim, Giấc ngủ, Vận động\nChống nước: 5ATM\nPin: 5 ngày\nMàu sắc: Nâu\nBảo hành: 18 tháng",
                    R.drawable.watchnaunau);

            addWatchProduct("wt3", "Watch Outdoor - Cam", "Cam", "5.490.000đ", "5.990.000đ",
                    "Outdoor Smart Watch", "AMOLED 1.5 inch", "Bluetooth 5.3", "GPS đa băng tần (La bàn, Đo độ cao)", "Nhịp tim, SpO2, Thể thao ngoài trời",
                    "10ATM", "10 ngày", "Mới 100%", 10, "18 tháng",
                    "Outdoor | AMOLED 1.5\" | GPS đa băng tần | La bàn & Đo độ cao | 10ATM | Pin 10 ngày",
                    "Đồng hồ thông minh dành cho thể thao ngoài trời với thiết kế chắc chắn, dây silicon màu cam nổi bật. Hỗ trợ GPS, la bàn, theo dõi độ cao, thời tiết và nhiều chế độ thể thao. Phù hợp trekking, chạy bộ, đạp xe và hoạt động ngoài trời.",
                    "Loại: Outdoor Smart Watch\nMàn hình: AMOLED 1.5 inch\nKết nối: Bluetooth 5.3\nGPS: GPS đa băng tần (La bàn, Đo độ cao)\nTheo dõi sức khỏe: Nhịp tim, SpO2, Thể thao ngoài trời\nChống nước: 10ATM\nPin: 10 ngày\nMàu sắc: Cam\nBảo hành: 18 tháng",
                    R.drawable.watchcam);

            addWatchProduct("wt4", "Watch Fit - Xanh", "Xanh", "1.690.000đ", "1.990.000đ",
                    "Smart Band / Fitness Watch", "AMOLED 1.6 inch", "Bluetooth 5.3", "Không (Qua Smartphone)", "Đo nhịp tim, Giấc ngủ, Bước chân, Calo",
                    "5ATM", "7 ngày", "Mới 100%", 20, "12 tháng",
                    "Fitness Watch | AMOLED 1.6\" | Đo nhịp tim, Giấc ngủ, Calo | Pin 7 ngày",
                    "Đồng hồ theo dõi sức khỏe nhỏ gọn, nhẹ và dễ đeo trong thời gian dài. Hỗ trợ đo nhịp tim, bước chân, lượng calo, giấc ngủ và nhiều chế độ tập luyện. Phù hợp học sinh, sinh viên và người thường xuyên tập thể thao.",
                    "Loại: Smart Band / Fitness Watch\nMàn hình: AMOLED 1.6 inch\nKết nối: Bluetooth 5.3\nTheo dõi sức khỏe: Đo nhịp tim, Giấc ngủ, Bước chân, Calo\nChống nước: 5ATM\nPin: 7 ngày\nMàu sắc: Xanh\nBảo hành: 12 tháng",
                    R.drawable.watchxanh);

            addWatchProduct("wt5", "Watch Luxury - Vàng Kem", "Vàng Kem", "4.990.000đ", "5.490.000đ",
                    "Premium Smart Watch", "AMOLED 1.9 inch (Khung kim loại mạ vàng)", "Bluetooth 5.3", "Có", "Nhịp tim, Giấc ngủ, Vận động, Thông báo",
                    "5ATM", "3 ngày", "Mới 100%", 8, "24 tháng",
                    "Premium Smart Watch | AMOLED 1.9\" Khung mạ vàng | GPS | Pin 3 ngày",
                    "Đồng hồ thông minh cao cấp với khung kim loại màu vàng và dây màu kem. Thiết kế sang trọng, phù hợp thời trang và công việc. Hỗ trợ theo dõi sức khỏe, vận động, giấc ngủ, nhận thông báo và điều khiển một số tính năng từ điện thoại.",
                    "Loại: Premium Smart Watch\nMàn hình: AMOLED 1.9 inch (Khung kim loại mạ vàng)\nKết nối: Bluetooth 5.3\nGPS: Có\nTheo dõi sức khỏe: Nhịp tim, Giấc ngủ, Vận động, Thông báo\nChống nước: 5ATM\nPin: 3 ngày\nMàu sắc: Vàng Kem\nBảo hành: 24 tháng",
                    R.drawable.watchvangkem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addIPhoneProduct(String id, String name, String color, String price, String oldPrice, String capacity,
                                   String status, int stock, String warranty, String shortDesc, String desc,
                                   String category, int imageRes) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", id);
        p.put("name", name + " - " + color);
        p.put("color", color);
        p.put("price", price);
        p.put("oldPrice", oldPrice);
        p.put("capacity", capacity);
        p.put("status", status);
        p.put("stock", stock);
        p.put("warranty", warranty);
        p.put("shortDesc", shortDesc);
        p.put("desc", desc);
        p.put("category", category);
        p.put("imageRes", imageRes);

        mDatabase.child("Products").child(id).setValue(p);
    }

    private void addLaptopProduct(String id, String name, String color, String price, String oldPrice,
                                  String cpu, String ram, String ssd, String gpu, String screen,
                                  String status, int stock, String warranty, String shortDesc, String desc,
                                  String specs, int imageRes) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", id);
        p.put("name", name);
        p.put("color", color);
        p.put("price", price);
        p.put("oldPrice", oldPrice);
        p.put("cpu", cpu);
        p.put("ram", ram);
        p.put("ssd", ssd);
        p.put("gpu", gpu);
        p.put("screen", screen);
        p.put("status", status);
        p.put("stock", stock);
        p.put("warranty", warranty);
        p.put("shortDesc", shortDesc);
        p.put("desc", desc);
        p.put("specs", specs);
        p.put("category", "Laptop");
        p.put("imageRes", imageRes);

        mDatabase.child("Products").child(id).setValue(p);
    }

    private void addAudioProduct(String id, String name, String color, String price, String oldPrice,
                                 String type, String connection, String battery, String anc, String mic,
                                 String status, int stock, String warranty, String shortDesc, String desc,
                                 String specs, int imageRes) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", id);
        p.put("name", name);
        p.put("color", color);
        p.put("price", price);
        p.put("oldPrice", oldPrice);
        p.put("type", type);
        p.put("connection", connection);
        p.put("battery", battery);
        p.put("anc", anc);
        p.put("mic", mic);
        p.put("status", status);
        p.put("stock", stock);
        p.put("warranty", warranty);
        p.put("shortDesc", shortDesc);
        p.put("desc", desc);
        p.put("specs", specs);
        p.put("category", "audio");
        p.put("imageRes", imageRes);

        mDatabase.child("Products").child(id).setValue(p);
    }

    private void addWatchProduct(String id, String name, String color, String price, String oldPrice,
                                 String watchType, String screen, String connection, String gps, String health,
                                 String waterproof, String battery, String status, int stock, String warranty,
                                 String shortDesc, String desc, String specs, int imageRes) {
        Map<String, Object> p = new HashMap<>();
        p.put("id", id);
        p.put("name", name);
        p.put("color", color);
        p.put("price", price);
        p.put("oldPrice", oldPrice);
        p.put("watchType", watchType);
        p.put("screen", screen);
        p.put("connection", connection);
        p.put("gps", gps);
        p.put("health", health);
        p.put("waterproof", waterproof);
        p.put("battery", battery);
        p.put("status", status);
        p.put("stock", stock);
        p.put("warranty", warranty);
        p.put("shortDesc", shortDesc);
        p.put("desc", desc);
        p.put("specs", specs);
        p.put("category", "watch");
        p.put("imageRes", imageRes);

        mDatabase.child("Products").child(id).setValue(p);
    }

    private void addToCart(String name, String price, int imageRes) {
        String userId;
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            userId = WishlistManager.getUserKey(this);
        }
        
        String cartItemId = name.replaceAll("[^a-zA-Z0-9]", "");
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("quantity", 1);
        item.put("imageRes", imageRes);

        if (mDatabase != null) {
            mDatabase.child("Carts").child(userId).child(cartItemId).setValue(item)
                .addOnSuccessListener(aVoid -> CustomToast.show(this, "🛒 Đã thêm vào giỏ hàng!"))
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}
