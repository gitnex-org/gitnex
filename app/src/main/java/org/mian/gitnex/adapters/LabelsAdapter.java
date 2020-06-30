package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.models.Labels;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.LabelsViewHolder>  {

    private List<Labels> labelsList;
    final private Context mCtx;
    private ArrayList<Integer> labelsArray = new ArrayList<>();

    static class LabelsViewHolder extends RecyclerView.ViewHolder {

        private TextView labelTitle;
        private TextView labelId;
        private TextView labelColor;

        private CardView labelView;
        private ImageView labelIcon;
        private TextView labelName;

        private LabelsViewHolder(View itemView) {
            super(itemView);

            labelView = itemView.findViewById(R.id.labelView);
            labelIcon = itemView.findViewById(R.id.labelIcon);
            labelName = itemView.findViewById(R.id.labelName);
            ImageView labelsOptionsMenu = itemView.findViewById(R.id.labelsOptionsMenu);
            labelTitle = itemView.findViewById(R.id.labelTitle);
            labelId = itemView.findViewById(R.id.labelId);
            labelColor = itemView.findViewById(R.id.labelColor);

            labelsOptionsMenu.setOnClickListener(v -> {

                final Context context = v.getContext();

                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_labels_in_list, null);

                TextView labelMenuEdit = view.findViewById(R.id.labelMenuEdit);
                TextView labelMenuDelete = view.findViewById(R.id.labelMenuDelete);
                TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

                bottomSheetHeader.setText(labelTitle.getText());
                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(view);
                dialog.show();

                labelMenuEdit.setOnClickListener(editLabel -> {

                    Intent intent = new Intent(context, CreateLabelActivity.class);
                    intent.putExtra("labelId", labelId.getText());
                    intent.putExtra("labelTitle", labelTitle.getText());
                    intent.putExtra("labelColor", labelColor.getText());
                    intent.putExtra("labelAction", "edit");
                    context.startActivity(intent);
                    dialog.dismiss();

                });

                labelMenuDelete.setOnClickListener(deleteLabel -> {

                    AlertDialogs.labelDeleteDialog(context, labelTitle.getText().toString(), labelId.getText().toString(),
                            context.getResources().getString(R.string.labelDeleteTitle),
                            context.getResources().getString(R.string.labelDeleteMessage),
                            context.getResources().getString(R.string.labelDeletePositiveButton),
                            context.getResources().getString(R.string.labelDeleteNegativeButton));
                    dialog.dismiss();

                });

            });

        }
    }

    public LabelsAdapter(Context mCtx, List<Labels> labelsMain) {
        this.mCtx = mCtx;
        this.labelsList = labelsMain;
    }

    @NonNull
    @Override
    public LabelsAdapter.LabelsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_labels, parent, false);
        return new LabelsAdapter.LabelsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LabelsAdapter.LabelsViewHolder holder, int position) {

        Labels currentItem = labelsList.get(position);

        holder.labelTitle.setText(currentItem.getName());
        holder.labelId.setText(String.valueOf(currentItem.getId()));
        holder.labelColor.setText(currentItem.getColor());

        String labelColor = currentItem.getColor();
        String labelName = currentItem.getName();

        int color = Color.parseColor("#" + labelColor);
        int contrastColor = new ColorInverter().getContrastColor(color);

	    ImageViewCompat.setImageTintList(holder.labelIcon, ColorStateList.valueOf(contrastColor));

        holder.labelName.setTextColor(contrastColor);
        holder.labelName.setText(labelName);
        holder.labelView.setCardBackgroundColor(color);

    }

    @Override
    public int getItemCount() {
        return labelsList.size();
    }

}
