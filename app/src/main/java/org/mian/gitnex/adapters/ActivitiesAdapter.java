package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vdurmont.emoji.EmojiParser;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Activity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.databinding.ListActivitiesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesAdapter.DashboardHolder> {

	private final Context context;
	private List<Activity> activityList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false;
	private boolean isMoreDataAvailable = true;
	public boolean isUserOrg = false;

	public ActivitiesAdapter(List<Activity> dataList, Context ctx) {
		this.context = ctx;
		this.activityList = dataList;
	}

	@NonNull @Override
	public DashboardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListActivitiesBinding binding =
				ListActivitiesBinding.inflate(LayoutInflater.from(context), parent, false);
		return new DashboardHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull DashboardHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}
		holder.bindData(activityList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return activityList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Activity> list) {
		this.activityList = list;
		this.isLoading = false;
		notifyDataSetChanged();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		this.isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable && loadMoreListener != null) {
			loadMoreListener.onLoadFinished();
		}
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public interface OnLoadMoreListener {
		void onLoadMore();

		void onLoadFinished();
	}

	public class DashboardHolder extends RecyclerView.ViewHolder {
		private final ListActivitiesBinding binding;
		private Activity activity;

		DashboardHolder(ListActivitiesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bindData(Activity activity) {
			this.activity = activity;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			Glide.with(context)
					.load(activity.getActUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(binding.userAvatar);

			setupUserActions(activity);

			for (int i = binding.dashTextFrame.getChildCount() - 1; i >= 0; i--) {
				View child = binding.dashTextFrame.getChildAt(i);
				if (child.getId() != binding.text.getId()) {
					binding.dashTextFrame.removeViewAt(i);
				}
			}
			binding.dashTextFrame.setVisibility(View.GONE);
			binding.text.setVisibility(View.GONE);
			binding.text.setText("");

			String opType = activity.getOpType().getValue().toLowerCase();
			ExtractedData data = extractIdAndContent(activity.getContent());

			setupTypeAndHeader(opType, data);
			handleContentPreview(opType, data);

			binding.getRoot().setOnClickListener(v -> handleItemClick(opType, data));
			binding.createdTime.setText(TimeHelper.formatTime(activity.getCreated(), locale));
			binding.createdTime.setOnClickListener(
					v ->
							Toasty.show(
									context,
									TimeHelper.getFullDateTime(
											activity.getCreated(), Locale.getDefault())));
		}

		private void setupUserActions(Activity activity) {
			String login = activity.getActUser().getLogin();
			if (AppUtil.checkGhostUsers(login)) return;

			binding.userAvatar.setOnClickListener(
					v -> {
						Intent intent = new Intent(context, ProfileActivity.class);
						intent.putExtra("username", login);
						context.startActivity(intent);
					});

			binding.userAvatar.setOnLongClickListener(
					v -> {
						AppUtil.copyToClipboard(
								context,
								login,
								context.getString(R.string.copyLoginIdToClipBoard, login));
						return true;
					});
		}

		private void setupTypeAndHeader(String opType, ExtractedData data) {

			String login = activity.getActUser().getLogin();
			String repoName = activity.getRepo() != null ? activity.getRepo().getFullName() : "";
			String action = "";
			String metadata = "";
			String branch = getRawBranchName(activity.getRefName());
			String displayBranch = branch.isEmpty() ? "---" : branch;

			switch (opType) {
				case "push_tag":
				case "create_tag":
					binding.typeIcon.setImageResource(R.drawable.ic_tag);
					action = safeFormat(R.string.pushedTag, displayBranch);
					metadata = repoName + " (" + displayBranch + ")";
					break;

				case "delete_tag":
					binding.typeIcon.setImageResource(R.drawable.ic_tag);
					action = safeFormat(R.string.deletedBranch, displayBranch);
					metadata = repoName + " (" + displayBranch + ")";
					break;

				case "publish_release":
				case "create_release":
					binding.typeIcon.setImageResource(R.drawable.ic_tag);
					action = safeFormat(R.string.releasedBranch, displayBranch);
					metadata = repoName + " (" + displayBranch + ")";
					break;

				case "commit_repo":
				case "push_repo":
					binding.typeIcon.setImageResource(R.drawable.ic_commit);
					action = safeFormat(R.string.pushedTo, displayBranch);
					metadata = repoName + " (" + displayBranch + ")";
					break;

				case "create_branch":
					binding.typeIcon.setImageResource(R.drawable.ic_fork);
					action = context.getString(R.string.createdBranch) + " " + displayBranch;
					metadata = repoName;
					break;

				case "delete_branch":
					binding.typeIcon.setImageResource(R.drawable.ic_commit);
					action = safeFormat(R.string.deletedBranch, displayBranch);
					metadata = repoName;
					break;

				case "star_repo":
					binding.typeIcon.setImageResource(R.drawable.ic_star);
					action = context.getString(R.string.starredRepository);
					metadata = repoName;
					break;

				case "fork_repo":
					binding.typeIcon.setImageResource(R.drawable.ic_fork);
					action = context.getString(R.string.forked_repository);
					metadata = repoName;
					break;

				case "comment_issue":
					binding.typeIcon.setImageResource(R.drawable.ic_comment);
					action = context.getString(R.string.commentedOnIssue);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "comment_pull":
					binding.typeIcon.setImageResource(R.drawable.ic_comment);
					action = context.getString(R.string.commentedOnPR);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "close_issue":
					binding.typeIcon.setImageResource(R.drawable.ic_issue_closed);
					action = context.getString(R.string.closedIssue);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "close_pull_request":
					binding.typeIcon.setImageResource(R.drawable.ic_issue_closed);
					action = context.getString(R.string.closedPR);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "merge_pull_request":
					binding.typeIcon.setImageResource(R.drawable.ic_pull_request);
					action = context.getString(R.string.mergedPR);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "auto_merge_pull_request":
					binding.typeIcon.setImageResource(R.drawable.ic_issue_closed);
					action = context.getString(R.string.autoMergePR);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "approve_pull_request":
					binding.typeIcon.setImageResource(R.drawable.ic_done);
					action = context.getString(R.string.approved);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "reject_pull_request":
					binding.typeIcon.setImageResource(R.drawable.ic_diff);
					action = context.getString(R.string.suggestedChanges);
					metadata = repoName + context.getString(R.string.hash) + data.id;
					break;

				case "rename_repo":
					binding.typeIcon.setImageResource(R.drawable.ic_edit);
					action = safeFormat(R.string.renamedRepository, activity.getContent());
					metadata = repoName;
					break;

				case "transfer_repo":
					binding.typeIcon.setImageResource(R.drawable.ic_arrow_up);
					action = safeFormat(R.string.transferredRepository, activity.getContent());
					metadata = repoName;
					break;

				case "mirror_sync_push":
					binding.typeIcon.setImageResource(R.drawable.ic_refresh);
					action = safeFormat(R.string.syncedCommits, repoName);
					metadata = "";
					break;

				case "mirror_sync_create":
					binding.typeIcon.setImageResource(R.drawable.ic_refresh);
					action = safeFormat(R.string.syncedRefs, repoName);
					metadata = "";
					break;

				case "mirror_sync_delete":
					binding.typeIcon.setImageResource(R.drawable.ic_refresh);
					action = safeFormat(R.string.syncedDeletedRefs, repoName);
					metadata = "";
					break;

				default:
					if (opType.contains("issue")) {
						binding.typeIcon.setImageResource(R.drawable.ic_issue);
						action = context.getString(R.string.openedIssue);
						metadata =
								repoName
										+ (data.id.isEmpty()
												? ""
												: context.getString(R.string.hash) + data.id);
					} else if (opType.contains("pull")) {
						binding.typeIcon.setImageResource(R.drawable.ic_pull_request);
						action = context.getString(R.string.createdPR);
						metadata =
								repoName
										+ (data.id.isEmpty()
												? ""
												: context.getString(R.string.hash) + data.id);
					} else {
						binding.typeIcon.setImageResource(R.drawable.ic_repo);
						action = opType;
						metadata = repoName;
					}
					break;
			}

			String fullText = login + " " + action + " " + metadata;
			SpannableStringBuilder builder = new SpannableStringBuilder(fullText);
			int currentTextColor = binding.typeDetails.getCurrentTextColor();
			int alphaColor = ColorUtils.setAlphaComponent(currentTextColor, 179);

			builder.setSpan(
					new ForegroundColorSpan(alphaColor),
					0,
					login.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			int metadataStart = fullText.lastIndexOf(metadata);
			if (metadataStart != -1) {
				builder.setSpan(
						new ForegroundColorSpan(alphaColor),
						metadataStart,
						fullText.length(),
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			binding.typeDetails.setText(builder);
		}

		private void handleContentPreview(String opType, ExtractedData data) {
			String displayContent = "";

			if (activity.getComment() != null && activity.getComment().getBody() != null) {
				displayContent = activity.getComment().getBody();
			} else if (!data.content.isEmpty()) {
				displayContent = data.content;
			}

			binding.text.setText("");

			if (!displayContent.isEmpty()) {
				binding.dashTextFrame.setVisibility(View.VISIBLE);
				binding.text.setVisibility(View.VISIBLE);
				Markdown.render(context, EmojiParser.parseToUnicode(displayContent), binding.text);
			} else if (opType.equals("commit_repo")) {
				handleCommitRepo();
			} else {
				binding.dashTextFrame.setVisibility(View.GONE);
				binding.text.setVisibility(View.GONE);
			}
		}

		private void handleCommitRepo() {
			if (activity.getContent() == null || activity.getContent().isEmpty()) return;
			try {
				JSONObject commitsObj = new JSONObject(activity.getContent());
				JSONArray commitsArray = commitsObj.getJSONArray("Commits");

				binding.dashTextFrame.setVisibility(View.VISIBLE);
				binding.dashTextFrame.setOrientation(LinearLayout.VERTICAL);

				int currentTextColor = binding.typeDetails.getCurrentTextColor();
				int alphaColor = ColorUtils.setAlphaComponent(currentTextColor, 179);

				for (int i = 0; i < Math.min(commitsArray.length(), 10); i++) {
					JSONObject commitItem = commitsArray.getJSONObject(i);
					String sha = commitItem.getString("Sha1");
					String displaySha = sha.length() > 7 ? sha.substring(0, 7) : sha;
					String message = commitItem.optString("Message", "");

					TextView commitTv = new TextView(context);
					SpannableStringBuilder shaBuilder =
							new SpannableStringBuilder(displaySha + " " + message);
					shaBuilder.setSpan(
							new ForegroundColorSpan(alphaColor),
							0,
							displaySha.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

					commitTv.setText(shaBuilder);
					commitTv.setTextAppearance(
							com.google.android.material.R.style
									.TextAppearance_Material3_LabelMedium);
					commitTv.setPadding(0, 8, 0, 8);

					commitTv.setOnClickListener(
							v -> {
								RepositoryContext repo =
										new RepositoryContext(activity.getRepo(), context);
								Intent intent = new Intent(context, RepoDetailActivity.class);
								intent.putExtra("goToSection", "yes");
								intent.putExtra("goToSectionType", "commit");
								intent.putExtra("sha", sha);
								repo.saveToDB(context);
								intent.putExtra(RepositoryContext.INTENT_EXTRA, repo);
								context.startActivity(intent);
							});

					binding.dashTextFrame.addView(commitTv);
				}
			} catch (JSONException ignored) {
			}
		}

		private void handleItemClick(String opType, ExtractedData data) {
			if (opType.contains("repo") || opType.contains("mirror")) {
				RepositoryContext repo = new RepositoryContext(activity.getRepo(), context);
				repo.saveToDB(context);

				if (opType.equals("commit_repo") && activity.getContent().isEmpty()) {
					Intent intent = new Intent(context, RepoDetailActivity.class);
					intent.putExtra("goToSection", "yes");
					intent.putExtra("goToSectionType", "commitsList");
					intent.putExtra("branchName", getRawBranchName(activity.getRefName()));
					intent.putExtra(RepositoryContext.INTENT_EXTRA, repo);
					context.startActivity(intent);
				} else if (opType.contains("release") || opType.contains("tag")) {
					Intent intent = new Intent(context, RepoDetailActivity.class);
					intent.putExtra("goToSection", "yes");
					intent.putExtra("goToSectionType", "releases");
					intent.putExtra("releaseTagName", getRawBranchName(activity.getRefName()));
					intent.putExtra(RepositoryContext.INTENT_EXTRA, repo);
					context.startActivity(intent);
				} else {
					Intent intent = repo.getIntent(context, RepoDetailActivity.class);
					if (isUserOrg) intent.putExtra("openedFromUserOrg", true);
					context.startActivity(intent);
				}
			} else if (opType.contains("issue") || opType.contains("pull")) {
				try {
					String[] parts = activity.getRepo().getFullName().split("/");
					RepositoryContext repo = new RepositoryContext(parts[0], parts[1], context);
					IssueContext issueCtx =
							new IssueContext(repo, Integer.parseInt(data.id), "open");
					Intent intent = issueCtx.getIntent(context, IssueDetailActivity.class);
					intent.putExtra("openedFromLink", "true");
					if (activity.getCommentId() > 0) {
						intent.putExtra("commentId", String.valueOf(activity.getCommentId()));
					}
					repo.saveToDB(context);
					context.startActivity(intent);
				} catch (Exception ignored) {
				}
			}
		}

		private String getRawBranchName(String refName) {
			if (refName == null || refName.isEmpty()) return "";
			if (refName.contains("/")) {
				return refName.substring(refName.lastIndexOf("/") + 1).trim();
			}
			return refName.trim();
		}

		private String safeFormat(int resId, String arg) {
			String base = context.getString(resId);
			if (base.contains("%s") || base.contains("%1$s")) {
				return String.format(base, arg);
			}
			return base + " " + arg;
		}
	}

	private static class ExtractedData {
		String id = "";
		String content = "";
	}

	private ExtractedData extractIdAndContent(String rawContent) {
		ExtractedData data = new ExtractedData();
		if (rawContent == null || rawContent.isEmpty()) return data;

		String trimmed = rawContent.trim();
		if (trimmed.startsWith("[\"")) {
			try {
				String contentInside = trimmed.substring(1, trimmed.length() - 1);
				String[] parts = contentInside.split("\",\"", 2);
				data.id = cleanRawContent(parts[0]);
				if (parts.length > 1) {
					data.content =
							cleanRawContent(
									parts[1].endsWith("\"")
											? parts[1].substring(0, parts[1].length() - 1)
											: parts[1]);
				}
			} catch (Exception e) {
				data.id = "0";
			}
		} else {
			String[] parts = trimmed.split("\\|");
			data.id = cleanRawContent(parts[0]);
			if (parts.length > 1) data.content = cleanRawContent(parts[1]);
		}
		return data;
	}

	private String cleanRawContent(String input) {
		if (input == null) return "";
		String cleaned = input.trim();
		if (cleaned.startsWith("\"")) cleaned = cleaned.substring(1);
		if (cleaned.endsWith("\"")) cleaned = cleaned.substring(0, cleaned.length() - 1);
		cleaned = unescapeJson(cleaned).replace("```", "").replace("`", "");
		return cleaned.replaceAll("[\\n\\r\\t]+", " ").replaceAll("\\s+", " ").trim();
	}

	private String unescapeJson(String input) {
		StringBuilder result = new StringBuilder();
		int i = 0;
		while (i < input.length()) {
			char c = input.charAt(i);
			if (c == '\\' && i + 1 < input.length()) {
				char next = input.charAt(i + 1);
				switch (next) {
					case '\\' -> result.append('\\');
					case '"' -> result.append('"');
					case '\'' -> result.append('\'');
					case 'n' -> result.append('\n');
					case 'r' -> result.append('\r');
					case 't' -> result.append('\t');
					case 'u' -> {
						if (i + 5 < input.length()) {
							try {
								result.append(
										(char) Integer.parseInt(input.substring(i + 2, i + 6), 16));
								i += 5;
							} catch (NumberFormatException ignored) {
							}
						}
					}
					default -> result.append('\\').append(next);
				}
				i += 2;
				continue;
			}
			result.append(c);
			i++;
		}
		return result.toString();
	}
}
