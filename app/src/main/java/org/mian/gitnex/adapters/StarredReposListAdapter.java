package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.TinyDB;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
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
        private TextView repoWatchers;
        private TextView repoOpenIssuesCount;

        private StarredReposViewHolder(View itemView) {
            super(itemView);
            mTextView1 = itemView.findViewById(R.id.repoName);
            mTextView2 = itemView.findViewById(R.id.repoDescription);
            image = itemView.findViewById(R.id.imageAvatar);
            fullName = itemView.findViewById(R.id.repoFullName);
            repoPrivatePublic = itemView.findViewById(R.id.imageRepoType);
            repoStars = itemView.findViewById(R.id.repoStars);
            repoWatchers = itemView.findViewById(R.id.repoWatchers);
            repoOpenIssuesCount = itemView.findViewById(R.id.repoOpenIssuesCount);
            ImageView reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();

                    Intent intent = new Intent(context, RepoDetailActivity.class);
                    intent.putExtra("repoFullName", fullName.getText().toString());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("repoFullName", fullName.getText().toString());
                    tinyDb.putBoolean("resumeIssues", true);
                    context.startActivity(intent);

                }
            });

            reposDropdownMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Context context = v.getContext();
                    Context context_ = new ContextThemeWrapper(context, R.style.popupMenuStyle);

                    PopupMenu popupMenu = new PopupMenu(context_, v);
                    popupMenu.inflate(R.menu.repo_dotted_list_menu);

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
                                case R.id.repoStargazers:

                                    Intent intent = new Intent(context, RepoStargazersActivity.class);
                                    intent.putExtra("repoFullNameForStars", fullName.getText());
                                    context.startActivity(intent);
                                    break;

                                case R.id.repoWatchers:

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

    public StarredReposListAdapter(Context mCtx, List<UserRepositories> reposListMain) {
        this.mCtx = mCtx;
        this.reposList = reposListMain;
        reposListFull = new ArrayList<>(reposList);
    }

    @NonNull
    @Override
    public StarredReposListAdapter.StarredReposViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.starred_repos_list, parent, false);
        return new StarredReposListAdapter.StarredReposViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StarredReposListAdapter.StarredReposViewHolder holder, int position) {

        UserRepositories currentItem = reposList.get(position);
        holder.mTextView2.setVisibility(View.GONE);

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

        holder.image.setImageDrawable(drawable);
        holder.mTextView1.setText(currentItem.getName());
        if (!currentItem.getDescription().equals("")) {
            holder.mTextView2.setVisibility(View.VISIBLE);
            holder.mTextView2.setText(currentItem.getDescription());
        }
        holder.fullName.setText(currentItem.getFullname());
        if(currentItem.getPrivateFlag()) {
            holder.repoPrivatePublic.setImageResource(R.drawable.ic_lock_bold);
        }
        else {
            holder.repoPrivatePublic.setImageResource(R.drawable.ic_public);
        }
        holder.repoStars.setText(currentItem.getStars_count());
        holder.repoWatchers.setText(currentItem.getWatchers_count());
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
