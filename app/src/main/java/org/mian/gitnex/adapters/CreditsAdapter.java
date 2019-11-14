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

public class CreditsAdapter extends RecyclerView.Adapter<CreditsAdapter.CreditsViewHolder> {

    private List<CharSequence> creditsList;

    static class CreditsViewHolder extends RecyclerView.ViewHolder {

        private TextView creditText;

        private CreditsViewHolder(View itemView) {
            super(itemView);

            creditText = itemView.findViewById(R.id.creditText);

        }
    }

    public CreditsAdapter(List<CharSequence> creditsListMain) {
        this.creditsList = creditsListMain;
    }

    @NonNull
    @Override
    public CreditsAdapter.CreditsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.credits, parent, false);
        return new CreditsAdapter.CreditsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CreditsAdapter.CreditsViewHolder holder, int position) {

        SpannableStringBuilder strBuilder = new SpannableStringBuilder(creditsList.get(position));
        holder.creditText.setText((strBuilder));
        holder.creditText.setMovementMethod(LinkMovementMethod.getInstance());

    }

    @Override
    public int getItemCount() {
        return creditsList.size();
    }

}
