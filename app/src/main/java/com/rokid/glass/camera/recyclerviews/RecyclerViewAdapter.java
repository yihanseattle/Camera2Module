package com.rokid.glass.camera.recyclerviews;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rokid.glass.camera.R;
import com.rokid.glass.camera.constant.Constants;
import com.rokid.glass.camera.utils.Utils;

import java.util.ArrayList;

/**
 * Created by yihan on 5/17/18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    // vars
    private ArrayList<String> mCameraModes = new ArrayList<>();
    private Context mContext;

    public RecyclerViewAdapter(Context mContext, ArrayList<String> mCameraModes) {
        this.mContext = mContext;
        this.mCameraModes = mCameraModes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: called");

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_listitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        holder.tvCameraMode.setText(mCameraModes.get(position));
        if (position == 1) {
            holder.tvCameraMode.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_text_highlighted));
            holder.tvCameraMode.setTextColor(mContext.getColor(R.color.color_text_highlight));
//            holder.tvCameraMode.setTypeface(ResourcesCompat.getFont(mContext, R.font.notosanscjk_medium));
            holder.tvCameraMode.setTypeface(null, Typeface.BOLD);
            holder.tvCameraMode.setPadding(
                    Utils.getDPFromPx(mContext, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                    0,
                    Utils.getDPFromPx(mContext, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                    0);

        } else if (position == 2) {
            holder.tvCameraMode.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_text_default));
            holder.tvCameraMode.setPadding(
                    Utils.getDPFromPx(mContext, Constants.CAMERA_MODE_TEXT_PADDING_LEFT),
                    Utils.getDPFromPx(mContext, Constants.CAMERA_MODE_TEXT_PADDING_TOP),
                    Utils.getDPFromPx(mContext, Constants.CAMERA_MODE_TEXT_PADDING_RIGHT),
                    0);
            holder.tvCameraMode.setTextColor(mContext.getColor(R.color.color_text_highlight));
//            holder.tvCameraMode.setTypeface(ResourcesCompat.getFont(mContext, R.font.notosanscjk_regular));
            holder.tvCameraMode.setTypeface(null, Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return mCameraModes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCameraMode;

        public ViewHolder(View itemView) {
            super(itemView);
            tvCameraMode = itemView.findViewById(R.id.tvCameraMode);
        }
    }
}
