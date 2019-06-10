package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.LabelWidthCalculator;
import org.mian.gitnex.models.Labels;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

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
        private ImageView labelsView;
        private ImageView labelsOptionsMenu;

        private LabelsViewHolder(View itemView) {
            super(itemView);

            labelsView = itemView.findViewById(R.id.labelsView);
            labelsOptionsMenu = itemView.findViewById(R.id.labelsOptionsMenu);
            labelTitle = itemView.findViewById(R.id.labelTitle);
            labelId = itemView.findViewById(R.id.labelId);
            labelColor = itemView.findViewById(R.id.labelColor);

            labelsOptionsMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Context context = v.getContext();
                    Context context_ = new ContextThemeWrapper(context, R.style.popupMenuStyle);

                    PopupMenu popupMenu = new PopupMenu(context_, v);
                    popupMenu.inflate(R.menu.labels_menu);

                    Object menuHelper;
                    Class[] argTypes;
                    try {

                        Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                        fMenuHelper.setAccessible(true);
                        menuHelper = fMenuHelper.get(popupMenu);
                        argTypes = new Class[] { boolean.class };
                        menuHelper.getClass().getDeclaredMethod("setForceShowIcon",
                                argTypes).invoke(menuHelper, true);

                    } catch (Exception e) {

                        popupMenu.show();
                        return;

                    }

                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.labelMenuEdit:

                                    Intent intent = new Intent(context, CreateLabelActivity.class);
                                    intent.putExtra("labelId", labelId.getText());
                                    intent.putExtra("labelTitle", labelTitle.getText());
                                    intent.putExtra("labelColor", labelColor.getText());
                                    intent.putExtra("labelAction", "edit");
                                    context.startActivity(intent);
                                    break;

                                case R.id.labelMenuDelete:

                                    AlertDialogs.labelDeleteDialog(context, labelTitle.getText().toString(), labelId.getText().toString(),
                                            context.getResources().getString(R.string.labelDeleteTitle),
                                            context.getResources().getString(R.string.labelDeleteMessage),
                                            context.getResources().getString(R.string.labelDeletePositiveButton),
                                            context.getResources().getString(R.string.labelDeleteNegativeButton));
                                    break;

                            }
                            return false;
                        }
                    });

                    popupMenu.show();

                }
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.labels_list, parent, false);
        return new LabelsAdapter.LabelsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LabelsAdapter.LabelsViewHolder holder, int position) {

        Labels currentItem = labelsList.get(position);
        int width = 33;

        holder.labelTitle.setText(currentItem.getName());
        holder.labelId.setText(String.valueOf(currentItem.getId()));
        holder.labelColor.setText(currentItem.getColor());

        String labelColor = currentItem.getColor();
        String labelName = currentItem.getName();
        int color = Color.parseColor("#" + labelColor);

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                //.useFont(Typeface.DEFAULT)
                .textColor(new ColorInverter().getContrastColor(color))
                .fontSize(36)
                .width(LabelWidthCalculator.customWidth(getMaxLabelLength()))
                .height(60)
                .endConfig()
                .buildRoundRect(labelName, color, 8);
        holder.labelsView.setImageDrawable(drawable);

    }

    private int getMaxLabelLength() {

        for(int i = 0; i < labelsList.size(); i++) {

            Labels labelItem = labelsList.get(i);
            labelsArray.add(labelItem.getName().length());

        }

        return Collections.max(labelsArray);

    }

    @Override
    public int getItemCount() {
        return labelsList.size();
    }

}