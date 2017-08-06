package biz.zenpets.users.utils.adapters.adoptions;

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
import biz.zenpets.users.adoptions.images.AdoptionGalleryActivity;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;

public class AdoptionsImagesAdapter extends RecyclerView.Adapter<AdoptionsImagesAdapter.AlbumsVH> {

    /** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER **/
    private final Activity activity;

    /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<AdoptionsImageData> arrImages;

    public AdoptionsImagesAdapter(Activity activity, ArrayList<AdoptionsImageData> arrImages) {

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
    public void onBindViewHolder(final AlbumsVH holder, int position) {
        AdoptionsImageData data = arrImages.get(position);

        /* SET THE CLINIC IMAGE **/
        String url = data.getImageURL();
        Glide.with(activity)
                .load(url)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .centerCrop()
                .into(holder.imgvwImage);

        /* SHOW THE FULL SCREEN IMAGE */
        holder.imgvwImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] strings = new String[arrImages.size()];
                for (int i = 0; i < arrImages.size(); i++) {
                    strings[i] = arrImages.get(i).getImageURL();
                }
                Intent intent = new Intent(activity, AdoptionGalleryActivity.class);
                intent.putExtra("position", holder.getAdapterPosition());
                intent.putExtra("array", strings);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public AlbumsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.adoptions_images_item, parent, false);

        return new AlbumsVH(itemView);
    }

    class AlbumsVH extends RecyclerView.ViewHolder   {
        
        final AppCompatImageView imgvwImage;

        AlbumsVH(View v) {
            super(v);
            imgvwImage = (AppCompatImageView) v.findViewById(R.id.imgvwImage);
        }
    }
}
