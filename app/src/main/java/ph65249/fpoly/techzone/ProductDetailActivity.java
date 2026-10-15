package ph65249.fpoly.techzone;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase, mReviewsRef;
    private String productId, productName, productPrice, productCategory;
    private int productImage;

    private ImageView imgProduct;
    private TextView tvName, tvPrice, tvOldPrice, tvSoldCount, tvStockCount;
    private TextView tvLabel1, tvLabel2, tvLabel3, tvLabel4, tvLabel5;
    private TextView tvCpu, tvRam, tvSsd, tvGpu, tvScreen, tvColor, tvStatus, tvWarranty, tvStock;
    private TextView tvDesc, tvSpecs;

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private RatingBar ratingBarInput;
    private EditText edtComment;

    private String selectedReviewImageUri = null;
    private ImageView imgReviewPreview;
    private View cardReviewPreview;

    private final ActivityResultLauncher<Intent> reviewImagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    Uri uri = result.getData().getData();
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception ignored) {}
                    selectedReviewImageUri = uri.toString();
                    if (imgReviewPreview != null && cardReviewPreview != null) {
                        imgReviewPreview.setImageURI(uri);
                        cardReviewPreview.setVisibility(View.VISIBLE);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        try {
            mAuth = FirebaseAuth.getInstance();
            mDatabase = FirebaseDatabase.getInstance("https://ph65039-fc619-default-rtdb.firebaseio.com").getReference();
        } catch (Exception e) {
            e.printStackTrace();
        }

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Binding UI elements
        imgProduct = findViewById(R.id.imgProductDetail);
        tvName = findViewById(R.id.tvDetailName);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvOldPrice = findViewById(R.id.tvDetailOldPrice);
        tvSoldCount = findViewById(R.id.tvDetailSoldCount);
        tvStockCount = findViewById(R.id.tvDetailStockCount);

        tvLabel1 = findViewById(R.id.tvLabel1);
        tvLabel2 = findViewById(R.id.tvLabel2);
        tvLabel3 = findViewById(R.id.tvLabel3);
        tvLabel4 = findViewById(R.id.tvLabel4);
        tvLabel5 = findViewById(R.id.tvLabel5);

        tvCpu = findViewById(R.id.tvDetailCpu);
        tvRam = findViewById(R.id.tvDetailRam);
        tvSsd = findViewById(R.id.tvDetailSsd);
        tvGpu = findViewById(R.id.tvDetailGpu);
        tvScreen = findViewById(R.id.tvDetailScreen);
        tvColor = findViewById(R.id.tvDetailColor);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvWarranty = findViewById(R.id.tvDetailWarranty);
        tvStock = findViewById(R.id.tvDetailStock);

        tvDesc = findViewById(R.id.tvDetailDesc);
        tvSpecs = findViewById(R.id.tvDetailSpecs);

        imgReviewPreview = findViewById(R.id.imgReviewPreview);
        cardReviewPreview = findViewById(R.id.cardReviewPreview);

        // Get Product ID and Extras
        productId = getIntent().getStringExtra("productId");
        productName = getIntent().getStringExtra("productName");
        productPrice = getIntent().getStringExtra("productPrice");
        productCategory = getIntent().getStringExtra("productCategory");
        productImage = getIntent().getIntExtra("productImage", R.drawable.watchdenden);

        if (productId != null && !productId.isEmpty() && mDatabase != null) {
            loadProductFromDatabase(productId);
        } else {
            bindFromIntentData();
        }

        // Reviews section
        String safeProductId = (productId != null && !productId.isEmpty()) ? productId 
                : (productName != null ? productName.replaceAll("[^a-zA-Z0-9]", "") : "default_prod");
        
        if (mDatabase != null) {
            mReviewsRef = mDatabase.child("Reviews").child(safeProductId);
        }

        ratingBarInput = findViewById(R.id.ratingBarInput);
        edtComment = findViewById(R.id.edtComment);
        rvReviews = findViewById(R.id.rvReviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));

        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);

        loadReviews(safeProductId);

        View btnSelectReviewImage = findViewById(R.id.btnSelectReviewImage);
        if (btnSelectReviewImage != null) {
            btnSelectReviewImage.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                reviewImagePickerLauncher.launch(intent);
            });
        }

        findViewById(R.id.btnSubmitReview).setOnClickListener(v -> submitReview());

        // Nút Thêm vào giỏ hàng
        findViewById(R.id.btnAddToCart).setOnClickListener(v -> addToCart());

        // Nút Mua ngay
        findViewById(R.id.btnBuyNow).setOnClickListener(v -> {
            addToCart();
            Intent intent = new Intent(ProductDetailActivity.this, CheckoutActivity.class);
            startActivity(intent);
        });

        setupFavoriteButton();
    }

    private void setupFavoriteButton() {
        View btnFavorite = findViewById(R.id.btnFavorite);
        ImageView imgFavoriteHeart = findViewById(R.id.imgFavoriteHeart);
        final String wishlistKey = (productId != null && !productId.isEmpty()) ? productId
                : (productName != null ? productName : "default_prod");

        if (imgFavoriteHeart != null) {
            boolean isLiked = WishlistManager.isLiked(this, wishlistKey);
            imgFavoriteHeart.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        }

        if (btnFavorite != null) {
            btnFavorite.setOnClickListener(v -> {
                boolean nowLiked = WishlistManager.toggleLike(ProductDetailActivity.this, wishlistKey);
                if (imgFavoriteHeart != null) {
                    imgFavoriteHeart.setImageResource(nowLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                }
                if (nowLiked) {
                    CustomToast.show(ProductDetailActivity.this, "❤️ Đã thêm vào Sản phẩm yêu thích!");
                } else {
                    CustomToast.show(ProductDetailActivity.this, "Đã bỏ yêu thích!");
                }
            });
        }
    }

    private void loadProductFromDatabase(String id) {
        if (mDatabase == null) {
            bindFromIntentData();
            return;
        }
        mDatabase.child("Products").child(id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Product product = snapshot.getValue(Product.class);
                if (product != null) {
                    product.setId(snapshot.getKey());
                    displayProduct(product);
                } else {
                    bindFromIntentData();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bindFromIntentData();
            }
        });
    }

    private void bindFromIntentData() {
        if (productName != null) tvName.setText(productName);
        if (productPrice != null) tvPrice.setText(productPrice);
        if (productImage != 0) imgProduct.setImageResource(productImage);

        String oldPrice = getIntent().getStringExtra("productOldPrice");
        if (oldPrice != null && !oldPrice.isEmpty()) {
            tvOldPrice.setVisibility(View.VISIBLE);
            tvOldPrice.setText(oldPrice);
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOldPrice.setVisibility(View.GONE);
        }

        // Sold count & Stock count
        String safeKey = (productId != null) ? productId : (productName != null ? productName : "default");
        int seed = Math.abs(safeKey.hashCode());
        int soldCount = 85 + (seed % 450);
        int stockCount = getIntent().getIntExtra("productStock", 12 + (seed % 20));

        if (tvSoldCount != null) tvSoldCount.setText("🔥 Đã bán: " + soldCount + " đơn hàng");
        if (tvStockCount != null) tvStockCount.setText("📦 Còn lại: " + stockCount + " sản phẩm");

        String category = getIntent().getStringExtra("productCategory");
        if ("watch".equalsIgnoreCase(category) || getIntent().getStringExtra("productWatchType") != null) {
            if (tvLabel1 != null) tvLabel1.setText("• Loại đồng hồ: ");
            if (tvLabel2 != null) tvLabel2.setText("• Màn hình: ");
            if (tvLabel3 != null) tvLabel3.setText("• Kết nối & GPS: ");
            if (tvLabel4 != null) tvLabel4.setText("• Theo dõi sức khỏe: ");
            if (tvLabel5 != null) tvLabel5.setText("• Chống nước / Pin: ");

            if (getIntent().getStringExtra("productWatchType") != null) tvCpu.setText(getIntent().getStringExtra("productWatchType"));
            if (getIntent().getStringExtra("productScreen") != null) tvRam.setText(getIntent().getStringExtra("productScreen"));
            
            String conn = getIntent().getStringExtra("productConnection");
            String gps = getIntent().getStringExtra("productGps");
            tvSsd.setText((conn != null ? conn : "") + (gps != null ? " | GPS: " + gps : ""));

            if (getIntent().getStringExtra("productHealth") != null) tvGpu.setText(getIntent().getStringExtra("productHealth"));

            String waterproof = getIntent().getStringExtra("productWaterproof");
            String battery = getIntent().getStringExtra("productBattery");
            tvScreen.setText((waterproof != null ? waterproof : "") + (battery != null ? " | Pin: " + battery : ""));
        } else if ("audio".equalsIgnoreCase(category) || getIntent().getStringExtra("productType") != null) {
            if (tvLabel1 != null) tvLabel1.setText("• Loại tai nghe: ");
            if (tvLabel2 != null) tvLabel2.setText("• Kết nối: ");
            if (tvLabel3 != null) tvLabel3.setText("• Thời lượng pin: ");
            if (tvLabel4 != null) tvLabel4.setText("• Chống ồn: ");
            if (tvLabel5 != null) tvLabel5.setText("• Micro: ");

            if (getIntent().getStringExtra("productType") != null) tvCpu.setText(getIntent().getStringExtra("productType"));
            if (getIntent().getStringExtra("productConnection") != null) tvRam.setText(getIntent().getStringExtra("productConnection"));
            if (getIntent().getStringExtra("productBattery") != null) tvSsd.setText(getIntent().getStringExtra("productBattery"));
            if (getIntent().getStringExtra("productAnc") != null) tvGpu.setText(getIntent().getStringExtra("productAnc"));
            if (getIntent().getStringExtra("productMic") != null) tvScreen.setText(getIntent().getStringExtra("productMic"));
        } else {
            if (tvLabel1 != null) tvLabel1.setText("• CPU: ");
            if (tvLabel2 != null) tvLabel2.setText("• RAM: ");
            if (tvLabel3 != null) tvLabel3.setText("• SSD: ");
            if (tvLabel4 != null) tvLabel4.setText("• GPU: ");
            if (tvLabel5 != null) tvLabel5.setText("• Màn hình: ");

            if (getIntent().getStringExtra("productCpu") != null) tvCpu.setText(getIntent().getStringExtra("productCpu"));
            if (getIntent().getStringExtra("productRam") != null) tvRam.setText(getIntent().getStringExtra("productRam"));
            if (getIntent().getStringExtra("productSsd") != null) tvSsd.setText(getIntent().getStringExtra("productSsd"));
            if (getIntent().getStringExtra("productGpu") != null) tvGpu.setText(getIntent().getStringExtra("productGpu"));
            if (getIntent().getStringExtra("productScreen") != null) tvScreen.setText(getIntent().getStringExtra("productScreen"));
        }

        if (getIntent().getStringExtra("productColor") != null) tvColor.setText(getIntent().getStringExtra("productColor"));
        if (getIntent().getStringExtra("productStatus") != null) tvStatus.setText(getIntent().getStringExtra("productStatus"));
        if (getIntent().getStringExtra("productWarranty") != null) tvWarranty.setText(getIntent().getStringExtra("productWarranty"));
        tvStock.setText(stockCount + " sản phẩm");

        if (getIntent().getStringExtra("productDesc") != null) tvDesc.setText(getIntent().getStringExtra("productDesc"));
        if (getIntent().getStringExtra("productSpecs") != null) tvSpecs.setText(getIntent().getStringExtra("productSpecs"));
    }

    private void displayProduct(Product p) {
        productName = p.getName();
        productPrice = p.getPrice();
        productCategory = p.getCategory();
        if (p.getImageRes() != 0) productImage = p.getImageRes();

        if (p.getName() != null) tvName.setText(p.getName());
        if (p.getPrice() != null) tvPrice.setText(p.getPrice());
        
        if (p.getOldPrice() != null && !p.getOldPrice().isEmpty()) {
            tvOldPrice.setVisibility(View.VISIBLE);
            tvOldPrice.setText(p.getOldPrice());
            tvOldPrice.setPaintFlags(tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvOldPrice.setVisibility(View.GONE);
        }

        if (p.getImageRes() != 0) imgProduct.setImageResource(p.getImageRes());

        int seed = Math.abs((p.getId() != null ? p.getId() : p.getName()).hashCode());
        int soldCount = 85 + (seed % 450);
        int stockCount = p.getStock() > 0 ? p.getStock() : (12 + (seed % 20));

        if (tvSoldCount != null) tvSoldCount.setText("Đã bán: " + soldCount + " đơn hàng");
        if (tvStockCount != null) tvStockCount.setText("Còn lại: " + stockCount + " sản phẩm");

        if ("watch".equalsIgnoreCase(p.getCategory()) || p.getWatchType() != null) {
            if (tvLabel1 != null) tvLabel1.setText("• Loại đồng hồ: ");
            if (tvLabel2 != null) tvLabel2.setText("• Màn hình: ");
            if (tvLabel3 != null) tvLabel3.setText("• Kết nối & GPS: ");
            if (tvLabel4 != null) tvLabel4.setText("• Theo dõi sức khỏe: ");
            if (tvLabel5 != null) tvLabel5.setText("• Chống nước / Pin: ");

            if (p.getWatchType() != null) tvCpu.setText(p.getWatchType());
            if (p.getScreen() != null) tvRam.setText(p.getScreen());
            
            String conn = p.getConnection();
            String gps = p.getGps();
            tvSsd.setText((conn != null ? conn : "") + (gps != null ? " | GPS: " + gps : ""));

            if (p.getHealth() != null) tvGpu.setText(p.getHealth());

            String waterproof = p.getWaterproof();
            String battery = p.getBattery();
            tvScreen.setText((waterproof != null ? waterproof : "") + (battery != null ? " | Pin: " + battery : ""));
        } else if ("audio".equalsIgnoreCase(p.getCategory()) || p.getType() != null) {
            if (tvLabel1 != null) tvLabel1.setText("• Loại tai nghe: ");
            if (tvLabel2 != null) tvLabel2.setText("• Kết nối: ");
            if (tvLabel3 != null) tvLabel3.setText("• Thời lượng pin: ");
            if (tvLabel4 != null) tvLabel4.setText("• Chống ồn: ");
            if (tvLabel5 != null) tvLabel5.setText("• Micro: ");

            if (p.getType() != null) tvCpu.setText(p.getType());
            if (p.getConnection() != null) tvRam.setText(p.getConnection());
            if (p.getBattery() != null) tvSsd.setText(p.getBattery());
            if (p.getAnc() != null) tvGpu.setText(p.getAnc());
            if (p.getMic() != null) tvScreen.setText(p.getMic());
        } else {
            if (tvLabel1 != null) tvLabel1.setText("• CPU: ");
            if (tvLabel2 != null) tvLabel2.setText("• RAM: ");
            if (tvLabel3 != null) tvLabel3.setText("• SSD: ");
            if (tvLabel4 != null) tvLabel4.setText("• GPU: ");
            if (tvLabel5 != null) tvLabel5.setText("• Màn hình: ");

            if (p.getCpu() != null) tvCpu.setText(p.getCpu());
            if (p.getRam() != null) tvRam.setText(p.getRam());
            if (p.getSsd() != null) tvSsd.setText(p.getSsd());
            if (p.getGpu() != null) tvGpu.setText(p.getGpu());
            if (p.getScreen() != null) tvScreen.setText(p.getScreen());
        }

        if (p.getColor() != null) tvColor.setText(p.getColor());
        if (p.getStatus() != null) tvStatus.setText(p.getStatus());
        if (p.getWarranty() != null) tvWarranty.setText(p.getWarranty());
        tvStock.setText(stockCount + " sản phẩm");

        if (p.getDesc() != null) tvDesc.setText(p.getDesc());
        if (p.getSpecs() != null) tvSpecs.setText(p.getSpecs());
    }

    private void updateRatingSummaryScore() {
        if (reviewList == null || reviewList.isEmpty()) return;
        float totalRating = 0f;
        for (Review r : reviewList) {
            totalRating += r.getRating();
        }
        float avg = totalRating / reviewList.size();
        TextView tvRatingScore = findViewById(R.id.tvRatingScore);
        TextView tvTotalReviewsCount = findViewById(R.id.tvTotalReviewsCount);

        if (tvRatingScore != null) {
            tvRatingScore.setText(String.format(java.util.Locale.US, "%.1f", avg));
        }
        if (tvTotalReviewsCount != null) {
            tvTotalReviewsCount.setText(reviewList.size() + " đánh giá");
        }
    }

    private void loadReviews(String safeId) {
        if (mReviewsRef == null) {
            reviewList.clear();
            reviewList.addAll(generateAiReviews(safeId, productCategory));
            reviewAdapter.notifyDataSetChanged();
            updateRatingSummaryScore();
            return;
        }

        mReviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review review = data.getValue(Review.class);
                    if (review != null) {
                        reviewList.add(review);
                    }
                }
                // Always append the AI generated reviews
                List<Review> aiReviews = generateAiReviews(safeId, productCategory);
                reviewList.addAll(aiReviews);
                reviewAdapter.notifyDataSetChanged();
                updateRatingSummaryScore();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                reviewList.clear();
                reviewList.addAll(generateAiReviews(safeId, productCategory));
                reviewAdapter.notifyDataSetChanged();
                updateRatingSummaryScore();
            }
        });
    }

    private List<Review> generateAiReviews(String safeId, String cat) {
        int seed = Math.abs((safeId != null ? safeId : "prod").hashCode());
        int[] countOptions = {10, 7, 4};
        int reviewCount = countOptions[seed % countOptions.length];

        String[] femaleNames = {"Ngọc Ánh", "Thu Hà", "Khánh Vy", "Mai Anh", "Hương Giang", "Thảo My", "Hải Yến", "Phương Linh", "Lan Anh", "Trà My", "Thanh Hằng", "Như Quỳnh", "Cẩm Tú", "Hồng Nhung", "Thùy Trang"};
        int[] femaleAvatars = {R.drawable.anhnu1, R.drawable.anhnu2, R.drawable.anhnu3, R.drawable.anhnu4, R.drawable.anhnu5};

        String[] maleNames = {"Minh Quân", "Bảo Lâm", "Gia Huy", "Anh Tuấn", "Đức Minh", "Quang Huy", "Đình Nam", "Thanh Tùng", "Tuấn Kiệt", "Hữu Phước", "Hoàng Nam", "Khánh Duy", "Văn Hải", "Trung Kiên", "Tiến Dũng"};
        int[] maleAvatars = {R.drawable.anhnam1, R.drawable.anhnam2, R.drawable.anhnam3, R.drawable.anhnam4, R.drawable.anhnam5};

        String[] iPhoneComments = {
            "Màn hình Super Retina XDR sáng mượt, vuốt 120Hz cực kỳ thích mắt. Camera chụp đêm xuất sắc.",
            "Màu Titanium sang trọng, cầm đầm tay. Khả năng quay video 4K sắc nét không giật lag.",
            "Dung lượng pin khá trâu, dùng liên tục cả ngày làm việc vẫn còn 25%.",
            "Thiết kế viền mỏng đẹp, tuy nhiên máy hơi ấm nhẹ khi vừa sạc vừa sử dụng.",
            "Nút Action Button tuỳ chỉnh tiện lợi, mở camera nhanh trong 1 giây.",
            "Chất lượng hoàn thiện tuyệt vời, loa ngoài to rõ và âm bass trầm ấm.",
            "Giao hàng nguyên seal rất nhanh, đầy đủ hóa đơn bảo hành chính hãng.",
            "Màn hình Always-On Display hiển thị thông báo rất tiện lợi.",
            "Dùng khá êm nhưng củ sạc phải mua riêng nên hơi tiếc.",
            "Máy tốt nhưng giá hơi cao một chút so với trải nghiệm ban đầu."
        };

        String[] laptopComments = {
            "Máy khởi động siêu nhanh, mở 30 tab Chrome mượt mà không hề giật lag.",
            "Màn hình 2K sắc nét, độ phủ màu chuẩn làm đồ họa Photoshop rất chuẩn màu.",
            "Bàn phím gõ êm, hành trình phím sâu, gõ văn bản lâu không mỏi tay.",
            "Tản nhiệt hoạt động hiệu quả, chiến game ổn định ở mức FPS cao.",
            "Thiết kế mỏng nhẹ sang trọng, dễ dàng bỏ balo mang đi học và đi làm.",
            "Thời lượng pin sử dụng thực tế tầm 6-7 tiếng công việc văn phòng.",
            "Âm thanh loa kép khá hay, micro lọc ồn tốt cho các buổi họp Zoom.",
            "Hàng chính hãng đóng gói cẩn thận, bảo hành 24 tháng yên tâm.",
            "Máy chạy hơi nóng khi rendering video nặng.",
            "Cổng kết nối hơi ít, phải dùng thêm Hub chuyển đổi."
        };

        String[] audioComments = {
            "Chất âm tuyệt vời! Bass đập sâu lực, dải mid trong trẻo nghe nhạc Pop/EDM đỉnh.",
            "Chống ồn chủ động ANC hiệu quả rõ rệt, đeo lên là cách ly tạp âm xung quanh.",
            "Thời lượng pin cực trâu! Nghe liên tục cả tuần mới phải sạc lại một lần.",
            "Đệm tai bọc da mềm mại, đeo liên tục 3-4 tiếng không bị đau vành tai.",
            "Micro đàm thoại HD rõ ràng, gọi điện ngoài đường gió to vẫn nghe tốt.",
            "Kết nối Bluetooth 5.3 nhanh và ổn định, xem video không có độ trễ.",
            "Hộp tai nghe thiết kế đẹp, vỏ nhám mịn chống bám vân tay.",
            "Sản phẩm chất lượng trong tầm giá, nhân viên hỗ trợ nhiệt tình.",
            "Âm treble đôi lúc hơi gắt khi mở âm lượng tối đa.",
            "Thiết kế hơi to so với vòng tai nhỏ của nữ."
        };

        String[] watchComments = {
            "Màn hình AMOLED hiển thị rất nét ngay cả dưới ánh nắng mặt trời gay gắt.",
            "Đo nhịp tim, SpO2 và theo dõi giấc ngủ chính xác, báo cáo chi tiết trên app.",
            "GPS độc lập bắt sóng nhanh, chạy bộ không cần mang theo điện thoại.",
            "Dây đeo silicon/da mềm mại, thoáng khí không gây kích ứng da.",
            "Thông báo cuộc gọi, Zalo, Messenger hiển thị tiếng Việt đầy đủ không lỗi font.",
            "Nhiều chế độ luyện tập thể thao, đếm bước chân và calo rất chuẩn.",
            "Kháng nước 5ATM yên tâm bơi lội và đi mưa lớn.",
            "Thiết kế mặt đồng hồ thời trang, dễ phối đồ công sở lẫn thể thao.",
            "Thời lượng pin dùng được tầm 2 ngày rưỡi hơi ngắn so với mong đợi.",
            "Mặt kính chống xước trung bình, nên dán thêm dán bảo vệ."
        };

        String[] comments;
        if ("iPhone".equalsIgnoreCase(cat)) comments = iPhoneComments;
        else if ("Laptop".equalsIgnoreCase(cat)) comments = laptopComments;
        else if ("watch".equalsIgnoreCase(cat)) comments = watchComments;
        else comments = audioComments;

        float[] ratings = {5.0f, 5.0f, 5.0f, 4.5f, 4.0f, 5.0f, 4.0f, 3.5f, 3.0f, 2.0f};
        String[] dates = {"2 giờ trước", "Hôm qua", "2 ngày trước", "3 ngày trước", "5 ngày trước", "1 tuần trước", "2 tuần trước", "3 tuần trước", "1 tháng trước", "2 tháng trước"};

        List<Review> list = new ArrayList<>();
        for (int i = 0; i < reviewCount; i++) {
            boolean isFemale = ((seed + i) % 2 == 0);
            String author = isFemale ? femaleNames[(seed + i) % femaleNames.length] : maleNames[(seed + i) % maleNames.length];
            int avatar = isFemale ? femaleAvatars[(seed + i) % femaleAvatars.length] : maleAvatars[(seed + i) % maleAvatars.length];
            float rating = ratings[i % ratings.length];
            String text = comments[i % comments.length];
            String dateText = "Đã mua hàng chính hãng • " + dates[i % dates.length];

            list.add(new Review("ai_" + safeId + "_" + i, author, text, rating, null, avatar, dateText));
        }
        return list;
    }

    private void submitReview() {
        String comment = edtComment.getText().toString().trim();
        float rating = ratingBarInput.getRating();

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập nội dung đánh giá!", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("TechZonePref", MODE_PRIVATE);
        String name = pref.getString("saved_name", "");
        String avatarName = pref.getString("saved_avatar", "anhnam1");

        if (name == null || name.trim().isEmpty()) {
            if (mAuth != null && mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                name = mAuth.getCurrentUser().getEmail().split("@")[0];
            } else {
                name = "Khách hàng TechZone";
            }
        }
        final String authorName = name;

        int userAvatarRes = getResources().getIdentifier(avatarName, "drawable", getPackageName());
        if (userAvatarRes == 0) userAvatarRes = R.drawable.anhnam1;

        Review newReview = new Review("user_" + System.currentTimeMillis(), authorName, comment, rating, selectedReviewImageUri, userAvatarRes, "Vừa đánh giá • Vừa xong");
        reviewList.add(0, newReview);
        reviewAdapter.notifyDataSetChanged();

        if (mReviewsRef != null) {
            String reviewId = mReviewsRef.push().getKey();
            Map<String, Object> review = new HashMap<>();
            review.put("id", reviewId);
            review.put("userName", authorName);
            review.put("comment", comment);
            review.put("rating", rating);
            if (selectedReviewImageUri != null) {
                review.put("imageUrl", selectedReviewImageUri);
            }
            if (reviewId != null) {
                mReviewsRef.child(reviewId).setValue(review);
            }
        }

        Toast.makeText(this, "Cảm ơn " + authorName + " đã đánh giá!", Toast.LENGTH_SHORT).show();
        edtComment.setText("");
        selectedReviewImageUri = null;
        if (cardReviewPreview != null) cardReviewPreview.setVisibility(View.GONE);
    }

    private void addToCart() {
        String userId = (mAuth != null && mAuth.getCurrentUser() != null) ? mAuth.getCurrentUser().getUid() : WishlistManager.getUserKey(this);
        String cartItemId = productName != null ? productName.replaceAll("[^a-zA-Z0-9]", "") : "item_cart"; 

        Map<String, Object> item = new HashMap<>();
        item.put("name", productName);
        item.put("price", productPrice);
        item.put("quantity", 1);
        item.put("imageRes", productImage);

        if (mDatabase != null) {
            mDatabase.child("Carts").child(userId).child(cartItemId).setValue(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        }
    }
}
