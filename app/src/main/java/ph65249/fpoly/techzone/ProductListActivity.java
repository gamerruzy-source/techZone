package ph65249.fpoly.techzone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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

public class ProductListActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private RecyclerView rvProducts;
    private ProductAdapter adapter;
    private List<Product> productList;
    private TextView tvCategoryTitle;
    private MaterialButton btnFilterWatch, btnFilterAudio, btnFilterLaptop, btnFilterPhone, btnFilterAll;
    private String currentCategory = "watch";

    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference("Products");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        btnFilterWatch = findViewById(R.id.btnFilterWatch);
        btnFilterAudio = findViewById(R.id.btnFilterAudio);
        btnFilterLaptop = findViewById(R.id.btnFilterLaptop);
        btnFilterPhone = findViewById(R.id.btnFilterPhone);
        btnFilterAll = findViewById(R.id.btnFilterAll);

        String categoryExtra = getIntent().getStringExtra("category");
        if (categoryExtra != null && !categoryExtra.isEmpty()) {
            currentCategory = categoryExtra;
        }

        String searchQueryExtra = getIntent().getStringExtra("searchQuery");
        if (searchQueryExtra != null && !searchQueryExtra.trim().isEmpty()) {
            currentSearchQuery = searchQueryExtra.trim();
            currentCategory = "All";
        }

        android.widget.EditText edtListSearch = findViewById(R.id.edtListSearch);
        if (edtListSearch != null) {
            if (!currentSearchQuery.isEmpty()) {
                edtListSearch.setText(currentSearchQuery);
            }
            edtListSearch.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchQuery = s.toString().trim();
                    if (!currentSearchQuery.isEmpty() && tvCategoryTitle != null) {
                        tvCategoryTitle.setText("Tìm kiếm: '" + currentSearchQuery + "'");
                    }
                    loadProductsFromDatabase();
                }

                @Override
                public void afterTextChanged(android.text.Editable s) {}
            });
        }

        rvProducts = findViewById(R.id.rvProducts);
        if (rvProducts != null) {
            rvProducts.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
            rvProducts.setHasFixedSize(true);
            rvProducts.setItemViewCacheSize(20);
        }

        productList = new ArrayList<>();
        adapter = new ProductAdapter(productList, new ProductAdapter.OnProductClickListener() {
            @Override
            public void onClick(Product product) {
                Intent intent = new Intent(ProductListActivity.this, ProductDetailActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                intent.putExtra("productOldPrice", product.getOldPrice());
                intent.putExtra("productImage", product.getImageRes());
                intent.putExtra("productCategory", product.getCategory());
                intent.putExtra("productColor", product.getColor());
                
                // Laptop specs
                intent.putExtra("productCpu", product.getCpu());
                intent.putExtra("productRam", product.getRam());
                intent.putExtra("productSsd", product.getSsd());
                intent.putExtra("productGpu", product.getGpu());
                intent.putExtra("productScreen", product.getScreen());
                
                // Audio specs
                intent.putExtra("productType", product.getType());
                intent.putExtra("productConnection", product.getConnection());
                intent.putExtra("productBattery", product.getBattery());
                intent.putExtra("productAnc", product.getAnc());
                intent.putExtra("productMic", product.getMic());

                // Watch specs
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

            @Override
            public void onAddToCart(Product product) {
                addToCart(product.getName(), product.getPrice(), product.getImageRes());
            }
        });

        if (rvProducts != null) {
            rvProducts.setAdapter(adapter);
        }

        if (btnFilterWatch != null) btnFilterWatch.setOnClickListener(v -> selectCategory("watch"));
        if (btnFilterAudio != null) btnFilterAudio.setOnClickListener(v -> selectCategory("audio"));
        if (btnFilterLaptop != null) btnFilterLaptop.setOnClickListener(v -> selectCategory("Laptop"));
        if (btnFilterPhone != null) btnFilterPhone.setOnClickListener(v -> selectCategory("iPhone"));
        if (btnFilterAll != null) btnFilterAll.setOnClickListener(v -> selectCategory("All"));

        selectCategory(currentCategory);
    }

    private void selectCategory(String category) {
        currentCategory = category;
        android.widget.HorizontalScrollView layoutCategoryFilters = findViewById(R.id.layoutCategoryFilters);

        if ("watch".equalsIgnoreCase(category)) {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Danh mục Đồng hồ Watch");
        } else if ("audio".equalsIgnoreCase(category)) {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Danh mục Audio");
        } else if ("Laptop".equalsIgnoreCase(category)) {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Danh mục Laptop");
        } else if ("iPhone".equalsIgnoreCase(category)) {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Danh mục iPhone");
        } else if ("liked".equalsIgnoreCase(category)) {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.GONE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Sản phẩm yêu thích");
        } else {
            if (layoutCategoryFilters != null) layoutCategoryFilters.setVisibility(View.VISIBLE);
            if (tvCategoryTitle != null) tvCategoryTitle.setText("Tất cả sản phẩm");
        }

        updateFilterButtonsHighlight(category);
        loadProductsFromDatabase();
    }

    private void updateFilterButtonsHighlight(String category) {
        highlightButton(btnFilterWatch, "watch".equalsIgnoreCase(category));
        highlightButton(btnFilterAudio, "audio".equalsIgnoreCase(category));
        highlightButton(btnFilterLaptop, "Laptop".equalsIgnoreCase(category));
        highlightButton(btnFilterPhone, "iPhone".equalsIgnoreCase(category));
        highlightButton(btnFilterAll, "All".equalsIgnoreCase(category));
    }

    private void highlightButton(MaterialButton btn, boolean isSelected) {
        if (btn == null) return;
        if (isSelected) {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FF6B00")));
            btn.setTextColor(android.graphics.Color.WHITE);
            btn.setStrokeWidth(0);
        } else {
            btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFF1EC")));
            btn.setTextColor(android.graphics.Color.parseColor("#475569"));
            btn.setStrokeColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFE8D6")));
            btn.setStrokeWidth(2);
        }
    }

    private boolean matchesSearchQuery(Product p) {
        if (currentSearchQuery == null || currentSearchQuery.isEmpty()) return true;
        String q = currentSearchQuery.toLowerCase();
        String name = p.getName() != null ? p.getName().toLowerCase() : "";
        String cat = p.getCategory() != null ? p.getCategory().toLowerCase() : "";
        String desc = p.getDesc() != null ? p.getDesc().toLowerCase() : "";
        String shortDesc = p.getShortDesc() != null ? p.getShortDesc().toLowerCase() : "";

        if (q.contains("iphon") && (cat.contains("iphone") || name.contains("iphone"))) return true;
        if (q.contains("lap") && (cat.contains("laptop") || name.contains("laptop"))) return true;
        if (q.contains("aud") && (cat.contains("audio") || name.contains("tai nghe"))) return true;
        if (q.contains("wat") && (cat.contains("watch") || name.contains("đồng hồ"))) return true;

        return name.contains(q) || cat.contains(q) || desc.contains(q) || shortDesc.contains(q);
    }

    private void loadProductsFromDatabase() {
        if (mDatabase == null) {
            useFallbackProducts();
            return;
        }

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                productList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Product product = data.getValue(Product.class);
                    if (product != null) {
                        product.setId(data.getKey());
                        if ("liked".equalsIgnoreCase(currentCategory)) {
                            if (WishlistManager.isLiked(ProductListActivity.this, product.getId()) && matchesSearchQuery(product)) {
                                productList.add(product);
                            }
                        } else if (("All".equalsIgnoreCase(currentCategory) || currentCategory.equalsIgnoreCase(product.getCategory())) && matchesSearchQuery(product)) {
                            productList.add(product);
                        }
                    }
                }
                if (productList.isEmpty()) {
                    useFallbackProducts();
                } else if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                useFallbackProducts();
            }
        });
    }

    private void useFallbackProducts() {
        productList.clear();
        List<Product> all = getFallbackProducts();
        for (Product product : all) {
            if ("liked".equalsIgnoreCase(currentCategory)) {
                if (WishlistManager.isLiked(this, product.getId()) && matchesSearchQuery(product)) {
                    productList.add(product);
                }
            } else if (("All".equalsIgnoreCase(currentCategory) || currentCategory.equalsIgnoreCase(product.getCategory())) && matchesSearchQuery(product)) {
                productList.add(product);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    private List<Product> getFallbackProducts() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("ip1", "iPhone Pro Max - Graphite", "Graphite (Xám Đen)", "28.990.000đ", "32.990.000đ", "256GB", "Mới 100%", 15, "12 tháng", "Thiết kế mờ sang trọng", "iPhone Pro Max phiên bản Graphite...", "iPhone", R.drawable.iphonepromaxgraphite));
        list.add(new Product("ip2", "iPhone Pro Max - Bạc", "Bạc (Silver)", "29.490.000đ", "33.490.000đ", "256GB", "Mới 100%", 20, "12 tháng", "Màu Bạc tinh tế", "iPhone Pro Max màu Bạc...", "iPhone", R.drawable.iphonepromaxbac));
        list.add(new Product("lt1", "Laptop Gaming - Đen", "Đen", "25.990.000đ", "28.990.000đ", "", "Mới 100%", 10, "24 tháng", "Intel Core i7 | 16GB", "Laptop gaming...", "Laptop", R.drawable.laptopgamingden));
        list.add(new Product("au1", "Tai nghe chụp tai Premium - Đen", "Đen", "2.490.000đ", "2.990.000đ", "", "Mới 100%", 15, "12 tháng", "Over-ear | Pin 40h", "Tai nghe chụp tai...", "audio", R.drawable.tainghechuptaiden));
        list.add(new Product("wt1", "Watch Sport - Đen", "Đen", "3.490.000đ", "3.990.000đ", "", "Mới 100%", 15, "12 tháng", "Smart Watch | GPS", "Đồng hồ thể thao...", "watch", R.drawable.watchdenden));
        return list;
    }

    private void addToCart(String name, String price, int imageRes) {
        String userId;
        if (mAuth != null && mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
        } else {
            userId = WishlistManager.getUserKey(this);
        }
        String cartItemId = name != null ? name.replaceAll("[^a-zA-Z0-9]", "") : "cart_item";
        
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("price", price);
        item.put("quantity", 1);
        item.put("imageRes", imageRes);

        FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com")
            .getReference("Carts").child(userId).child(cartItemId).setValue(item)
            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show());
    }
}
