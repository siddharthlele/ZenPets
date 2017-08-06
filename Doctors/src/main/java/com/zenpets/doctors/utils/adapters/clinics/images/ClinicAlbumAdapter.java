package com.zenpets.doctors.utils.adapters.clinics.images;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.models.clinics.images.ClinicAlbumData;

import java.util.ArrayList;

public class ClinicAlbumAdapter extends RecyclerView.Adapter<ClinicAlbumAdapter.AlbumsVH> {

    /** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER **/
    private final Activity activity;

    /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<ClinicAlbumData> arrImages;

    public ClinicAlbumAdapter(Activity activity, ArrayList<ClinicAlbumData> arrImages) {

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
    public void onBindViewHolder(AlbumsVH holder, int position) {
        final ClinicAlbumData data = arrImages.get(position);

        /** SET THE CLINIC IMAGE **/
        if (data.getImageURL() != null) {
            Glide.with(activity)
                    .load(data.getImageURL())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(holder.imgvwClinicImage);
        }
    }

    @Override
    public AlbumsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.clinic_album_item, parent, false);

        return new AlbumsVH(itemView);
    }

    class AlbumsVH extends RecyclerView.ViewHolder   {

        AppCompatImageView imgvwClinicImage;

        AlbumsVH(View v) {
            super(v);
            imgvwClinicImage = (AppCompatImageView) v.findViewById(R.id.imgvwClinicImage);
        }
    }
}
