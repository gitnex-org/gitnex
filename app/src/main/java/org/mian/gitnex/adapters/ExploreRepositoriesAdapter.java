package org.mian.gitnex.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class ExploreRepositoriesAdapter extends RecyclerView.Adapter<ExploreRepositoriesAdapter.ReposSearchViewHolder> {


    private List<UserRepositories> searchedReposList;
    private Context mCtx;

    public ExploreRepositoriesAdapter(List<UserRepositories> dataList, Context mCtx) {
        this.mCtx = mCtx;
        this.searchedReposList = dataList;
    }

    static class ReposSearchViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView mTextView1;
        private TextView mTextView2;
        private TextView fullName;
        private ImageView repoPrivatePublic;
        private TextView repoStars;
        private TextView repoForks;
        private TextView repoOpenIssuesCount;

        private ReposSearchViewHolder(View itemView) {
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Context context = v.getContext();
                    TextView repoFullName = v.findViewById(R.id.repoFullName);

                    Intent intent = new Intent(context, RepoDetailActivity.class);
                    intent.putExtra("repoFullName", repoFullName.getText().toString());

                    TinyDB tinyDb = new TinyDB(context);
                    tinyDb.putString("repoFullName", repoFullName.getText().toString());
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
                        Objects.requireNonNull(menuHelper).getClass().getDeclaredMethod("setForceShowIcon",
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

                                    Intent intentW = new Intent(context, RepoWatchersActivity.class);
                                    intentW.putExtra("repoFullNameForWatchers", fullName.getText());
                                    context.startActivity(intentW);
                                    break;

                                case R.id.repoOpenInBrowser:

                                    Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
                                    intentOpenInBrowser.putExtra("repoFullNameBrowser", fullName.getText());
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

    @NonNull
    @Override
    public ExploreRepositoriesAdapter.ReposSearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.repos_list, parent, false);
        return new ExploreRepositoriesAdapter.ReposSearchViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ExploreRepositoriesAdapter.ReposSearchViewHolder holder, int position) {

        final UserRepositories currentItem = searchedReposList.get(position);


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
                Picasso.get().load(currentItem.getAvatar_url()).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.image);
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
        }
        else {
            holder.repoPrivatePublic.setImageResource(R.drawable.ic_public);
        }
        holder.repoStars.setText(currentItem.getStars_count());
        holder.repoForks.setText(currentItem.getForks_count());
        holder.repoOpenIssuesCount.setText(currentItem.getOpen_issues_count());

    }

    @Override
    public int getItemCount() {
        return searchedReposList.size();
    }
}
