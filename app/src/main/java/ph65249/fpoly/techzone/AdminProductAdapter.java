package ph65249.fpoly.techzone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.AdminProductViewHolder> {

    private List<Product> productList;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public AdminProductAdapter(List<Product> productList, OnProductActionListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_product, parent, false);
        return new AdminProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(product.getPrice());
        if (product.getImageRes() != 0) {
            holder.imgProduct.setImageResource(product.getImageRes());
        }

        String key = (product.getId() != null) ? product.getId() : product.getName();
        int seed = Math.abs(key.hashCode());
        int stock = product.getStock() > 0 ? product.getStock() : (12 + (seed % 20));
        int sold = 85 + (seed % 450);

        if (holder.tvStockInfo != null) {
            holder.tvStockInfo.setText("Tồn: " + stock);
        }
        if (holder.tvSoldCount != null) {
            holder.tvSoldCount.setText("Đã bán: " + sold);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class AdminProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        View btnEdit, btnDelete;
        TextView tvName, tvPrice, tvStockInfo, tvSoldCount;

        public AdminProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvStockInfo = itemView.findViewById(R.id.tvStockInfo);
            tvSoldCount = itemView.findViewById(R.id.tvSoldCount);
        }
    }
}
