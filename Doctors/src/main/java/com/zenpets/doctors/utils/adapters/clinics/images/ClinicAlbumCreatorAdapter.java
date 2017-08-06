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

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.models.clinics.images.ClinicAlbumCreatorData;

import java.util.ArrayList;

public class ClinicAlbumCreatorAdapter extends RecyclerView.Adapter<ClinicAlbumCreatorAdapter.AlbumsVH> {

    /** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER **/
    private final Activity activity;

    /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<ClinicAlbumCreatorData> arrAlbums;

    public ClinicAlbumCreatorAdapter(Activity activity, ArrayList<ClinicAlbumCreatorData> arrAlbums) {

        /** CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE **/
        this.activity = activity;

        /** CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE **/
        this.arrAlbums = arrAlbums;
    }

    @Override
    public int getItemCount() {
        return arrAlbums.size();
    }

    @Override
    public void onBindViewHolder(AlbumsVH holder, int position) {
        final ClinicAlbumCreatorData td = arrAlbums.get(position);

        /** SET THE CLINIC IMAGE **/
        Bitmap bmpImage = td.getBmpClinicImage();
        if (bmpImage!= null)	{
            holder.imgvwClinicAlbum.setImageBitmap(bmpImage);
            holder.imgvwClinicAlbum.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        /** SET THE IMAGE NUMBER **/
        String strNumber = td.getTxtImageNumber();
        holder.txtImageNumber.setText(strNumber);
    }

    @Override
    public AlbumsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.clinic_album_creator_item, parent, false);

        return new AlbumsVH(itemView);
    }

    class AlbumsVH extends RecyclerView.ViewHolder   {
        
        AppCompatImageView imgvwClinicAlbum;
        AppCompatTextView txtImageNumber;

        AlbumsVH(View v) {
            super(v);
            imgvwClinicAlbum = (AppCompatImageView) v.findViewById(R.id.imgvwClinicAlbum);
            txtImageNumber = (AppCompatTextView) v.findViewById(R.id.txtImageNumber);
            txtImageNumber.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/RobotoCondensed-Bold.ttf"));
        }
    }
}
