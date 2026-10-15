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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private AdminProductAdapter adapter;
    private List<Product> productList;
    private DatabaseReference mDatabase;

    private TextView tvTotalStock, tvTotalProductsCount, tvTotalSoldCount, tvRatingSummaryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_products);

        try {
            mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference("Products");
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvTotalStock = findViewById(R.id.tvTotalStock);
        tvTotalProductsCount = findViewById(R.id.tvTotalProductsCount);
        tvTotalSoldCount = findViewById(R.id.tvTotalSoldCount);
        tvRatingSummaryText = findViewById(R.id.tvRatingSummaryText);

        rvProducts = findViewById(R.id.rvAdminProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        
        productList = new ArrayList<>();
        adapter = new AdminProductAdapter(productList, new AdminProductAdapter.OnProductActionListener() {
            @Override
            public void onEdit(Product product) {
                Intent intent = new Intent(AdminProductsActivity.this, AddProductActivity.class);
                intent.putExtra("productId", product.getId());
                intent.putExtra("productName", product.getName());
                intent.putExtra("productPrice", product.getPrice());
                startActivity(intent);
            }

            @Override
            public void onDelete(Product product) {
                if (mDatabase != null && product.getId() != null) {
                    mDatabase.child(product.getId()).removeValue()
                        .addOnSuccessListener(aVoid -> Toast.makeText(AdminProductsActivity.this, "Đã xóa " + product.getName() + "!", Toast.LENGTH_SHORT).show());
                } else {
                    productList.remove(product);
                    adapter.notifyDataSetChanged();
                    updateDashboardMetrics();
                    Toast.makeText(AdminProductsActivity.this, "Đã xóa!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        rvProducts.setAdapter(adapter);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        View btnAddProductHeader = findViewById(R.id.btnAddProductHeader);
        if (btnAddProductHeader != null) {
            btnAddProductHeader.setOnClickListener(v -> startActivity(new Intent(AdminProductsActivity.this, AddProductActivity.class)));
        }

        loadProducts();
    }

    private void loadProducts() {
        if (mDatabase == null) {
            useFallbackData();
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
                        productList.add(product);
                    }
                }
                if (productList.isEmpty()) {
                    useFallbackData();
                } else {
                    adapter.notifyDataSetChanged();
                    updateDashboardMetrics();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                useFallbackData();
            }
        });
    }

    private void useFallbackData() {
        productList.clear();
        productList.addAll(getFallbackProducts());
        adapter.notifyDataSetChanged();
        updateDashboardMetrics();
    }

    private void updateDashboardMetrics() {
        int totalProducts = productList.size();
        int totalStock = 0;
        int totalSold = 0;

        for (Product p : productList) {
            String key = (p.getId() != null) ? p.getId() : p.getName();
            int seed = Math.abs((key != null ? key : "prod").hashCode());
            int stock = p.getStock() > 0 ? p.getStock() : (12 + (seed % 20));
            int sold = 85 + (seed % 450);

            totalStock += stock;
            totalSold += sold;
        }

        if (tvTotalStock != null) tvTotalStock.setText(totalStock + " chiếc");
        if (tvTotalProductsCount != null) tvTotalProductsCount.setText(totalProducts + " mẫu sản phẩm");
        if (tvTotalSoldCount != null) tvTotalSoldCount.setText(String.format(java.util.Locale.US, "%,d đơn", totalSold).replace(",", "."));
        if (tvRatingSummaryText != null) tvRatingSummaryText.setText("Điểm TB: 4.8 ★ | " + (totalProducts * 7) + " lượt nhận xét");
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
}
