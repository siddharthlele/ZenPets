package biz.zenpets.users.utils.adapters.questions;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.details.questions.QuestionDetails;
import biz.zenpets.users.utils.models.questions.UserQuestionsData;

public class UserQuestionsAdapter extends RecyclerView.Adapter<UserQuestionsAdapter.ConsultationsVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<UserQuestionsData> arrQuestions;

    public UserQuestionsAdapter(Activity activity, ArrayList<UserQuestionsData> arrQuestions) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrQuestions = arrQuestions;
    }

    @Override
    public int getItemCount() {
        return arrQuestions.size();
    }

    @Override
    public void onBindViewHolder(ConsultationsVH holder, final int position) {
        final UserQuestionsData data = arrQuestions.get(position);

        /* SET THE TITLE */
        if (data.getConsultationTitle() != null)    {
            holder.txtConsultationTitle.setText(data.getConsultationTitle());
        }

        /* SET THE DESCRIPTION */
        if (data.getConsultationsDescription() != null) {
            holder.txtConsultationDescription.setText(data.getConsultationsDescription());
        }

        /* SET THE TIME STAMP */
        if (data.getConsultationTimestamp() != null)    {
            holder.txtConsultationTimestamp.setText(data.getConsultationTimestamp());
        }

        /* SHOW THE CONSULTATION DETAILS */
        holder.linlaConsultationContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, QuestionDetails.class);
                intent.putExtra("CONSULTATION_ID", data.getConsultationID());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public ConsultationsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.fragment_question_item, parent, false);

        return new ConsultationsVH(itemView);
    }

    class ConsultationsVH extends RecyclerView.ViewHolder	{
        final LinearLayout linlaConsultationContainer;
        final AppCompatTextView txtConsultationTitle;
        final AppCompatTextView txtConsultationDescription;
        final AppCompatTextView txtConsultationTimestamp;
        final AppCompatTextView txtConsultationViews;

        ConsultationsVH(View v) {
            super(v);
            linlaConsultationContainer = (LinearLayout) v.findViewById(R.id.linlaConsultationContainer);
            txtConsultationTitle = (AppCompatTextView) v.findViewById(R.id.txtConsultationTitle);
            txtConsultationDescription = (AppCompatTextView) v.findViewById(R.id.txtConsultationDescription);
            txtConsultationTimestamp = (AppCompatTextView) v.findViewById(R.id.txtConsultationTimestamp);
            txtConsultationViews = (AppCompatTextView) v.findViewById(R.id.txtConsultationViews);
        }
    }
}