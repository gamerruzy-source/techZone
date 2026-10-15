package ph65249.fpoly.techzone;

public class Product {
    private String id;
    private String name;
    private String color;
    private String price;
    private String oldPrice;
    private String capacity;
    private String status;
    private int stock;
    private String warranty;
    private String shortDesc;
    private String desc;
    private String category;
    private int imageRes;

    // Additional specs for Laptop & Devices
    private String cpu;
    private String ram;
    private String ssd;
    private String gpu;
    private String screen;
    private String specs;

    // Audio specific specs
    private String type;
    private String connection;
    private String battery;
    private String anc;
    private String mic;

    // Watch specific specs
    private String watchType;
    private String gps;
    private String health;
    private String waterproof;

    public Product() {}

    public Product(String id, String name, String color, String price, String oldPrice, String capacity,
                   String status, int stock, String warranty, String shortDesc, String desc,
                   String category, int imageRes) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.price = price;
        this.oldPrice = oldPrice;
        this.capacity = capacity;
        this.status = status;
        this.stock = stock;
        this.warranty = warranty;
        this.shortDesc = shortDesc;
        this.desc = desc;
        this.category = category;
        this.imageRes = imageRes;
    }

    public Product(String id, String name, String color, String price, String oldPrice, String capacity,
                   String status, int stock, String warranty, String shortDesc, String desc,
                   String category, int imageRes, String cpu, String ram, String ssd, String gpu, String screen, String specs) {
        this(id, name, color, price, oldPrice, capacity, status, stock, warranty, shortDesc, desc, category, imageRes);
        this.cpu = cpu;
        this.ram = ram;
        this.ssd = ssd;
        this.gpu = gpu;
        this.screen = screen;
        this.specs = specs;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }

    public String getOldPrice() { return oldPrice; }
    public void setOldPrice(String oldPrice) { this.oldPrice = oldPrice; }

    public String getCapacity() { return capacity; }
    public void setCapacity(String capacity) { this.capacity = capacity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getWarranty() { return warranty; }
    public void setWarranty(String warranty) { this.warranty = warranty; }

    public String getShortDesc() { return shortDesc; }
    public void setShortDesc(String shortDesc) { this.shortDesc = shortDesc; }

    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getSsd() { return ssd; }
    public void setSsd(String ssd) { this.ssd = ssd; }

    public String getGpu() { return gpu; }
    public void setGpu(String gpu) { this.gpu = gpu; }

    public String getScreen() { return screen; }
    public void setScreen(String screen) { this.screen = screen; }

    public String getSpecs() { return specs; }
    public void setSpecs(String specs) { this.specs = specs; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getConnection() { return connection; }
    public void setConnection(String connection) { this.connection = connection; }

    public String getBattery() { return battery; }
    public void setBattery(String battery) { this.battery = battery; }

    public String getAnc() { return anc; }
    public void setAnc(String anc) { this.anc = anc; }

    public String getMic() { return mic; }
    public void setMic(String mic) { this.mic = mic; }

    public String getWatchType() { return watchType; }
    public void setWatchType(String watchType) { this.watchType = watchType; }

    public String getGps() { return gps; }
    public void setGps(String gps) { this.gps = gps; }

    public String getHealth() { return health; }
    public void setHealth(String health) { this.health = health; }

    public String getWaterproof() { return waterproof; }
    public void setWaterproof(String waterproof) { this.waterproof = waterproof; }
}
