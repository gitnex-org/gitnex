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
import org.gitnex.tea4j.v2.models.Issue;
import org.gitnex.tea4j.v2.models.Label;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.databinding.ListPrBinding;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class MyPullRequestsAdapter extends RecyclerView.Adapter<MyPullRequestsAdapter.ViewHolder> {

	private final Context context;
	private List<Issue> prList;
	private OnPrClickListener clickListener;

	public interface OnPrClickListener {
		void onPrClick(Issue issue);
	}

	public void setOnPrClickListener(OnPrClickListener listener) {
		this.clickListener = listener;
	}

	public MyPullRequestsAdapter(Context context, List<Issue> prList) {
		this.context = context;
		this.prList = prList;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListPrBinding binding = ListPrBinding.inflate(LayoutInflater.from(context), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		holder.bindData(prList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return prList != null ? prList.size() : 0;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Issue> list) {
		this.prList = list;
		notifyDataSetChanged();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		final ListPrBinding binding;
		private Issue issue;

		ViewHolder(ListPrBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			View.OnClickListener openPr =
					v -> {
						if (clickListener != null
								&& issue != null
								&& issue.getRepository() != null) {
							clickListener.onPrClick(issue);
						}
					};

			binding.getRoot().setOnClickListener(openPr);
			binding.frameLabels.setOnClickListener(openPr);
			binding.frameLabelsDots.setOnClickListener(openPr);
		}

		void bindData(Issue issue) {
			this.issue = issue;
			Locale locale = Locale.getDefault();

			if (issue.getMilestone() != null) {
				binding.milestoneLayout.setVisibility(View.VISIBLE);
				binding.milestoneTitle.setText(issue.getMilestone().getTitle());
			} else {
				binding.milestoneLayout.setVisibility(View.GONE);
			}

			if (isDraftOrWip(issue)) {
				binding.prStateIcon.setVisibility(View.VISIBLE);
				binding.prStateIcon.setImageResource(R.drawable.ic_draft);
				binding.prStateIcon.setOnClickListener(
						v -> Toasty.show(context, context.getString(R.string.releaseDraftText)));
			} else {
				binding.prStateIcon.setVisibility(View.GONE);
			}

			if (issue.getPullRequest() != null) {
				if (Boolean.TRUE.equals(issue.getPullRequest().isMerged())) {
					binding.mergedBadge.setVisibility(View.VISIBLE);
					int mergedColor =
							ContextCompat.getColor(context, R.color.alert_important_border);
					binding.mergedBadge.setImageDrawable(
							AvatarGenerator.getLabelDrawable(
									context,
									context.getString(R.string.merged).toUpperCase(),
									mergedColor,
									16));
				} else if ("closed".equalsIgnoreCase(issue.getState())) {
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
			}

			User user = issue.getUser();
			if (user != null) {
				binding.userName.setText(user.getLogin());
				setupUserClicks(user);
				loadAvatar(user);
			}

			if (issue.getRepository() != null) {
				binding.repoFullName.setText(issue.getRepository().getFullName());
			}

			binding.prNumber.setText(context.getString(R.string.hash_with_text, issue.getNumber()));
			binding.prCommentsCount.setText(String.valueOf(issue.getComments()));

			if (issue.getCreatedAt() != null) {
				binding.prCreatedTime.setText(TimeHelper.formatTime(issue.getCreatedAt(), locale));
				binding.prCreatedTime.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												issue.getCreatedAt(), Locale.getDefault())));
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

			Markdown.render(context, EmojiParser.parseToUnicode(issue.getTitle()), binding.prTitle);

			renderLabels(issue.getLabels());
		}

		private boolean isDraftOrWip(Issue issue) {
			String title = issue.getTitle().toLowerCase().trim();
			return title.startsWith("[wip]")
					|| title.startsWith("wip:")
					|| title.startsWith("draft:")
					|| title.startsWith("(draft)")
					|| title.startsWith("[draft]")
					|| (issue.getPullRequest() != null
							&& Boolean.TRUE.equals(issue.getPullRequest().isDraft()));
		}

		private void setupUserClicks(User user) {
			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(user.getLogin())) {
									binding.assigneeAvatar.setOnClickListener(
											v -> {
												Intent i =
														new Intent(context, ProfileActivity.class);
												i.putExtra("username", user.getLogin());
												context.startActivity(i);
											});
								}
							},
							500);
		}

		private void loadAvatar(User user) {
			Glide.with(context)
					.load(user.getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.assigneeAvatar);
		}

		private void renderLabels(List<Label> labels) {
			binding.frameLabels.removeAllViews();
			binding.frameLabelsDots.removeAllViews();

			if (labels == null || labels.isEmpty()) {
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

			for (Label label : labels) {
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
