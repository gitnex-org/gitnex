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
import com.squareup.picasso.Picasso;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OpenRepoInBrowserActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.helpers.RoundedTransformation;
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
        private TextView repoForksMy;
        private TextView repoOpenIssuesCountMy;

        private MyReposViewHolder(View itemView) {
            super(itemView);
            mTextView1My = itemView.findViewById(R.id.repoNameMy);
            mTextView2My = itemView.findViewById(R.id.repoDescriptionMy);
            imageMy = itemView.findViewById(R.id.imageAvatarMy);
            fullNameMy = itemView.findViewById(R.id.repoFullNameMy);
            repoPrivatePublicMy = itemView.findViewById(R.id.imageRepoTypeMy);
            repoStarsMy = itemView.findViewById(R.id.repoStarsMy);
            repoForksMy = itemView.findViewById(R.id.repoForksMy);
            repoOpenIssuesCountMy = itemView.findViewById(R.id.repoOpenIssuesCountMy);
            ImageView reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);

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
                                    intent.putExtra("repoFullNameForStars", fullNameMy.getText());
                                    context.startActivity(intent);
                                    break;

                                case R.id.repoWatchers:

                                    Intent intentW = new Intent(context, RepoWatchersActivity.class);
                                    intentW.putExtra("repoFullNameForWatchers", fullNameMy.getText());
                                    context.startActivity(intentW);
                                    break;

                                case R.id.repoOpenInBrowser:

                                    Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
                                    intentOpenInBrowser.putExtra("repoFullNameBrowser", fullNameMy.getText());
                                    context.startActivity(intentOpenInBrowser);
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
                Picasso.get().load(currentItem.getAvatar_url()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.imageMy);
            } else {
                holder.imageMy.setImageDrawable(drawable);
            }
        }
        else {
            holder.imageMy.setImageDrawable(drawable);
        }

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
        holder.repoForksMy.setText(currentItem.getForks_count());
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
