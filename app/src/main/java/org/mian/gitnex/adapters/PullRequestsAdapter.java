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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.databinding.ListPrBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;

/**
 * @author mmarif
 */
public class PullRequestsAdapter
		extends RecyclerView.Adapter<PullRequestsAdapter.PullRequestsHolder> {

	private final Context context;
	private List<PullRequest> prList;

	public PullRequestsAdapter(Context context, List<PullRequest> prListMain) {
		this.context = context;
		this.prList = prListMain;
	}

	@NonNull @Override
	public PullRequestsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListPrBinding binding = ListPrBinding.inflate(LayoutInflater.from(context), parent, false);
		return new PullRequestsHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull PullRequestsHolder holder, int position) {
		holder.bindData(prList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return prList != null ? prList.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
	}

	public void updateList(List<PullRequest> list) {
		prList = list;
		notifyDataChanged();
	}

	public class PullRequestsHolder extends RecyclerView.ViewHolder {
		final ListPrBinding binding;
		private PullRequest prObject;

		PullRequestsHolder(ListPrBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			View.OnClickListener openPr =
					v -> {
						if (context instanceof RepoDetailActivity) {
							Intent intent =
									new IssueContext(
													prObject,
													((RepoDetailActivity) context).repository)
											.getIntent(context, IssueDetailActivity.class);
							context.startActivity(intent);
						}
					};

			binding.getRoot().setOnClickListener(openPr);
			binding.frameLabels.setOnClickListener(openPr);
			binding.frameLabelsDots.setOnClickListener(openPr);
		}

		void bindData(PullRequest pr) {
			this.prObject = pr;
			Locale locale = Locale.getDefault();

			if (pr.getMilestone() != null) {
				binding.milestoneLayout.setVisibility(View.VISIBLE);
				binding.milestoneTitle.setText(pr.getMilestone().getTitle());
			} else {
				binding.milestoneLayout.setVisibility(View.GONE);
			}

			if (isDraftOrWip(pr)) {
				binding.prStateIcon.setVisibility(View.VISIBLE);
				binding.prStateIcon.setImageResource(R.drawable.ic_draft);
				binding.prStateIcon.setOnClickListener(
						v -> {
							Toasty.show(
									context,
									context.getResources().getString(R.string.releaseDraftText));
						});
			} else {
				binding.prStateIcon.setVisibility(View.GONE);
			}

			if (pr.isMerged()) {
				binding.mergedBadge.setVisibility(View.VISIBLE);
				int mergedColor = ContextCompat.getColor(context, R.color.alert_important_border);
				binding.mergedBadge.setImageDrawable(
						AvatarGenerator.getLabelDrawable(
								context,
								context.getString(R.string.merged).toUpperCase(),
								mergedColor,
								16));

			} else if (pr.getState().equalsIgnoreCase("closed")) {
				binding.mergedBadge.setVisibility(View.VISIBLE);
				int closedColor = ContextCompat.getColor(context, R.color.colorRed);
				binding.mergedBadge.setImageDrawable(
						AvatarGenerator.getLabelDrawable(
								context,
								context.getString(R.string.isClosed).toUpperCase(),
								closedColor,
								16));

			} else {
				binding.mergedBadge.setVisibility(View.GONE);
			}

			binding.userName.setText(pr.getUser().getLogin());
			binding.repoFullName.setText(pr.getBase().getRepo().getFullName());
			binding.prNumber.setText(context.getString(R.string.hash_with_text, pr.getNumber()));
			binding.prCommentsCount.setText(String.valueOf(pr.getComments()));
			binding.prCreatedTime.setText(TimeHelper.formatTime(pr.getCreatedAt(), locale));

			binding.prCreatedTime.setOnClickListener(
					v -> {
						Toasty.show(
								context,
								TimeHelper.getFullDateTime(pr.getCreatedAt(), Locale.getDefault()));
					});

			if (pr.getComments() > 10) {
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
					.load(pr.getUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.assigneeAvatar);

			setupUserClicks(pr);
			Markdown.render(context, EmojiParser.parseToUnicode(pr.getTitle()), binding.prTitle);
			renderLabels(pr);
		}

		private boolean isDraftOrWip(PullRequest pr) {
			String title = pr.getTitle().toLowerCase().trim();
			return title.startsWith("[wip]")
					|| title.startsWith("wip:")
					|| title.startsWith("draft:")
					|| title.startsWith("(draft)")
					|| title.startsWith("[draft]")
					|| pr.isDraft();
		}

		private void setupUserClicks(PullRequest pr) {
			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(pr.getUser().getLogin())) {
									binding.assigneeAvatar.setOnClickListener(
											v -> {
												Intent i =
														new Intent(context, ProfileActivity.class);
												i.putExtra("username", pr.getUser().getLogin());
												context.startActivity(i);
											});
								}
							},
							500);
		}

		private void renderLabels(PullRequest pr) {
			binding.frameLabels.removeAllViews();
			binding.frameLabelsDots.removeAllViews();

			if (pr.getLabels() == null || pr.getLabels().isEmpty()) {
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

			for (org.gitnex.tea4j.v2.models.Label label : pr.getLabels()) {
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
