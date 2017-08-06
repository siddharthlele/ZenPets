package biz.zenpets.users.utils.adapters.problems;

//public class ProblemsListAdapter extends RecyclerView.Adapter<ProblemsListAdapter.ProblemsVH> {
//
//    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
//    private final Activity activity;
//
//    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
//    private final ArrayList<ProblemData> arrProblems;
//
//    public ProblemsListAdapter(Activity activity, ArrayList<ProblemData> arrProblems) {
//
//        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
//        this.activity = activity;
//
//        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
//        this.arrProblems = arrProblems;
//    }
//
//    @Override
//    public int getItemCount() {
//        return arrProblems.size();
//    }
//
//    @Override
//    public void onBindViewHolder(ProblemsVH holder, final int position) {
//        final ProblemData data = arrProblems.get(position);
//
//        /* SET THE PROBLEM TEXT */
//        holder.txtProblemText.setText(data.getProblemText());
//
//        /** SEND THE PROBLEM ID BACK TO THE FILTERS ACTIVITY **/
//        holder.txtProblemText.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent();
//                intent.putExtra("PROBLEM_ID", data.getProblemID());
//                activity.setResult(activity.RESULT_OK, intent);
//                activity.finish();
//            }
//        });
//    }
//
//    @Override
//    public ProblemsVH onCreateViewHolder(ViewGroup parent, int i) {
//
//        View itemView = LayoutInflater.
//                from(parent.getContext()).
//                inflate(R.layout.problems_list_item, parent, false);
//
//        return new ProblemsVH(itemView);
//    }
//
//    class ProblemsVH extends RecyclerView.ViewHolder	{
//
//        final AppCompatTextView txtProblemText;
//
//        ProblemsVH(View v) {
//            super(v);
//            txtProblemText = (AppCompatTextView) v.findViewById(R.id.txtProblemText);
//        }
//    }
//}