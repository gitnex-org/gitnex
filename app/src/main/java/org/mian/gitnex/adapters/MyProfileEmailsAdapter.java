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
import org.gitnex.tea4j.v2.models.Email;
import org.mian.gitnex.R;
import java.util.List;

/**
 * Author M M Arif
 */

public class MyProfileEmailsAdapter extends RecyclerView.Adapter<MyProfileEmailsAdapter.EmailsViewHolder> {

    private final List<Email> emailsList;
    private final Context context;

    static class EmailsViewHolder extends RecyclerView.ViewHolder {

        private final ImageView emailPrimary;
        private final TextView userEmail;

        private EmailsViewHolder(View itemView) {
            super(itemView);

            emailPrimary = itemView.findViewById(R.id.emailPrimary);
            userEmail = itemView.findViewById(R.id.userEmail);

        }
    }

    public MyProfileEmailsAdapter(Context ctx, List<Email> emailsListMain) {
        this.context = ctx;
        this.emailsList = emailsListMain;
    }

    @NonNull
    @Override
    public MyProfileEmailsAdapter.EmailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_profile_emails, parent, false);
        return new MyProfileEmailsAdapter.EmailsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyProfileEmailsAdapter.EmailsViewHolder holder, int position) {

        Email currentItem = emailsList.get(position);

        holder.userEmail.setText(currentItem.getEmail());

        if(currentItem.isPrimary()) {
            TextDrawable drawable = TextDrawable.builder()
                    .beginConfig()
                    .textColor(ResourcesCompat.getColor(context.getResources(), R.color.colorWhite, null))
                    .fontSize(36)
                    .width(220)
                    .height(60)
                    .endConfig()
                    .buildRoundRect(context.getResources().getString(R.string.emailTypeText), ResourcesCompat.getColor(context.getResources(), R.color.tooltipBackground, null), 8);
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
