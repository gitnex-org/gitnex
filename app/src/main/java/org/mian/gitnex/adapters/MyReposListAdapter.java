package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author M M Arif
 */

public class MyReposListAdapter extends RecyclerView.Adapter<MyReposListAdapter.MyReposViewHolder> implements Filterable {

    private List<UserRepositories> reposList;
    private Context mCtx;
    private List<UserRepositories> reposListFull;

    static class MyReposViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageMy;
        private TextView mTextView1My;
        private TextView mTextView2My;
        private TextView fullNameMy;
        private ImageView repoPrivatePublicMy;
        private TextView repoStarsMy;
        private TextView repoWatchersMy;
        private TextView repoOpenIssuesCountMy;

        private MyReposViewHolder(View itemView) {
            super(itemView);
            mTextView1My = itemView.findViewById(R.id.repoNameMy);
            mTextView2My = itemView.findViewById(R.id.repoDescriptionMy);
            imageMy = itemView.findViewById(R.id.imageAvatarMy);
            fullNameMy = itemView.findViewById(R.id.repoFullNameMy);
            repoPrivatePublicMy = itemView.findViewById(R.id.imageRepoTypeMy);
            repoStarsMy = itemView.findViewById(R.id.repoStarsMy);
            repoWatchersMy = itemView.findViewById(R.id.repoWatchersMy);
            repoOpenIssuesCountMy = itemView.findViewById(R.id.repoOpenIssuesCountMy);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    Intent intent = new Intent(context, RepoDetailActivity.class);
                    intent.putExtra("repoFullName", fullNameMy.getText().toString());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("repoFullName", fullNameMy.getText().toString());
                    tinyDb.putBoolean("resumeIssues", true);
                    context.startActivity(intent);

                }
            });

        }
    }

    public MyReposListAdapter(Context mCtx, List<UserRepositories> reposListMain) {
        this.mCtx = mCtx;
        this.reposList = reposListMain;
        reposListFull = new ArrayList<>(reposList);
    }

    @NonNull
    @Override
    public MyReposListAdapter.MyReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_repos_list, parent, false);
        return new MyReposListAdapter.MyReposViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReposListAdapter.MyReposViewHolder holder, int position) {

        UserRepositories currentItem = reposList.get(position);
        holder.mTextView2My.setVisibility(View.GONE);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(currentItem.getName());
        String charac = String.valueOf(currentItem.getName().charAt(0));

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.DEFAULT)
                .fontSize(16)
                .toUpperCase()
                .width(28)
                .height(28)
                .endConfig()
                .buildRound(charac, color);

        holder.imageMy.setImageDrawable(drawable);
        holder.mTextView1My.setText(currentItem.getName());
        if (!currentItem.getDescription().equals("")) {
            holder.mTextView2My.setVisibility(View.VISIBLE);
            holder.mTextView2My.setText(currentItem.getDescription());
        }
        holder.fullNameMy.setText(currentItem.getFullname());
        if(currentItem.getPrivateFlag()) {
            holder.repoPrivatePublicMy.setImageResource(R.drawable.ic_lock_bold);
        }
        else {
            holder.repoPrivatePublicMy.setImageResource(R.drawable.ic_public);
        }
        holder.repoStarsMy.setText(currentItem.getStars_count());
        holder.repoWatchersMy.setText(currentItem.getWatchers_count());
        holder.repoOpenIssuesCountMy.setText(currentItem.getOpen_issues_count());

    }

    @Override
    public int getItemCount() {
        return reposList.size();
    }

    @Override
    public Filter getFilter() {
        return myReposFilter;
    }

    private Filter myReposFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<UserRepositories> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(reposListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (UserRepositories item : reposListFull) {
                    if (item.getFullname().toLowerCase().contains(filterPattern) || item.getDescription().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            reposList.clear();
            reposList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
