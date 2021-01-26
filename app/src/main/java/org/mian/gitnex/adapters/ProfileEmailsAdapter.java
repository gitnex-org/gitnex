package org.mian.gitnex.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import org.mian.gitnex.R;
import org.mian.gitnex.models.Emails;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileEmailsAdapter extends RecyclerView.Adapter<ProfileEmailsAdapter.EmailsViewHolder> {

    private List<Emails> emailsList;
    private Context mCtx;

    static class EmailsViewHolder extends RecyclerView.ViewHolder {

        private ImageView emailPrimary;
        private TextView userEmail;

        private EmailsViewHolder(View itemView) {
            super(itemView);

            emailPrimary = itemView.findViewById(R.id.emailPrimary);
            userEmail = itemView.findViewById(R.id.userEmail);

        }
    }

    public ProfileEmailsAdapter(Context mCtx, List<Emails> emailsListMain) {
        this.mCtx = mCtx;
        this.emailsList = emailsListMain;
    }

    @NonNull
    @Override
    public ProfileEmailsAdapter.EmailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_profile_emails, parent, false);
        return new ProfileEmailsAdapter.EmailsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileEmailsAdapter.EmailsViewHolder holder, int position) {

        Emails currentItem = emailsList.get(position);

        holder.userEmail.setText(currentItem.getEmail());

        if(currentItem.getPrimary()) {
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .textColor(ResourcesCompat.getColor(mCtx.getResources(), R.color.colorWhite, null))
                    .fontSize(36)
                    .width(220)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(mCtx.getResources().getString(R.string.emailTypeText), ResourcesCompat.getColor(mCtx.getResources(), R.color.tooltipBackground, null), 8);
            holder.emailPrimary.setImageDrawable(drawable);
        }
        else {
            holder.emailPrimary.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return emailsList.size();
    }

}
