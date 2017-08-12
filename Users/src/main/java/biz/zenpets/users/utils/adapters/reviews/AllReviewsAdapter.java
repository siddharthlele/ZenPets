package biz.zenpets.users.utils.adapters.reviews;

/*
public class AllReviewsAdapter extends RecyclerView.Adapter<AllReviewsAdapter.ReviewsVH> {

    */
/***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****//*

    private final Activity activity;

    */
/***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****//*

    private final ArrayList<ReviewsData> arrReviews;

    public AllReviewsAdapter(Activity activity, ArrayList<ReviewsData> arrReviews) {

        */
/* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE *//*

        this.activity = activity;

        */
/* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE *//*

        this.arrReviews = arrReviews;
    }

    @Override
    public int getItemCount() {
        return arrReviews.size();
    }

    @Override
    public void onBindViewHolder(ReviewsVH holder, final int position) {
        ReviewsData data = arrReviews.get(position);

        */
/** SET THE RECOMMEND STATUS **//*

        String strRecommendStatus = data.getRecommendStatus();
        if (strRecommendStatus.equalsIgnoreCase("Yes")) {
            holder.imgvwLikeStatus.setIcon("faw-thumbs-o-up");
            holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_blue_dark));
        } else if (strRecommendStatus.equalsIgnoreCase("No"))   {
            holder.imgvwLikeStatus.setIcon("faw-thumbs-o-down");
            holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
        }

        */
/** GET THE FIRST CHARACTER OF THE USER NAME **//*

        String strFirstChar = data.getUserName().substring(0, 1);
        holder.txtNameStart.setText(strFirstChar);

        */
/** SET THE VISIT REASON **//*

        if (data.getVisitReason() != null)  {
            holder.txtVisitReason.setText(data.getVisitReason());
        }

        */
/** SET THE VISIT EXPERIENCE **//*

        if (data.getDoctorExperience() != null) {
            holder.txtVisitExperience.setText(data.getDoctorExperience());
        }

        */
/** SET THE USER NAME **//*

        if (data.getUserName() != null) {
            holder.txtUserName.setText(data.getUserName());
        }

        */
/** SET THE TIMESTAMP **//*

        if (data.getReviewTimestamp() != null)  {
            holder.txtTimeStamp.setText(data.getReviewTimestamp());
        }

        */
/** SET THE HELPFUL VOTES **//*


    }

    @Override
    public ReviewsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.custom_all_reviews_item, parent, false);

        return new ReviewsVH(itemView);
    }

    class ReviewsVH extends RecyclerView.ViewHolder	{
        final AppCompatTextView txtNameStart;
        final AppCompatTextView txtUserName;
        final AppCompatTextView txtTimeStamp;
        final IconicsImageView imgvwLikeStatus;
        final AppCompatTextView txtVisitReason;
        final AppCompatTextView txtVisitExperience;
        final AppCompatTextView txtHelpfulNo;
        final AppCompatTextView txtHelpfulYes;

        ReviewsVH(View v) {
            super(v);
            txtNameStart = (AppCompatTextView) itemView.findViewById(R.id.txtNameStart);
            txtUserName = (AppCompatTextView) itemView.findViewById(R.id.txtUserName);
            txtTimeStamp = (AppCompatTextView) itemView.findViewById(R.id.txtTimeStamp);
            imgvwLikeStatus = (IconicsImageView) itemView.findViewById(R.id.imgvwLikeStatus);
            txtVisitReason = (AppCompatTextView) itemView.findViewById(R.id.txtVisitReason);
            txtVisitExperience = (AppCompatTextView) itemView.findViewById(R.id.txtVisitExperience);
            txtHelpfulNo = (AppCompatTextView) itemView.findViewById(R.id.txtHelpfulNo);
            txtHelpfulYes = (AppCompatTextView) itemView.findViewById(R.id.txtHelpfulYes);
        }
    }
}*/
