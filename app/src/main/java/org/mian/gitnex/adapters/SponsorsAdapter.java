package org.mian.gitnex.adapters;

import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import java.util.List;

/**
 * Author M M Arif
 */

public class SponsorsAdapter extends RecyclerView.Adapter<SponsorsAdapter.SponsorsViewHolder> {

    private List<CharSequence> sponsorsList;

    static class SponsorsViewHolder extends RecyclerView.ViewHolder {

        private TextView sponsorText;

        private SponsorsViewHolder(View itemView) {
            super(itemView);

            sponsorText = itemView.findViewById(R.id.sponsorText);

        }
    }

    public SponsorsAdapter(List<CharSequence> sponsorsListMain) {
        this.sponsorsList = sponsorsListMain;
    }

    @NonNull
    @Override
    public SponsorsAdapter.SponsorsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.sponsors, parent, false);
        return new SponsorsAdapter.SponsorsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SponsorsAdapter.SponsorsViewHolder holder, int position) {

        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sponsorsList.get(position));
        holder.sponsorText.setText((strBuilder));
        holder.sponsorText.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public int getItemCount() {
        return sponsorsList.size();
    }

}
