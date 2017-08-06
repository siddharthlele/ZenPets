package biz.zenpets.users.utils.adapters.calendar;

//public class ZenCalendarAdapter extends RecyclerView.Adapter<ZenCalendarAdapter.CalendarVH> {
//
//    /** AN ACTIVITY INSTANCE **/
//    private Activity activity;
//
//    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
//    private ArrayList<ZenCalendarData> arrDates;
//
//    /** SELECTED ITEM **/
//    private int selectedPosition = 0;
//
//    public ZenCalendarAdapter(Activity activity, ArrayList<ZenCalendarData> arrDates) {
//
//        /* CAST THE ACTIVITY IN THE GLOBAL INSTANCE */
//        this.activity = activity;
//
//        /* CAST THE CONTENTS OF THE LOCAL ARRAY LIST IN THE METHOD TO THE GLOBAL INSTANCE */
//        this.arrDates = arrDates;
//    }
//
//    @Override
//    public int getItemCount() {
//        return arrDates.size();
//    }
//
//    @Override
//    public void onBindViewHolder(final CalendarVH holder, final int position) {
//        ZenCalendarData data = arrDates.get(position);
//
//        if (selectedPosition == position)   {
//            holder.txtShortDate.setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.circular_btn_bg));
//        } else {
//            holder.txtShortDate.setBackgroundDrawable(null);
//        }
//
//        /* SET THE SHORT DAY */
//        if (data.getShortDay() != null) {
//            holder.txtShortDay.setText(data.getShortDay());
//        }
//
//        /* SET THE SHORT DATE */
//        if (data.getShortDate() != null)    {
//            holder.txtShortDate.setText(data.getShortDate());
//        }
//
//        /** SHOW THE AVAILABILITY ON THE SELECTED DATE **/
//        holder.linlaDate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                selectedPosition = position;
//                notifyDataSetChanged();
//                Toast.makeText(activity, "Clicked", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public CalendarVH onCreateViewHolder(ViewGroup parent, int i) {
//
//        View itemView = LayoutInflater.
//                from(parent.getContext()).
//                inflate(R.layout.zen_calendar_item, parent, false);
//
//        return new CalendarVH(itemView);
//    }
//
//    class CalendarVH extends RecyclerView.ViewHolder	{
//
//        LinearLayout linlaDate;
//        AppCompatTextView txtShortDay;
//        AppCompatTextView txtShortDate;
//
//        CalendarVH(View v) {
//            super(v);
//            linlaDate = (LinearLayout) v.findViewById(R.id.linlaDate);
//            txtShortDay = (AppCompatTextView) v.findViewById(R.id.txtShortDay);
//            txtShortDate = (AppCompatTextView) v.findViewById(R.id.txtShortDate);
//
//            /* GET THE DISPLAY SIZE */
//            DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
//            int width = (int) ((float) metrics.widthPixels);
//            linlaDate.getLayoutParams().width = width / 7;
//        }
//    }
//}