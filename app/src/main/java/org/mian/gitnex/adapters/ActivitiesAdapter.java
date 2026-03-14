package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.card.MaterialCardView;
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
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class ActivitiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	TinyDB tinyDb;
	private List<Activity> activityList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;
	private Intent intent;
	public boolean isUserOrg = false;

	public ActivitiesAdapter(List<Activity> dataList, Context ctx) {
		this.context = ctx;
		this.activityList = dataList;
		this.tinyDb = TinyDB.getInstance(ctx);
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new ActivitiesAdapter.DashboardHolder(
				inflater.inflate(R.layout.list_activities, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (position >= getItemCount() - 1
				&& isMoreDataAvailable
				&& !isLoading
				&& loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((ActivitiesAdapter.DashboardHolder) holder).bindData(activityList.get(position), position);
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return activityList.size();
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if (!isMoreDataAvailable) {
			loadMoreListener.onLoadFinished();
		}
	}

	@SuppressLint("NotifyDataSetChanged")
	public void notifyDataChanged() {
		notifyDataSetChanged();
		isLoading = false;
		loadMoreListener.onLoadFinished();
	}

	public void setLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.loadMoreListener = loadMoreListener;
	}

	public void updateList(List<Activity> list) {
		activityList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class DashboardHolder extends RecyclerView.ViewHolder {

		private final ImageView userAvatar;
		private final TextView typeDetails;
		private final TextView createdTime;
		private final ImageView typeIcon;
		private final TextView dashText;
		private final LinearLayout dashTextFrame;

		private Activity activityObject;

		DashboardHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.user_avatar);
			typeDetails = itemView.findViewById(R.id.type_details);
			typeIcon = itemView.findViewById(R.id.type_icon);
			createdTime = itemView.findViewById(R.id.created_time);
			dashText = itemView.findViewById(R.id.text);
			dashTextFrame = itemView.findViewById(R.id.dash_text_frame);
			LinearLayout dashboardLayoutCardsFrame =
					itemView.findViewById(R.id.dashboardLayoutCardsFrame);
			MaterialCardView cardLayout = itemView.findViewById(R.id.cardLayout);

			new Handler()
					.postDelayed(
							() -> {
								if (!AppUtil.checkGhostUsers(
										activityObject.getActUser().getLogin())) {

									userAvatar.setOnLongClickListener(
											loginId -> {
												AppUtil.copyToClipboard(
														context,
														activityObject.getActUser().getLogin(),
														context.getString(
																R.string.copyLoginIdToClipBoard,
																activityObject
																		.getActUser()
																		.getLogin()));
												return true;
											});

									userAvatar.setOnClickListener(
											v -> {
												intent = new Intent(context, ProfileActivity.class);
												intent.putExtra(
														"username",
														activityObject.getActUser().getLogin());
												context.startActivity(intent);
											});
								}

								if (activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("create_repo")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("rename_repo")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("star_repo")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("transfer_repo")) {

									itemView.setOnClickListener(
											v -> {
												Context context = v.getContext();
												RepositoryContext repo =
														new RepositoryContext(
																activityObject.getRepo(), context);
												repo.saveToDB(context);
												Intent intent =
														repo.getIntent(
																context, RepoDetailActivity.class);
												if (isUserOrg) {
													intent.putExtra("openedFromUserOrg", true);
												}
												context.startActivity(intent);
											});
								}

								if (activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("create_issue")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("comment_issue")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("close_issue")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("reopen_issue")) {

									String[] parts =
											activityObject.getRepo().getFullName().split("/");
									final String repoOwner = parts[0];
									final String repoName = parts[1];

									RepositoryContext repo =
											new RepositoryContext(repoOwner, repoName, context);

									String id = getString();

									try {
										Intent intentIssueDetail =
												new IssueContext(repo, Integer.parseInt(id), "open")
														.getIntent(
																context, IssueDetailActivity.class);
										intentIssueDetail.putExtra("openedFromLink", "true");

										itemView.setOnClickListener(
												v -> {
													repo.saveToDB(context);
													context.startActivity(intentIssueDetail);
												});
									} catch (NumberFormatException e) {
										itemView.setOnClickListener(null);
									}
								}

								if (activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("create_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("close_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("reopen_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("approve_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("reject_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("comment_pull")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("auto_merge_pull_request")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("merge_pull_request")) {

									String[] parts =
											activityObject.getRepo().getFullName().split("/");
									final String repoOwner = parts[0];
									final String repoName = parts[1];

									RepositoryContext repo =
											new RepositoryContext(repoOwner, repoName, context);

									String id = getString();

									try {
										Intent intentIssueDetail =
												new IssueContext(repo, Integer.parseInt(id), "open")
														.getIntent(
																context, IssueDetailActivity.class);
										intentIssueDetail.putExtra("openedFromLink", "true");

										itemView.setOnClickListener(
												v -> {
													repo.saveToDB(context);
													context.startActivity(intentIssueDetail);
												});
									} catch (NumberFormatException e) {
										itemView.setOnClickListener(null);
									}
								}

								if (activityObject
										.getOpType()
										.getValue()
										.equalsIgnoreCase("commit_repo")) {

									if (activityObject.getContent().isEmpty()) {

										itemView.setOnClickListener(
												v -> {
													RepositoryContext repo =
															new RepositoryContext(
																	activityObject.getRepo(),
																	context);

													Intent repoIntent =
															new Intent(
																	context,
																	RepoDetailActivity.class);
													repoIntent.putExtra("goToSection", "yes");
													repoIntent.putExtra(
															"goToSectionType", "commitsList");
													repoIntent.putExtra(
															"branchName",
															activityObject
																	.getRefName()
																	.substring(
																			activityObject
																							.getRefName()
																							.lastIndexOf(
																									"/")
																					+ 1)
																	.trim());

													repo.saveToDB(context);
													repoIntent.putExtra(
															RepositoryContext.INTENT_EXTRA, repo);

													context.startActivity(repoIntent);
												});
									}
								}

								if (activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("publish_release")
										|| activityObject
												.getOpType()
												.getValue()
												.equalsIgnoreCase("push_tag")) {

									itemView.setOnClickListener(
											v -> {
												RepositoryContext repo =
														new RepositoryContext(
																activityObject.getRepo(), context);

												Intent repoIntent =
														new Intent(
																context, RepoDetailActivity.class);
												repoIntent.putExtra("goToSection", "yes");
												repoIntent.putExtra("goToSectionType", "releases");
												repoIntent.putExtra(
														"releaseTagName",
														activityObject
																.getRefName()
																.substring(
																		activityObject
																						.getRefName()
																						.lastIndexOf(
																								"/")
																				+ 1)
																.trim());

												repo.saveToDB(context);
												repoIntent.putExtra(
														RepositoryContext.INTENT_EXTRA, repo);

												context.startActivity(repoIntent);
											});
								}
							},
							200);
		}

		private static class ExtractedData {
			String id = "";
			String content = "";
		}

		private String getString() {
			String content = activityObject.getContent();
			ExtractedData data = extractIdAndContent(content);
			return data.id;
		}

		private String cleanRawContent(String input) {
			if (input == null) return "";

			String cleaned = input.trim();

			if (cleaned.startsWith("\"")) {
				cleaned = cleaned.substring(1);
			}
			if (cleaned.endsWith("\"")) {
				cleaned = cleaned.substring(0, cleaned.length() - 1);
			}

			cleaned = unescapeJson(cleaned);
			cleaned = cleaned.replace("```", "");
			cleaned = cleaned.replace("`", "");

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
						case '\\' -> {
							result.append('\\');
							i += 2;
							continue;
						}
						case '"' -> {
							result.append('"');
							i += 2;
							continue;
						}
						case '\'' -> {
							result.append('\'');
							i += 2;
							continue;
						}
						case 'n' -> {
							result.append('\n');
							i += 2;
							continue;
						}
						case 'r' -> {
							result.append('\r');
							i += 2;
							continue;
						}
						case 't' -> {
							result.append('\t');
							i += 2;
							continue;
						}
						case 'u' -> {
							if (i + 5 < input.length()) {
								try {
									String hex = input.substring(i + 2, i + 6);
									int code = Integer.parseInt(hex, 16);
									result.append((char) code);
									i += 6;
									continue;
								} catch (NumberFormatException ignored) {
								}
							}
						}
					}
					result.append('\\').append(next);
					i += 2;
					continue;
				}
				result.append(c);
				i++;
			}
			return result.toString();
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
						String content = parts[1];
						if (content.endsWith("\"")) {
							content = content.substring(0, content.length() - 1);
						}
						data.content = cleanRawContent(content);
					}
				} catch (Exception e) {
					data.id = "0";
				}
			} else {
				String[] parts = trimmed.split("\\|");
				data.id = cleanRawContent(parts[0]);
				if (parts.length > 1) {
					data.content = cleanRawContent(parts[1]);
				}
			}
			return data;
		}

		void bindData(Activity activity, int position) {
			this.activityObject = activity;
			Locale locale = context.getResources().getConfiguration().getLocales().get(0);

			Glide.with(context)
					.load(activity.getActUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(userAvatar);

			int lightGray =
					ResourcesCompat.getColor(context.getResources(), R.color.lightGray, null);
			String grayHex = String.format("#%06X", (0xFFFFFF & lightGray));
			String username =
					"<font color='" + grayHex + "'>" + activity.getActUser().getLogin() + "</font>";

			String headerString = "";
			String typeString = "";
			String opType = activity.getOpType().getValue().toLowerCase();

			dashTextFrame.setVisibility(View.GONE);
			dashText.setVisibility(View.VISIBLE);

			if (opType.contains("repo")) {
				headerString =
						"<font color='"
								+ grayHex
								+ "'>"
								+ activity.getRepo().getFullName()
								+ "</font>";
				switch (opType) {
					case "create_repo" -> {
						typeString = context.getString(R.string.createdRepository);
						typeIcon.setImageResource(R.drawable.ic_repo);
					}
					case "rename_repo" -> {
						typeString =
								String.format(
										context.getString(R.string.renamedRepository),
										activity.getContent());
						typeIcon.setImageResource(R.drawable.ic_repo);
					}
					case "star_repo" -> {
						typeString = context.getString(R.string.starredRepository);
						typeIcon.setImageResource(R.drawable.ic_star);
					}
					case "transfer_repo" -> {
						typeString =
								String.format(
										context.getString(R.string.transferredRepository),
										activity.getContent());
						typeIcon.setImageResource(R.drawable.ic_arrow_up);
					}
					case "commit_repo" -> {
						handleCommitRepo(activity, grayHex);
						typeString =
								String.format(
										context.getString(R.string.pushedTo),
										getShortBranch(activity.getRefName(), grayHex));
						typeIcon.setImageResource(R.drawable.ic_commit);
					}
				}

			} else if (opType.contains("issue") || opType.contains("pull")) {
				ExtractedData data = extractIdAndContent(activity.getContent());
				headerString =
						"<font color='"
								+ grayHex
								+ "'>"
								+ activity.getRepo().getFullName()
								+ context.getString(R.string.hash)
								+ data.id
								+ "</font>";

				if (!data.content.isEmpty()) {
					dashTextFrame.setVisibility(View.VISIBLE);
					Markdown.render(context, EmojiParser.parseToUnicode(data.content), dashText);
				}

				if (opType.contains("issue")) {
					switch (opType) {
						case "create_issue" -> {
							typeString = context.getString(R.string.openedIssue);
							typeIcon.setImageResource(R.drawable.ic_issue);
						}
						case "comment_issue" -> {
							typeString = context.getString(R.string.commentedOnIssue);
							typeIcon.setImageResource(R.drawable.ic_comment);
						}
						case "close_issue" -> {
							typeString = context.getString(R.string.closedIssue);
							typeIcon.setImageResource(R.drawable.ic_issue_closed);
						}
						default -> { // reopen_issue
							typeString = context.getString(R.string.reopenedIssue);
							typeIcon.setImageResource(R.drawable.ic_refresh);
						}
					}
				} else { // Pull Requests
					switch (opType) {
						case "create_pull_request" -> {
							typeString = context.getString(R.string.createdPR);
							typeIcon.setImageResource(R.drawable.ic_pull_request);
						}
						case "close_pull_request" -> {
							typeString = context.getString(R.string.closedPR);
							typeIcon.setImageResource(R.drawable.ic_issue_closed);
						}
						case "reopen_pull_request" -> {
							typeString = context.getString(R.string.reopenedPR);
							typeIcon.setImageResource(R.drawable.ic_refresh);
						}
						case "merge_pull_request" -> {
							typeString = context.getString(R.string.mergedPR);
							typeIcon.setImageResource(R.drawable.ic_pull_request);
						}
						case "approve_pull_request" -> {
							typeString = context.getString(R.string.approved);
							typeIcon.setImageResource(R.drawable.ic_done);
						}
						case "reject_pull_request" -> {
							typeString = context.getString(R.string.suggestedChanges);
							typeIcon.setImageResource(R.drawable.ic_diff);
						}
						case "comment_pull" -> {
							typeString = context.getString(R.string.commentedOnPR);
							typeIcon.setImageResource(R.drawable.ic_comment);
						}
						case "auto_merge_pull_request" -> {
							typeString = context.getString(R.string.autoMergePR);
							typeIcon.setImageResource(R.drawable.ic_issue_closed);
						}
					}
				}

			} else if (opType.contains("branch")
					|| opType.contains("tag")
					|| opType.contains("release")) {
				String branch = getShortBranch(activity.getRefName(), grayHex);
				headerString =
						"<font color='"
								+ grayHex
								+ "'>"
								+ activity.getRepo().getFullName()
								+ "</font>";

				if (opType.contains("release")) {
					typeString = String.format(context.getString(R.string.releasedBranch), branch);
					typeIcon.setImageResource(R.drawable.ic_tag);
				} else if (opType.contains("delete")) {
					typeString = String.format(context.getString(R.string.deletedBranch), branch);
					typeIcon.setImageResource(R.drawable.ic_commit);
				} else { // push_tag, etc
					typeString = String.format(context.getString(R.string.pushedTag), branch);
					typeIcon.setImageResource(R.drawable.ic_commit);
				}

			} else if (opType.contains("mirror")) {
				headerString =
						"<font color='"
								+ grayHex
								+ "'>"
								+ activity.getRepo().getFullName()
								+ "</font>";
				typeIcon.setImageResource(R.drawable.ic_tag);
				typeString =
						switch (opType) {
							case "mirror_sync_push" ->
									String.format(
											context.getString(R.string.syncedCommits),
											headerString);
							case "mirror_sync_create" ->
									String.format(
											context.getString(R.string.syncedRefs), headerString);
							case "mirror_sync_delete" ->
									String.format(
											context.getString(R.string.syncedDeletedRefs),
											headerString);
							default -> typeString;
						};
			} else {
				dashTextFrame.setVisibility(View.GONE);
				dashText.setVisibility(View.GONE);
			}

			typeDetails.setText(
					HtmlCompat.fromHtml(
							username + " " + typeString + " " + headerString,
							HtmlCompat.FROM_HTML_MODE_LEGACY));
			this.createdTime.setText(TimeHelper.formatTime(activity.getCreated(), locale));
			this.createdTime.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(activity.getCreated()),
							context));
		}

		private String getShortBranch(String refName, String color) {
			if (refName == null || !refName.contains("/")) return refName != null ? refName : "";
			String name = refName.substring(refName.lastIndexOf("/") + 1).trim();
			return "<font color='" + color + "'>" + name + "</font>";
		}

		private void handleCommitRepo(Activity activity, String blueColor) {
			if (activity.getContent() == null || activity.getContent().isEmpty()) return;
			try {
				JSONObject commitsObj = new JSONObject(activity.getContent());
				JSONArray commitsShaArray = commitsObj.getJSONArray("Commits");

				dashTextFrame.setVisibility(View.VISIBLE);
				dashTextFrame.setOrientation(LinearLayout.VERTICAL);
				dashTextFrame.removeAllViews();

				for (int i = 0; i < commitsShaArray.length(); i++) {
					JSONObject commitItem = commitsShaArray.getJSONObject(i);
					String sha = commitItem.getString("Sha1");
					String displaySha = sha.length() > 10 ? sha.substring(0, 10) : sha;

					TextView dynamicCommitTv = new TextView(context);
					dynamicCommitTv.setText(
							HtmlCompat.fromHtml(
									"<font color='" + blueColor + "'>" + displaySha + "</font>",
									HtmlCompat.FROM_HTML_MODE_LEGACY));

					dynamicCommitTv.setOnClickListener(
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
					dashTextFrame.addView(dynamicCommitTv);
				}
			} catch (JSONException ignored) {
			}
		}
	}
}
