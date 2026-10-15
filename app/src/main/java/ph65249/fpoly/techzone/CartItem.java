package ph65249.fpoly.techzone;

public class CartItem {
    private String id;
    private String name;
    private String price;
    private int quantity;
    private int imageRes;

    public CartItem() {
    }

    public CartItem(String id, String name, String price, int quantity, int imageRes) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageRes = imageRes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }
}
