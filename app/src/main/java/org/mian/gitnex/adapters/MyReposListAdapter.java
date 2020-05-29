package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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

        private ImageView imageAvatar;
        private TextView repoName;
        private TextView repoDescription;
        private TextView repoFullName;
        private ImageView repoPrivatePublic;
        private TextView repoStars;
        private TextView repoForks;
        private TextView repoOpenIssuesCount;
        private TextView repoType;
	    private CheckBox isRepoAdmin;

        private MyReposViewHolder(View itemView) {
            super(itemView);
	        repoName = itemView.findViewById(R.id.repoName);
	        repoDescription = itemView.findViewById(R.id.repoDescription);
	        imageAvatar = itemView.findViewById(R.id.imageAvatar);
	        repoFullName = itemView.findViewById(R.id.repoFullName);
            repoPrivatePublic = itemView.findViewById(R.id.imageRepoType);
            repoStars = itemView.findViewById(R.id.repoStars);
            repoForks = itemView.findViewById(R.id.repoForks);
            repoOpenIssuesCount = itemView.findViewById(R.id.repoOpenIssuesCount);
            ImageView reposDropdownMenu = itemView.findViewById(R.id.reposDropdownMenu);
            repoType = itemView.findViewById(R.id.repoType);
	        isRepoAdmin = itemView.findViewById(R.id.repoIsAdmin);

            itemView.setOnClickListener(v -> {

                Context context = v.getContext();

                Intent intent = new Intent(context, RepoDetailActivity.class);
                intent.putExtra("repoFullName", repoFullName.getText().toString());

                TinyDB tinyDb = new TinyDB(context);
                tinyDb.putString("repoFullName", repoFullName.getText().toString());
                tinyDb.putString("repoType", repoType.getText().toString());
                //tinyDb.putBoolean("resumeIssues", true);
	            tinyDb.putBoolean("isRepoAdmin", isRepoAdmin.isChecked());

                //store if user is watching this repo
                {
                    final String instanceUrl = tinyDb.getString("instanceUrl");
                    String[] parts = repoFullName.getText().toString().split("/");
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

                bottomSheetHeader.setText(repoFullName.getText());
                BottomSheetDialog dialog = new BottomSheetDialog(context);
                dialog.setContentView(view);
                dialog.show();

                repoOpenInBrowser.setOnClickListener(openInBrowser -> {

                    Intent intentOpenInBrowser = new Intent(context, OpenRepoInBrowserActivity.class);
                    intentOpenInBrowser.putExtra("repoFullNameBrowser", repoFullName.getText());
                    context.startActivity(intentOpenInBrowser);
                    dialog.dismiss();

                });

                repoStargazers.setOnClickListener(stargazers -> {

                    Intent intent = new Intent(context, RepoStargazersActivity.class);
                    intent.putExtra("repoFullNameForStars", repoFullName.getText());
                    context.startActivity(intent);
                    dialog.dismiss();

                });

                repoWatchers.setOnClickListener(watchers -> {

                    Intent intentW = new Intent(context, RepoWatchersActivity.class);
                    intentW.putExtra("repoFullNameForWatchers", repoFullName.getText());
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_repositories, parent, false);
        return new MyReposListAdapter.MyReposViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyReposListAdapter.MyReposViewHolder holder, int position) {

        UserRepositories currentItem = reposList.get(position);
        holder.repoDescription.setVisibility(View.GONE);

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
                PicassoService.getInstance(mCtx).get().load(currentItem.getAvatar_url()).placeholder(R.drawable.loader_animated).transform(new RoundedTransformation(8, 0)).resize(120, 120).centerCrop().into(holder.imageAvatar);
            } else {
                holder.imageAvatar.setImageDrawable(drawable);
            }
        }
        else {
            holder.imageAvatar.setImageDrawable(drawable);
        }

        holder.repoName.setText(currentItem.getName());
        if (!currentItem.getDescription().equals("")) {
            holder.repoDescription.setVisibility(View.VISIBLE);
            holder.repoDescription.setText(currentItem.getDescription());
        }
        holder.repoFullName.setText(currentItem.getFullname());
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

	    if(holder.isRepoAdmin == null) {
		    holder.isRepoAdmin = new CheckBox(mCtx);
	    }
	    holder.isRepoAdmin.setChecked(currentItem.getPermissions().isAdmin());

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
