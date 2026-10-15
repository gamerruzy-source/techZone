package ph65249.fpoly.techzone;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private OnQuantityChangeListener listener;

    public interface OnQuantityChangeListener {
        void onIncrease(CartItem item);
        void onDecrease(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnQuantityChangeListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(item.getPrice());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        int imageDrawable = getProductDrawableByName(item.getName(), item.getImageRes());
        holder.imgProduct.setImageResource(imageDrawable);

        holder.btnPlus.setOnClickListener(v -> listener.onIncrease(item));
        holder.btnMinus.setOnClickListener(v -> listener.onDecrease(item));
    }

    public static int getProductDrawableByName(String productName, int fallbackRes) {
        if (productName == null) return fallbackRes != 0 ? fallbackRes : R.drawable.iphonepromaxbac;
        String name = productName.toLowerCase();

        // Audio products
        if (name.contains("thể thao") && name.contains("xanh")) return R.drawable.tainghethethaoxanh;
        if (name.contains("wireless") || (name.contains("tai nghe") && name.contains("trắng"))) return R.drawable.tainghetrang;
        if (name.contains("gaming") || name.contains("đỏ")) return R.drawable.tainghegamingdendo;
        if (name.contains("kem")) return R.drawable.tainghekem;
        if (name.contains("tai nghe") || name.contains("premium")) return R.drawable.tainghechuptaiden;

        // Watch products
        if (name.contains("classic") || name.contains("nâu")) return R.drawable.watchnaunau;
        if (name.contains("outdoor") || (name.contains("watch") && name.contains("cam"))) return R.drawable.watchcam;
        if (name.contains("fit") || (name.contains("watch") && name.contains("xanh"))) return R.drawable.watchxanh;
        if (name.contains("luxury") || name.contains("vàng kem")) return R.drawable.watchvangkem;
        if (name.contains("watch") || name.contains("sport")) return R.drawable.watchdenden;

        // iPhone products
        if (name.contains("graphite") || name.contains("xám đen")) return R.drawable.iphonepromaxgraphite;
        if (name.contains("bạc") || name.contains("silver")) return R.drawable.iphonepromaxbac;
        if (name.contains("xanh") && name.contains("titanium")) return R.drawable.iphonepromaxxanh;
        if (name.contains("vàng") || name.contains("gold")) return R.drawable.iphonepromaxvang;
        if (name.contains("đỏ") || name.contains("red")) return R.drawable.iphonpromaxdo;
        if (name.contains("iphone")) return R.drawable.iphonepromaxbac;

        // Laptop products
        if (name.contains("văn phòng") || (name.contains("laptop") && name.contains("xám"))) return R.drawable.laptopvanphongxam;
        if (name.contains("pastel") || (name.contains("laptop") && name.contains("trắng"))) return R.drawable.laptoppasteltrang;
        if (name.contains("slim") || (name.contains("laptop") && name.contains("bạc"))) return R.drawable.laptopslimbac;
        if (name.contains("gập") || (name.contains("laptop") && name.contains("xanh"))) return R.drawable.laptopgapxanh;
        if (name.contains("laptop") || name.contains("gaming")) return R.drawable.laptopgamingden;

        return fallbackRes != 0 ? fallbackRes : R.drawable.iphonepromaxbac;
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnMinus, btnPlus;
        TextView tvName, tvPrice, tvQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgCartItem);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvPrice = itemView.findViewById(R.id.tvCartItemPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }
}
