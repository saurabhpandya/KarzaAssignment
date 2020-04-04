package com.karzaassignment.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.karzaassignment.R;
import com.karzaassignment.model.CaptureImageModel;

import java.util.ArrayList;

public class CapturedImagesAdapter extends RecyclerView.Adapter<CapturedImagesAdapter.CapturedImageViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private ArrayList<CaptureImageModel> arylstCapturedImages = new ArrayList<>();

    private boolean isLoadingAdded = false;

    private Context mContext;

    private CapturedImageClickListner imageClickListner;

    public CapturedImagesAdapter(Context context, CapturedImageClickListner imageClickListner) {
        mContext = context;
        this.imageClickListner = imageClickListner;
    }

    @NonNull
    @Override
    public CapturedImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.raw_captured_image, parent, false);
        return new CapturedImageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CapturedImageViewHolder holder, int position) {
        final CaptureImageModel captureImageModel = arylstCapturedImages.get(position);
//        Glide.with(mContext).load(captureImageModel.getPath()).into(holder.imgvw);
        Glide.with(mContext).load(captureImageModel.getPath())
                .thumbnail(0.5f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.imgvw);

        holder.imgvw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageClickListner.onTapImage(captureImageModel.getPath());
            }
        });

    }

    @Override
    public int getItemCount() {
        return arylstCapturedImages == null ? 0 : arylstCapturedImages.size();
    }

    public void clearImages() {
        this.arylstCapturedImages.clear();
        notifyDataSetChanged();
    }

    public void addImages(ArrayList<CaptureImageModel> arylstImages) {
        this.arylstCapturedImages.addAll(arylstImages);
        notifyDataSetChanged();
    }

    public interface CapturedImageClickListner {
        void onTapImage(String imagePath);
    }

    class CapturedImageViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imgvw;

        public CapturedImageViewHolder(View itemView) {
            super(itemView);
            imgvw = itemView.findViewById(R.id.imgvw);
        }
    }

}