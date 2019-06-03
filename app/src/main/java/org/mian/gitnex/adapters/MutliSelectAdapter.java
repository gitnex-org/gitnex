package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.MultiSelectDialog;
import org.mian.gitnex.models.MultiSelectModel;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author com.github.abumoallim, modified by M M Arif
 */

public class MutliSelectAdapter extends RecyclerView.Adapter<MutliSelectAdapter.MultiSelectDialogViewHolder> {

    private ArrayList<MultiSelectModel> mDataSet;
    private String mSearchQuery = "";
    private Context mContext;

    public MutliSelectAdapter(ArrayList<MultiSelectModel> dataSet, Context context) {
        this.mDataSet = dataSet;
        this.mContext = context;
    }

    @NonNull
    @Override
    public MultiSelectDialogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.multi_select_item, parent, false);
        return new MultiSelectDialogViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MultiSelectDialogViewHolder holder, int position) {

        if (!mSearchQuery.equals("") && mSearchQuery.length() > 1) {
            setHighlightedText(position, holder.dialog_name_item);
        } else {
            holder.dialog_name_item.setText(mDataSet.get(position).getName());
        }

        if (mDataSet.get(position).getSelected()) {

            if (!MultiSelectDialog.selectedIdsForCallback.contains(mDataSet.get(position).getId())) {
                MultiSelectDialog.selectedIdsForCallback.add(mDataSet.get(position).getId());
            }
        }

        if (checkForSelection(mDataSet.get(position).getId())) {
            holder.dialog_item_checkbox.setChecked(true);
        } else {
            holder.dialog_item_checkbox.setChecked(false);
        }

        /*holder.dialog_item_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.dialog_item_checkbox.isChecked()) {
                    MultiSelectDialog.selectedIdsForCallback.add(mDataSet.get(holder.getAdapterPosition()).getId());
                    holder.dialog_item_checkbox.setChecked(true);
                } else {
                    removeFromSelection(mDataSet.get(holder.getAdapterPosition()).getId());
                    holder.dialog_item_checkbox.setChecked(false);
                }
            }
        });*/

        holder.main_container.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!holder.dialog_item_checkbox.isChecked()) {
                    MultiSelectDialog.selectedIdsForCallback.add(mDataSet.get(holder.getAdapterPosition()).getId());
                    holder.dialog_item_checkbox.setChecked(true);
                    mDataSet.get(holder.getAdapterPosition()).setSelected(true);
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    removeFromSelection(mDataSet.get(holder.getAdapterPosition()).getId());
                    holder.dialog_item_checkbox.setChecked(false);
                    mDataSet.get(holder.getAdapterPosition()).setSelected(false);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            }

        });
    }

    private void setHighlightedText(int position, TextView textview) {

        String name = mDataSet.get(position).getName();
        SpannableString str = new SpannableString(name);
        int endLength = name.toLowerCase().indexOf(mSearchQuery) + mSearchQuery.length();
        ColorStateList highlightedColor = new ColorStateList(new int[][]{new int[]{}}, new int[]{ContextCompat.getColor(mContext, R.color.colorAccent)});
        TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(null, Typeface.NORMAL, -1, highlightedColor, null);
        str.setSpan(textAppearanceSpan, name.toLowerCase().indexOf(mSearchQuery), endLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textview.setText(str);

    }

    private void removeFromSelection(Integer id) {

        for (int i = 0; i < MultiSelectDialog.selectedIdsForCallback.size(); i++) {
            if (id.equals(MultiSelectDialog.selectedIdsForCallback.get(i))) {
                MultiSelectDialog.selectedIdsForCallback.remove(i);
            }
        }

    }

    private boolean checkForSelection(Integer id) {

        for (int i = 0; i < MultiSelectDialog.selectedIdsForCallback.size(); i++) {
            if (id.equals(MultiSelectDialog.selectedIdsForCallback.get(i))) {
                return true;
            }
        }
        return false;

    }

    /*//get selected name string separated by coma
    public String getDataString() {
        String data = "";
        for (int i = 0; i < mDataSet.size(); i++) {
            if (checkForSelection(mDataSet.get(i).getId())) {
                data = data + ", " + mDataSet.get(i).getName();
            }
        }
        if (data.length() > 0) {
            return data.substring(1);
        } else {
            return "";
        }
    }
    //get selected name ararylist
    public ArrayList<String> getSelectedNameList() {
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < mDataSet.size(); i++) {
            if (checkForSelection(mDataSet.get(i).getId())) {
                names.add(mDataSet.get(i).getName());
            }
        }
        //  return names.toArray(new String[names.size()]);
        return names;
    }*/

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }

    public void setData(ArrayList<MultiSelectModel> data, String query, MutliSelectAdapter mutliSelectAdapter) {

        this.mDataSet = data;
        this.mSearchQuery = query;
        mutliSelectAdapter.notifyDataSetChanged();

    }

    class MultiSelectDialogViewHolder extends RecyclerView.ViewHolder {

        private TextView dialog_name_item;
        private AppCompatCheckBox dialog_item_checkbox;
        private LinearLayout main_container;

        MultiSelectDialogViewHolder(View view) {

            super(view);
            dialog_name_item = view.findViewById(R.id.dialog_item_name);
            dialog_item_checkbox = view.findViewById(R.id.dialog_item_checkbox);
            main_container = view.findViewById(R.id.main_container);

        }

    }
}