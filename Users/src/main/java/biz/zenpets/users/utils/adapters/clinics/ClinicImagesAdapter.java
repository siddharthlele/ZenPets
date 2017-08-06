package biz.zenpets.users.utils.adapters.clinics;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.details.doctor.images.ClinicGalleryActivity;
import biz.zenpets.users.utils.models.clinics.ClinicImagesData;

public class ClinicImagesAdapter extends RecyclerView.Adapter<ClinicImagesAdapter.ImagesVH> {

    /** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER **/
    private final Activity activity;

    /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<ClinicImagesData> arrImages;

    public ClinicImagesAdapter(Activity activity, ArrayList<ClinicImagesData> arrImages) {

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE **/
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE **/
        this.arrImages = arrImages;
    }

    @Override
    public int getItemCount() {
        return arrImages.size();
    }

    @Override
    public void onBindViewHolder(final ImagesVH holder, int position) {
        final ClinicImagesData data = arrImages.get(position);

        /** SET THE CLINIC IMAGE **/
        if (data.getImageURL() != null) {
            Glide.with(activity)
                    .load(data.getImageURL())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(holder.imgvwClinicImage);
        }

        /** SHOW FULL SCREEN IMAGE **/
        holder.imgvwClinicImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strings = new String[arrImages.size()];
                for (int i = 0; i < arrImages.size(); i++) {
                    strings[i] = arrImages.get(i).getImageURL();
                }
                Intent intent = new Intent(activity, ClinicGalleryActivity.class);
                intent.putExtra("position", holder.getAdapterPosition());
                intent.putExtra("array", strings);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public ImagesVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.clinic_images_item, parent, false);

        return new ImagesVH(itemView);
    }

    class ImagesVH extends RecyclerView.ViewHolder   {
        final AppCompatImageView imgvwClinicImage;

        ImagesVH(View v) {
            super(v);
            imgvwClinicImage = (AppCompatImageView) v.findViewById(R.id.imgvwClinicImage);
        }
    }
}
