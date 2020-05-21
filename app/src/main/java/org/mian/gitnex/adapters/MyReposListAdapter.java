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
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OpenRepoInBrowserActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.models.WatchInfo;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;

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
        private TextView repoType;

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
            repoType = itemView.findViewById(R.id.repoType);

            itemView.setOnClickListener(v -> {

                Context context = v.getContext();

                Intent intent = new Intent(context, RepoDetailActivity.class);
                intent.putExtra("repoFullName", fullNameMy.getText().toString());

                TinyDB tinyDb = new TinyDB(context);
                tinyDb.putString("repoFullName", fullNameMy.getText().toString());
                tinyDb.putString("repoType", repoType.getText().toString());
                //tinyDb.putBoolean("resumeIssues", true);

                //store if user is watching this repo
                {
                    final String instanceUrl = tinyDb.getString("instanceUrl");
                    String[] parts = fullNameMy.getText().toString().split("/");
                    final String repoOwner = parts[0];
                    final String repoName = parts[1];
                    final String token = "token " + tinyDb.getString(tinyDb.getString("loginUid") + "-token");

                    WatchInfo watch = new WatchInfo();

                    Call<WatchInfo> call;

                    call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface().checkRepoWatchStatus(token, repoOwner, repoName);

                    call.enqueue(new Callback<WatchInfo>() {

                        @Override
                        public void onResponse(@NonNull Call<WatchInfo> call, @NonNull retrofit2.Response<WatchInfo> response) {

                            if(response.isSuccessful()) {

                                assert response.body() != null;
                                tinyDb.putBoolean("repoWatch", response.body().getSubscribed());

                            } else {

                                tinyDb.putBoolean("repoWatch", false);

                                if(response.code() != 404) {

                                    Toasty.info(context, context.getString(R.string.genericApiStatusError));

                                }

                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<WatchInfo> call, @NonNull Throwable t) {

                            tinyDb.putBoolean("repoWatch", false);
                            Toasty.info(context, context.getString(R.string.genericApiStatusError));

                        }
                    });
                }

                context.startActivity(intent);

            });

            reposDropdownMenu.setOnClickListener(v -> {

                final Context context = v.getContext();

                @SuppressLint("InflateParams")
                View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_repository_in_list, null);

                TextView repoOpenInBrowser = view.findViewById(R.id.repoOpenInBrowser);
                TextView repoStargazers = view.findViewById(R.id.repoStargazers);
                TextView repoWatchers = view.findViewById(R.id.repoWatchers);
                TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

                bottomSheetHeader.setText(fullNameMy.getText());
                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(view);
                dialog.show();

                repoOpenInBrowser.setOnClickListener(openInBrowser -> {

                    Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
                    intentOpenInBrowser.putExtra("repoFullNameBrowser", fullNameMy.getText());
                    context.startActivity(intentOpenInBrowser);
                    dialog.dismiss();

                });

                repoStargazers.setOnClickListener(stargazers -> {

                    Intent intent = new Intent(context, RepoStargazersActivity.class);
                    intent.putExtra("repoFullNameForStars", fullNameMy.getText());
                    context.startActivity(intent);
                    dialog.dismiss();

                });

                repoWatchers.setOnClickListener(watchers -> {

                    Intent intentW = new Intent(context, RepoWatchersActivity.class);
                    intentW.putExtra("repoFullNameForWatchers", fullNameMy.getText());
                    context.startActivity(intentW);
                    dialog.dismiss();

                });

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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_my_repos, parent, false);
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
                PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.imageMy);
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
            holder.repoType.setText(R.string.strPrivate);
        }
        else {
            holder.repoPrivatePublicMy.setImageResource(R.drawable.ic_public);
            holder.repoType.setText(R.string.strPublic);
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
