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
import org.gitnex.tea4j.models.Labels;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ColorInverter;
import java.util.List;

/**
 * Author M M Arif
 */

public class LabelsAdapter extends RecyclerView.Adapter<LabelsAdapter.LabelsViewHolder>  {

    private final List<Labels> labelsList;
    private static String type;
	private static String orgName;

    static class LabelsViewHolder extends RecyclerView.ViewHolder {

    	private Labels labels;

        private final CardView labelView;
        private final ImageView labelIcon;
        private final TextView labelName;

        private LabelsViewHolder(View itemView) {
            super(itemView);

            labelView = itemView.findViewById(R.id.labelView);
            labelIcon = itemView.findViewById(R.id.labelIcon);
            labelName = itemView.findViewById(R.id.labelName);
            ImageView labelsOptionsMenu = itemView.findViewById(R.id.labelsOptionsMenu);

            labelsOptionsMenu.setOnClickListener(v -> {

                final Context context = v.getContext();

                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_labels_in_list, null);

                TextView labelMenuEdit = view.findViewById(R.id.labelMenuEdit);
                TextView labelMenuDelete = view.findViewById(R.id.labelMenuDelete);
                TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

                bottomSheetHeader.setText(labels.getName());
                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(view);
                dialog.show();

                labelMenuEdit.setOnClickListener(editLabel -> {

                    Intent intent = new Intent(context, CreateLabelActivity.class);
                    intent.putExtra("labelId", String.valueOf(labels.getId()));
                    intent.putExtra("labelTitle", labels.getName());
                    intent.putExtra("labelColor", labels.getColor());
                    intent.putExtra("labelAction", "edit");
	                intent.putExtra("type", type);
	                intent.putExtra("orgName", orgName);
                    context.startActivity(intent);
                    dialog.dismiss();
                });

                labelMenuDelete.setOnClickListener(deleteLabel -> {

                    AlertDialogs.labelDeleteDialog(context, labels.getName(), String.valueOf(labels.getId()),
                            context.getResources().getString(R.string.labelDeleteTitle),
                            context.getResources().getString(R.string.labelDeleteMessage),
                            context.getResources().getString(R.string.labelDeleteTitle),
                            context.getResources().getString(R.string.labelDeleteNegativeButton),
	                        type, orgName);
                    dialog.dismiss();
                });

            });

        }
    }

    public LabelsAdapter(Context ctx, List<Labels> labelsMain, String type, String orgName) {

	    this.labelsList = labelsMain;
        LabelsAdapter.type = type;
        LabelsAdapter.orgName = orgName;
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
	    holder.labels = currentItem;

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
