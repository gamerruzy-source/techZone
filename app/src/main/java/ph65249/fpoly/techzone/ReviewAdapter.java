package ph65249.fpoly.techzone;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);
        holder.tvUser.setText(review.getUserName() != null ? review.getUserName() : "Khách hàng TechZone");
        holder.tvComment.setText(review.getComment());
        holder.ratingBar.setRating(review.getRating());

        if (review.getDateText() != null && !review.getDateText().isEmpty()) {
            holder.tvDate.setText(review.getDateText());
        } else {
            holder.tvDate.setText("Đã mua hàng xác thực");
        }

        if (review.getAvatarRes() != 0) {
            holder.imgAvatar.setImageResource(review.getAvatarRes());
        } else {
            holder.imgAvatar.setImageResource(R.drawable.anhnam1);
        }

        if (review.getImageUrl() != null && !review.getImageUrl().isEmpty()) {
            holder.cardPhoto.setVisibility(View.VISIBLE);
            try {
                int drawableId = Integer.parseInt(review.getImageUrl());
                holder.imgPhoto.setImageResource(drawableId);
            } catch (NumberFormatException e) {
                try {
                    holder.imgPhoto.setImageURI(Uri.parse(review.getImageUrl()));
                } catch (Exception ex) {
                    holder.cardPhoto.setVisibility(View.GONE);
                }
            }
        } else {
            holder.cardPhoto.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUser, tvDate, tvComment;
        RatingBar ratingBar;
        View cardPhoto;
        ImageView imgPhoto;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgReviewAvatar);
            tvUser = itemView.findViewById(R.id.tvReviewUser);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            tvComment = itemView.findViewById(R.id.tvReviewComment);
            ratingBar = itemView.findViewById(R.id.ratingBarItem);
            cardPhoto = itemView.findViewById(R.id.cardReviewPhoto);
            imgPhoto = itemView.findViewById(R.id.imgReviewPhoto);
        }
    }
}
