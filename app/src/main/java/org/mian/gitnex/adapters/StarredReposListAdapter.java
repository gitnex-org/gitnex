package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OpenRepoInBrowserActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Author M M Arif
 */

public class StarredReposListAdapter extends RecyclerView.Adapter<StarredReposListAdapter.StarredReposViewHolder> implements Filterable {

    private List<UserRepositories> reposList;
    private Context mCtx;
    private List<UserRepositories> reposListFull;

    static class StarredReposViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView mTextView1;
        private TextView mTextView2;
        private TextView fullName;
        private ImageView repoPrivatePublic;
        private TextView repoStars;
        private TextView repoForks;
        private TextView repoOpenIssuesCount;
        private TextView repoType;

        private StarredReposViewHolder(View itemView) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.repoName);
            mTextView2 = itemView.findViewById(R.id.repoDescription);
            image = itemView.findViewById(R.id.imageAvatar);
            fullName = itemView.findViewById(R.id.repoFullName);
            repoPrivatePublic = itemView.findViewById(R.id.imageRepoType);
            repoStars = itemView.findViewById(R.id.repoStars);
            repoForks = itemView.findViewById(R.id.repoForks);
            repoOpenIssuesCount = itemView.findViewById(R.id.repoOpenIssuesCount);
            ImageView reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);
            repoType = itemView.findViewById(R.id.repoType);

            itemView.setOnClickListener(v -> {

                Context context = v.getContext();

                Intent intent = new Intent(context, RepoDetailActivity.class);
                intent.putExtra("repoFullName", fullName.getText().toString());

                TinyDB tinyDb = new TinyDB(context);
                tinyDb.putString("repoFullName", fullName.getText().toString());
                tinyDb.putString("repoType", repoType.getText().toString());
                tinyDb.putBoolean("resumeIssues", true);
                context.startActivity(intent);

            });

            reposDropdownMenu.setOnClickListener(v -> {

                final Context context = v.getContext();

                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_repository_in_list, null);

                TextView repoOpenInBrowser = view.findViewById(R.id.repoOpenInBrowser);
                TextView repoStargazers = view.findViewById(R.id.repoStargazers);
                TextView repoWatchers = view.findViewById(R.id.repoWatchers);

                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(view);
                dialog.show();

                repoOpenInBrowser.setOnClickListener(openInBrowser -> {

                    Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
                    intentOpenInBrowser.putExtra("repoFullNameBrowser", fullName.getText());
                    context.startActivity(intentOpenInBrowser);
                    dialog.dismiss();

                });

                repoStargazers.setOnClickListener(openInBrowser -> {

                    Intent intent = new Intent(context, RepoStargazersActivity.class);
                    intent.putExtra("repoFullNameForStars", fullName.getText());
                    context.startActivity(intent);
                    dialog.dismiss();

                });

                repoWatchers.setOnClickListener(openInBrowser -> {

                    Intent intentW = new Intent(context, RepoWatchersActivity.class);
                    intentW.putExtra("repoFullNameForWatchers", fullName.getText());
                    context.startActivity(intentW);
                    dialog.dismiss();

                });

            });

        }

    }

    public StarredReposListAdapter(Context mCtx, List<UserRepositories> reposListMain) {
        this.mCtx = mCtx;
        this.reposList = reposListMain;
        reposListFull = new ArrayList<>(reposList);
    }

    @NonNull
    @Override
    public StarredReposListAdapter.StarredReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_starred_repos, parent, false);
        return new StarredReposListAdapter.StarredReposViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StarredReposListAdapter.StarredReposViewHolder holder, int position) {

        UserRepositories currentItem = reposList.get(position);
        holder.mTextView2.setVisibility(View.GONE);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        int color = generator.getColor(currentItem.getName());
        String firstCharacter = String.valueOf(currentItem.getName().charAt(0));

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.DEFAULT)
                .fontSize(18)
                .toUpperCase()
                .width(28)
                .height(28)
                .endConfig()
                .buildRoundRect(firstCharacter, color, 3);

        if (currentItem.getAvatar_url() != null) {
            if (!currentItem.getAvatar_url().equals("")) {
                Picasso.get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.image);
            } else {
                holder.image.setImageDrawable(drawable);
            }
        }
        else {
            holder.image.setImageDrawable(drawable);
        }

        holder.mTextView1.setText(currentItem.getName());
        if (!currentItem.getDescription().equals("")) {
            holder.mTextView2.setVisibility(View.VISIBLE);
            holder.mTextView2.setText(currentItem.getDescription());
        }
        holder.fullName.setText(currentItem.getFullname());
        if(currentItem.getPrivateFlag()) {
            holder.repoPrivatePublic.setImageResource(R.drawable.ic_lock_bold);
            holder.repoType.setText(R.string.strPrivate);
        }
        else {
            holder.repoPrivatePublic.setImageResource(R.drawable.ic_public);
            holder.repoType.setText(R.string.strPublic);
        }
        holder.repoStars.setText(currentItem.getStars_count());
        holder.repoForks.setText(currentItem.getForks_count());
        holder.repoOpenIssuesCount.setText(currentItem.getOpen_issues_count());

    }

    @Override
    public int getItemCount() {
        return reposList.size();
    }

    @Override
    public Filter getFilter() {
        return starredReposFilter;
    }

    private Filter starredReposFilter = new Filter() {
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
