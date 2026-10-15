package ph65249.fpoly.techzone;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private List<Product> productList;
    private OnFlashSaleClickListener listener;

    public interface OnFlashSaleClickListener {
        void onClick(Product product);
        void onAddToCart(Product product);
    }

    public FlashSaleAdapter(List<Product> productList, OnFlashSaleClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flash_sale_card, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());

        if (product.getOldPrice() != null && !product.getOldPrice().isEmpty()) {
            holder.tvOldPrice.setVisibility(View.VISIBLE);
            holder.tvOldPrice.setText(product.getOldPrice());
            holder.tvOldPrice.setPaintFlags(holder.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvOldPrice.setVisibility(View.GONE);
        }

        if (product.getImageRes() != 0) {
            holder.imgProduct.setImageResource(product.getImageRes());
        }

        String key = (product.getId() != null) ? product.getId() : product.getName();
        int seed = Math.abs((key != null ? key : "fs").hashCode());

        int discountPercent = 15 + (seed % 25); // e.g. 15%, 25%, 35%, 40%
        if (holder.tvDiscountPercent != null) {
            holder.tvDiscountPercent.setText("Giảm " + discountPercent + "%");
        }

        int soldCount = 5 + (seed % 5); // e.g. 5, 7, 8, 9 out of 10
        int progress = (soldCount * 100) / 10;
        if (holder.pbStock != null) {
            holder.pbStock.setProgress(progress);
        }
        if (holder.tvStockProgressText != null) {
            holder.tvStockProgressText.setText("Đã bán " + soldCount + "/10 chiếc");
        }

        View.OnClickListener cardClickListener = v -> {
            if (listener != null) {
                listener.onClick(product);
            }
        };

        holder.itemView.setOnClickListener(cardClickListener);
        if (holder.cardFlashSale != null) {
            holder.cardFlashSale.setOnClickListener(cardClickListener);
        }
        if (holder.imgProduct != null) {
            holder.imgProduct.setOnClickListener(cardClickListener);
        }
        if (holder.tvName != null) {
            holder.tvName.setOnClickListener(cardClickListener);
        }

        if (holder.btnAdd != null) {
            holder.btnAdd.setOnClickListener(v -> listener.onAddToCart(product));
        }
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        View cardFlashSale;
        ImageView imgProduct;
        View btnAdd;
        TextView tvName, tvPrice, tvOldPrice, tvDiscountPercent, tvStockProgressText;
        ProgressBar pbStock;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFlashSale = itemView.findViewById(R.id.cardFlashSale);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvOldPrice = itemView.findViewById(R.id.tvProductOldPrice);
            tvDiscountPercent = itemView.findViewById(R.id.tvDiscountPercent);
            tvStockProgressText = itemView.findViewById(R.id.tvStockProgressText);
            pbStock = itemView.findViewById(R.id.pbFlashSaleStock);
        }
    }
}
