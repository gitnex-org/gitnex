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
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
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
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
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
		private LinearLayout dashboardLayoutCardsFrame;
		private MaterialCardView cardLayout;

		private Activity activityObject;

		DashboardHolder(View itemView) {

			super(itemView);
			userAvatar = itemView.findViewById(R.id.user_avatar);
			typeDetails = itemView.findViewById(R.id.type_details);
			typeIcon = itemView.findViewById(R.id.type_icon);
			createdTime = itemView.findViewById(R.id.created_time);
			dashText = itemView.findViewById(R.id.text);
			dashTextFrame = itemView.findViewById(R.id.dash_text_frame);
			dashboardLayoutCardsFrame = itemView.findViewById(R.id.dashboardLayoutCardsFrame);
			cardLayout = itemView.findViewById(R.id.cardLayout);

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

									String[] contentParts =
											activityObject.getContent().split("\\|");
									String id = contentParts[0];

									Intent intentIssueDetail =
											new IssueContext(repo, Integer.parseInt(id), "open")
													.getIntent(context, IssueDetailActivity.class);
									intentIssueDetail.putExtra("openedFromLink", "true");

									itemView.setOnClickListener(
											v -> {
												repo.saveToDB(context);
												context.startActivity(intentIssueDetail);
											});
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

									String[] contentParts =
											activityObject.getContent().split("\\|");
									String id = contentParts[0];

									Intent intentIssueDetail =
											new IssueContext(repo, Integer.parseInt(id), "open")
													.getIntent(context, IssueDetailActivity.class);
									intentIssueDetail.putExtra("openedFromLink", "true");

									itemView.setOnClickListener(
											v -> {
												repo.saveToDB(context);
												context.startActivity(intentIssueDetail);
											});
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

		void bindData(Activity activity, int position) {

			this.activityObject = activity;
			Locale locale = context.getResources().getConfiguration().locale;

			Glide.with(context)
					.load(activity.getActUser().getAvatarUrl())
					.diskCacheStrategy(DiskCacheStrategy.ALL)
					.placeholder(R.drawable.loader_animated)
					.centerCrop()
					.into(userAvatar);

			String username =
					"<font color='"
							+ ResourcesCompat.getColor(
									context.getResources(), R.color.lightGray, null)
							+ "'>"
							+ activity.getActUser().getLogin()
							+ "</font>";

			String headerString = "";
			String typeString = "";

			if (activity.getOpType().getValue().contains("repo")) {

				if (activity.getOpType().getValue().equalsIgnoreCase("create_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = context.getString(R.string.createdRepository);
					typeIcon.setImageResource(R.drawable.ic_repo);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("rename_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString =
							String.format(
									context.getString(R.string.renamedRepository),
									activity.getContent());
					typeIcon.setImageResource(R.drawable.ic_repo);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("star_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString = context.getString(R.string.starredRepository);
					typeIcon.setImageResource(R.drawable.ic_star);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("transfer_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";
					typeString =
							String.format(
									context.getString(R.string.transferredRepository),
									activity.getContent());
					typeIcon.setImageResource(R.drawable.ic_arrow_up);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("commit_repo")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					if (activity.getContent().isEmpty()) {
						String branch =
								"<font color='"
										+ ResourcesCompat.getColor(
												context.getResources(), R.color.lightGray, null)
										+ "'>"
										+ activity.getRefName()
												.substring(
														activity.getRefName().lastIndexOf("/") + 1)
												.trim()
										+ "</font>";
						typeString =
								String.format(context.getString(R.string.createdBranch), branch);
					} else {
						String branch =
								"<font color='"
										+ ResourcesCompat.getColor(
												context.getResources(), R.color.lightGray, null)
										+ "'>"
										+ activity.getRefName()
												.substring(
														activity.getRefName().lastIndexOf("/") + 1)
												.trim()
										+ "</font>";
						typeString = String.format(context.getString(R.string.pushedTo), branch);

						JSONObject commitsObj = null;
						try {
							commitsObj = new JSONObject(activity.getContent());
						} catch (JSONException ignored) {
						}

						JSONArray commitsShaArray = null;
						try {
							commitsShaArray =
									Objects.requireNonNull(commitsObj).getJSONArray("Commits");
						} catch (JSONException ignored) {
						}

						dashTextFrame.setVisibility(View.VISIBLE);

						dashTextFrame.setOrientation(LinearLayout.VERTICAL);
						dashTextFrame.removeAllViews();

						for (int i = 0; i < Objects.requireNonNull(commitsShaArray).length(); i++) {

							try {

								String timelineCommits =
										"<font color='"
												+ ResourcesCompat.getColor(
														context.getResources(),
														R.color.lightBlue,
														null)
												+ "'>"
												+ StringUtils.substring(
														String.valueOf(commitsShaArray.get(i)),
														9,
														19)
												+ "</font>";

								TextView dynamicCommitTv = new TextView(context);
								dynamicCommitTv.setId(View.generateViewId());

								dynamicCommitTv.setText(
										HtmlCompat.fromHtml(
												timelineCommits, HtmlCompat.FROM_HTML_MODE_LEGACY));

								JSONObject sha1Obj = null;
								try {
									sha1Obj = (JSONObject) commitsShaArray.get(i);
								} catch (JSONException ignored) {
								}

								JSONObject finalSha1Obj = sha1Obj;
								dynamicCommitTv.setOnClickListener(
										v14 -> {
											RepositoryContext repo =
													new RepositoryContext(
															activity.getRepo(), context);

											Intent repoIntent =
													new Intent(context, RepoDetailActivity.class);
											repoIntent.putExtra("goToSection", "yes");
											repoIntent.putExtra("goToSectionType", "commit");
											try {
												assert finalSha1Obj != null;
												repoIntent.putExtra(
														"sha", (String) finalSha1Obj.get("Sha1"));
											} catch (JSONException ignored) {
											}

											repo.saveToDB(context);
											repoIntent.putExtra(
													RepositoryContext.INTENT_EXTRA, repo);

											context.startActivity(repoIntent);
										});

								dashTextFrame.setOrientation(LinearLayout.VERTICAL);
								dashTextFrame.addView(dynamicCommitTv);
							} catch (JSONException ignored) {
							}
						}
					}
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().getValue().contains("issue")) {

				String id;
				String content;
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					id = contentParts[0];
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				} else {
					id = contentParts[0];
				}

				if (activity.getOpType().getValue().equalsIgnoreCase("create_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.openedIssue);
					typeIcon.setImageResource(R.drawable.ic_issue);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("comment_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.commentedOnIssue);
					typeIcon.setImageResource(R.drawable.ic_comment);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("close_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.closedIssue);
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("reopen_issue")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.reopenedIssue);
					typeIcon.setImageResource(R.drawable.ic_reopen);
				}
			} else if (activity.getOpType().getValue().contains("pull")) {

				String id;
				String content;
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					id = contentParts[0];
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				} else {
					id = contentParts[0];
				}

				if (activity.getOpType().getValue().equalsIgnoreCase("create_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.createdPR);
					typeIcon.setImageResource(R.drawable.ic_pull_request);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("close_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.closedPR);
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				} else if (activity.getOpType()
						.getValue()
						.equalsIgnoreCase("reopen_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.reopenedPR);
					typeIcon.setImageResource(R.drawable.ic_reopen);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("merge_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.mergedPR);
					typeIcon.setImageResource(R.drawable.ic_pull_request);
				} else if (activity.getOpType()
						.getValue()
						.equalsIgnoreCase("approve_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.approved);
					typeIcon.setImageResource(R.drawable.ic_done);
				} else if (activity.getOpType()
						.getValue()
						.equalsIgnoreCase("reject_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.suggestedChanges);
					typeIcon.setImageResource(R.drawable.ic_diff);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("comment_pull")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.commentedOnPR);
					typeIcon.setImageResource(R.drawable.ic_comment);
				} else if (activity.getOpType()
						.getValue()
						.equalsIgnoreCase("auto_merge_pull_request")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ context.getResources().getString(R.string.hash)
									+ id
									+ "</font>";
					typeString = context.getString(R.string.autoMergePR);
					typeIcon.setImageResource(R.drawable.ic_issue_closed);
				}
			} else if (activity.getOpType().getValue().contains("branch")) {

				String content;
				String[] contentParts = activity.getContent().split("\\|");
				if (contentParts.length > 1) {
					content = contentParts[1];
					dashTextFrame.setVisibility(View.VISIBLE);
					dashText.setText(EmojiParser.parseToUnicode(content));
				}

				if (activity.getOpType().getValue().equalsIgnoreCase("delete_branch")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = String.format(context.getString(R.string.deletedBranch), branch);
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().getValue().contains("tag")) {

				if (activity.getOpType().getValue().equalsIgnoreCase("push_tag")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = String.format(context.getString(R.string.pushedTag), branch);
					typeIcon.setImageResource(R.drawable.ic_commit);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("delete_tag")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = String.format(context.getString(R.string.deletedTag), branch);
					typeIcon.setImageResource(R.drawable.ic_commit);
				}
			} else if (activity.getOpType().getValue().contains("release")) {

				if (activity.getOpType().getValue().equalsIgnoreCase("publish_release")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					String branch =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRefName()
											.substring(activity.getRefName().lastIndexOf("/") + 1)
											.trim()
									+ "</font>";

					typeString = String.format(context.getString(R.string.releasedBranch), branch);
					typeIcon.setImageResource(R.drawable.ic_tag);
				}
			} else if (activity.getOpType().getValue().contains("mirror")) {

				if (activity.getOpType().getValue().equalsIgnoreCase("mirror_sync_push")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString =
							String.format(context.getString(R.string.syncedCommits), headerString);
					typeIcon.setImageResource(R.drawable.ic_tag);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("mirror_sync_create")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString =
							String.format(context.getString(R.string.syncedRefs), headerString);
					typeIcon.setImageResource(R.drawable.ic_tag);
				} else if (activity.getOpType().getValue().equalsIgnoreCase("mirror_sync_delete")) {

					headerString =
							"<font color='"
									+ ResourcesCompat.getColor(
											context.getResources(), R.color.lightGray, null)
									+ "'>"
									+ activity.getRepo().getFullName()
									+ "</font>";

					typeString =
							String.format(
									context.getString(R.string.syncedDeletedRefs), headerString);
					typeIcon.setImageResource(R.drawable.ic_tag);
				}
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
	}
}
