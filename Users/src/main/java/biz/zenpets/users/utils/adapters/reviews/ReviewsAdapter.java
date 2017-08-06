package biz.zenpets.users.utils.adapters.reviews;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.reviews.ReviewsData;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<ReviewsData> arrReviews;

    public ReviewsAdapter(Activity activity, ArrayList<ReviewsData> arrReviews) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrReviews = arrReviews;
    }

    @Override
    public int getItemCount() {
        return arrReviews.size();
    }

    @Override
    public void onBindViewHolder(ReviewsVH holder, final int position) {
        ReviewsData data = arrReviews.get(position);

        /** SET THE VISIT REASON **/
        if (data.getVisitReason() != null)  {
            holder.txtVisitReason.setText(data.getVisitReason());
        }

        /** SET THE VISIT EXPERIENCE **/
        if (data.getDoctorExperience() != null) {
            holder.txtVisitExperience.setText(data.getDoctorExperience());
        }

        /** SET THE USER NAME **/
        if (data.getUserName() != null) {
            holder.txtUserName.setText(data.getUserName());
        }

        /** SET THE TIMESTAMP **/
        if (data.getReviewTimestamp() != null)  {
            holder.txtPostedAgo.setText(data.getReviewTimestamp());
        }

        /** SET THE RECOMMEND STATUS **/
        String strRecommendStatus = data.getRecommendStatus();
        if (strRecommendStatus.equalsIgnoreCase("Yes")) {
            holder.imgvwLikeStatus.setIcon("faw-thumbs-o-up");
            holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_blue_dark));
        } else if (strRecommendStatus.equalsIgnoreCase("No"))   {
            holder.imgvwLikeStatus.setIcon("faw-thumbs-o-down");
            holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
        }
    }

    @Override
    public ReviewsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctors_details_reviews_item, parent, false);

        return new ReviewsVH(itemView);
    }

    class ReviewsVH extends RecyclerView.ViewHolder	{
        final AppCompatTextView txtVisitReason;
        final AppCompatTextView txtVisitExperience;
        final IconicsImageView imgvwLikeStatus;
        final AppCompatTextView txtUserName;
        final AppCompatTextView txtPostedAgo;

        ReviewsVH(View v) {
            super(v);
            txtVisitReason = (AppCompatTextView) v.findViewById(R.id.txtVisitReason);
            txtVisitExperience = (AppCompatTextView) v.findViewById(R.id.txtVisitExperience);
            imgvwLikeStatus = (IconicsImageView) v.findViewById(R.id.imgvwLikeStatus);
            txtUserName = (AppCompatTextView) v.findViewById(R.id.txtUserName);
            txtPostedAgo = (AppCompatTextView) v.findViewById(R.id.txtPostedAgo);
        }
    }
}