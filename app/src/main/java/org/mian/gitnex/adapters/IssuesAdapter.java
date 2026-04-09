package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.databinding.ListIssuesBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class IssuesAdapter extends RecyclerView.Adapter<IssuesAdapter.IssuesHolder> {

	private final Context context;
	private List<Issue> issuesList;
	private final String type;

	public IssuesAdapter(Context ctx, List<Issue> issuesListMain, String type) {
		this.context = ctx;
		this.issuesList = issuesListMain;
		this.type = type;
	}

	@NonNull @Override
	public IssuesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListIssuesBinding binding =
				ListIssuesBinding.inflate(LayoutInflater.from(context), parent, false);

		View itemView = binding.getRoot();

		if (type.equalsIgnoreCase("pinned")) {
			ViewGroup.LayoutParams params = itemView.getLayoutParams();
			params.width = context.getResources().getDimensionPixelSize(R.dimen.dimen380dp);

			if (params instanceof ViewGroup.MarginLayoutParams) {
				((ViewGroup.MarginLayoutParams) params)
						.setMarginEnd(
								context.getResources().getDimensionPixelSize(R.dimen.dimen12dp));
			}
			itemView.setLayoutParams(params);
		}

		return new IssuesHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull IssuesHolder holder, int position) {
		holder.bindData(issuesList.get(position));

		if (type.equalsIgnoreCase("pinned")) {
			holder.binding.getRoot().updateAppearance(0, 1);
		} else {
			holder.binding.getRoot().updateAppearance(position, getItemCount());
		}
	}

	@Override
	public int getItemCount() {
		return issuesList != null ? issuesList.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<Issue> list) {
		issuesList = list;
		notifyDataChanged();
	}

	public class IssuesHolder extends RecyclerView.ViewHolder {
		final ListIssuesBinding binding;

		IssuesHolder(ListIssuesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bindData(Issue issue) {
			Locale locale = Locale.getDefault();

			binding.issueNumber.setText(
					context.getString(R.string.hash_with_text, issue.getNumber()));
			binding.issueCommentsCount.setText(String.valueOf(issue.getComments()));
			binding.issueCreatedTime.setText(TimeHelper.formatTime(issue.getCreatedAt(), locale));
			binding.issueCreatedTime.setOnClickListener(
					v ->
							Toasty.show(
									context,
									TimeHelper.getFullDateTime(issue.getCreatedAt(), locale)));

			if (issue.getMilestone() != null) {
				binding.milestoneLayout.setVisibility(View.VISIBLE);
				binding.milestoneTitle.setText(issue.getMilestone().getTitle());
			} else {
				binding.milestoneLayout.setVisibility(View.GONE);
			}

			if (issue.getComments() > 10) {
				binding.commentIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_flame));
				binding.commentIcon.setColorFilter(
						ContextCompat.getColor(context, R.color.releasePre));
			} else {
				binding.commentIcon.setImageDrawable(
						ContextCompat.getDrawable(context, R.drawable.ic_comment));
				binding.commentIcon.setColorFilter(null);
			}

			Glide.with(context)
					.load(issue.getUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.assigneeAvatar);

			binding.userName.setText(issue.getUser().getLogin());
			binding.repoFullName.setText(
					issue.getRepository() != null ? issue.getRepository().getFullName() : "");

			RepositoryContext repoContext = getRepositoryContext(issue);

			if (repoContext != null) {
				final RepositoryContext finalRepo = repoContext;
				Intent detailIntent =
						new IssueContext(issue, finalRepo)
								.getIntent(context, IssueDetailActivity.class);

				if (!(context instanceof RepoDetailActivity)) {
					detailIntent.putExtra("openedFromLink", "true");
				}

				itemView.setOnClickListener(
						v -> {
							finalRepo.saveToDB(context);
							context.startActivity(detailIntent);
						});
			}

			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(issue.getUser().getLogin())) {
									binding.assigneeAvatar.setOnClickListener(
											v -> {
												Intent i =
														new Intent(context, ProfileActivity.class);
												i.putExtra("username", issue.getUser().getLogin());
												context.startActivity(i);
											});
									binding.assigneeAvatar.setOnLongClickListener(
											v -> {
												AppUtil.copyToClipboard(
														context,
														issue.getUser().getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																issue.getUser().getLogin()));
												return true;
											});
								}
							},
							500);

			Markdown.render(
					context, EmojiParser.parseToUnicode(issue.getTitle()), binding.issueTitle);
			renderLabels(issue);

			if (type.equalsIgnoreCase("pinned")) {
				int strokeColorRes =
						issue.getState().equalsIgnoreCase("open")
								? R.color.darkGreen
								: R.color.iconIssuePrClosedColor;

				binding.cardView.setStrokeWidth(
						context.getResources().getDimensionPixelSize(R.dimen.dimen2dp));
				binding.cardView.setStrokeColor(ContextCompat.getColor(context, strokeColorRes));
			} else {
				binding.cardView.setStrokeWidth(0);
			}
		}

		@Nullable private RepositoryContext getRepositoryContext(Issue issue) {
			RepositoryContext repoContext = null;

			if (issue.getRepository() != null && issue.getRepository().getFullName() != null) {
				String[] parts = issue.getRepository().getFullName().split("/");
				if (parts.length == 2) {
					repoContext = new RepositoryContext(parts[0], parts[1], context);
				}
			} else if (context instanceof RepoDetailActivity) {
				repoContext = ((RepoDetailActivity) context).repository;
			}
			return repoContext;
		}

		private void renderLabels(Issue issue) {
			binding.frameLabels.removeAllViews();
			binding.frameLabelsDots.removeAllViews();

			if (issue.getLabels() == null || issue.getLabels().isEmpty()) {
				binding.labelsScrollViewWithText.setVisibility(View.GONE);
				binding.labelsScrollViewDots.setVisibility(View.GONE);
				return;
			}

			boolean showText =
					Boolean.parseBoolean(
							AppDatabaseSettings.getSettingsValue(
									context, AppDatabaseSettings.APP_LABELS_IN_LIST_KEY));
			binding.labelsScrollViewWithText.setVisibility(showText ? View.VISIBLE : View.GONE);
			binding.labelsScrollViewDots.setVisibility(showText ? View.GONE : View.VISIBLE);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
			params.setMargins(0, 0, 15, 0);

			for (org.gitnex.tea4j.v2.models.Label label : issue.getLabels()) {
				ImageView iv = new ImageView(context);
				iv.setLayoutParams(params);
				int color = Color.parseColor("#" + label.getColor());

				if (showText) {
					iv.setImageDrawable(
							AvatarGenerator.getLabelDrawable(context, label.getName(), color, 20));
					binding.frameLabels.addView(iv);
				} else {
					iv.setImageDrawable(AvatarGenerator.getCircleColorDrawable(context, color, 14));
					binding.frameLabelsDots.addView(iv);
				}
			}
		}
	}
}
