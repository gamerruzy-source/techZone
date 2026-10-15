package ph65249.fpoly.techzone;

public class Review {
    private String id;
    private String userName;
    private String comment;
    private float rating;
    private String imageUrl;
    private int avatarRes;
    private String dateText;

    public Review() {}

    public Review(String id, String userName, String comment, float rating) {
        this.id = id;
        this.userName = userName;
        this.comment = comment;
        this.rating = rating;
    }

    public Review(String id, String userName, String comment, float rating, String imageUrl) {
        this(id, userName, comment, rating);
        this.imageUrl = imageUrl;
    }

    public Review(String id, String userName, String comment, float rating, String imageUrl, int avatarRes, String dateText) {
        this(id, userName, comment, rating, imageUrl);
        this.avatarRes = avatarRes;
        this.dateText = dateText;
    }

    public String getId() { return id; }
    public String getUserName() { return userName; }
    public String getComment() { return comment; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getAvatarRes() { return avatarRes; }
    public void setAvatarRes(int avatarRes) { this.avatarRes = avatarRes; }
    public String getDateText() { return dateText; }
    public void setDateText(String dateText) { this.dateText = dateText; }
}
