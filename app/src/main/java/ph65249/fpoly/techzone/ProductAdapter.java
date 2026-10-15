package ph65249.fpoly.techzone;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onClick(Product product);
        void onAddToCart(Product product);
    }

    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());

        if (product.getOldPrice() != null && !product.getOldPrice().isEmpty()) {
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText(product.getOldPrice());
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        String desc = product.getShortDesc();
        if (desc == null || desc.isEmpty()) {
            desc = product.getDesc();
        }
        if (holder.tvDesc != null) {
            holder.tvDesc.setText(desc);
        }
        
        if (product.getImageRes() != 0) {
            holder.imgProduct.setImageResource(product.getImageRes());
        }

        // Bind icons: 03_ngoi_sao
        if (holder.imgStarIcon != null) holder.imgStarIcon.setImageResource(R.drawable.ic_ngoi_sao);

        // Bind Brand, Discount %, Rating Score, Member Chip based on product ID / Category
        String key = (product.getId() != null) ? product.getId() : product.getName();
        int seed = Math.abs(key.hashCode());

        String[] brands = {"Apple", "Asus", "Sony", "Garmin", "WiWU", "Samsung", "Lenovo", "Marshall"};
        String brand = brands[seed % brands.length];
        if ("iPhone".equalsIgnoreCase(product.getCategory())) brand = "Apple";
        else if ("Laptop".equalsIgnoreCase(product.getCategory())) brand = (seed % 2 == 0) ? "Asus" : "Lenovo";
        else if ("audio".equalsIgnoreCase(product.getCategory())) brand = (seed % 2 == 0) ? "Sony" : "Marshall";
        else if ("watch".equalsIgnoreCase(product.getCategory())) brand = (seed % 2 == 0) ? "Garmin" : "Apple";

        if (holder.tvBrandName != null) holder.tvBrandName.setText(brand);

        int discountPercent = 8 + (seed % 18); // e.g. 10%, 12%, 15%
        if (holder.tvDiscountPercent != null) holder.tvDiscountPercent.setText("Giảm " + discountPercent + "%");

        String[] ratings = {"5.0", "4.9", "4.8", "4.7", "4.5", "4.2"};
        String ratingScore = ratings[seed % ratings.length];
        if (holder.tvRatingScore != null) holder.tvRatingScore.setText(ratingScore);

        if (holder.tvMemberDiscount != null) {
            int memberSave = 50 + ((seed % 15) * 10);
            holder.tvMemberDiscount.setText("Member giảm " + memberSave + "K");
        }

        // Selection / Hover / Touch Animation Effects
        setupCardTouchAndHoverEffects(holder);

        // Heart Wishlist State (Trái tim rỗng ic_heart_outline / Trái tim đầy đủ ic_heart_filled)
        if (holder.btnWishlistHeart != null) {
            boolean isLiked = WishlistManager.isLiked(context, key);
            if (isLiked) {
                holder.btnWishlistHeart.setImageResource(R.drawable.ic_heart_filled);
                holder.btnWishlistHeart.clearColorFilter();
            } else {
                holder.btnWishlistHeart.setImageResource(R.drawable.ic_heart_outline);
                holder.btnWishlistHeart.clearColorFilter();
            }

            holder.btnWishlistHeart.setOnClickListener(v -> {
                boolean nowLiked = WishlistManager.toggleLike(context, key);
                if (nowLiked) {
                    holder.btnWishlistHeart.setImageResource(R.drawable.ic_heart_filled);
                    holder.btnWishlistHeart.clearColorFilter();
                    CustomToast.show(context, "❤️ Đã thêm vào Sản phẩm yêu thích!");
                } else {
                    holder.btnWishlistHeart.setImageResource(R.drawable.ic_heart_outline);
                    holder.btnWishlistHeart.clearColorFilter();
                    CustomToast.show(context, "Đã bỏ yêu thích!");
                }
            });
        }

        View.OnClickListener cardClickListener = v -> {
            if (listener != null) {
                listener.onClick(product);
            }
        };

        holder.itemView.setOnClickListener(cardClickListener);
        if (holder.cardProduct != null) holder.cardProduct.setOnClickListener(cardClickListener);
        if (holder.imgProduct != null) holder.imgProduct.setOnClickListener(cardClickListener);
        if (holder.tvName != null) holder.tvName.setOnClickListener(cardClickListener);
        if (holder.btnDetail != null) holder.btnDetail.setOnClickListener(cardClickListener);
        if (holder.btnAdd != null) holder.btnAdd.setOnClickListener(v -> listener.onAddToCart(product));
    }

    private void setupCardTouchAndHoverEffects(ProductViewHolder holder) {
        if (holder.cardProduct == null) return;

        holder.cardProduct.setOnHoverListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    animateCardSelection(holder.cardProduct, true);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    animateCardSelection(holder.cardProduct, false);
                    break;
            }
            return false;
        });

        holder.cardProduct.setOnFocusChangeListener((v, hasFocus) -> animateCardSelection(holder.cardProduct, hasFocus));

        holder.cardProduct.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    animateCardSelection(holder.cardProduct, true);
                    break;
                case MotionEvent.ACTION_UP:
                    animateCardSelection(holder.cardProduct, false);
                    v.performClick();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    animateCardSelection(holder.cardProduct, false);
                    break;
            }
            return false;
        });
    }

    private void animateCardSelection(MaterialCardView card, boolean isSelected) {
        if (card == null) return;
        if (isSelected) {
            card.animate()
                .scaleX(1.03f)
                .scaleY(1.03f)
                .setDuration(100)
                .start();
            card.setStrokeColor(Color.parseColor("#FF6B00"));
            card.setStrokeWidth(2);
            card.setCardElevation(8f);
        } else {
            card.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100)
                .start();
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setStrokeWidth(1);
            card.setCardElevation(3f);
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardProduct;
        ImageView imgProduct, imgDeliveryIcon, imgStarIcon, btnWishlistHeart;
        View btnAdd, btnDetail;
        TextView tvName, tvPrice, tvOldPrice, tvDesc, tvBrandName, tvDiscountPercent, tvRatingScore, tvMemberDiscount;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            cardProduct = itemView.findViewById(R.id.cardProduct);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgDeliveryIcon = itemView.findViewById(R.id.imgDeliveryIcon);
            imgStarIcon = itemView.findViewById(R.id.imgStarIcon);
            btnWishlistHeart = itemView.findViewById(R.id.btnWishlistHeart);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOldPrice = itemView.findViewById(R.id.tvProductOldPrice);
            tvDesc = itemView.findViewById(R.id.tvProductDesc);
            tvBrandName = itemView.findViewById(R.id.tvBrandName);
            tvDiscountPercent = itemView.findViewById(R.id.tvDiscountPercent);
            tvRatingScore = itemView.findViewById(R.id.tvRatingScore);
            tvMemberDiscount = itemView.findViewById(R.id.tvMemberDiscount);
        }
    }
}
