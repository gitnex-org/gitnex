package org.mian.gitnex.helpers;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.PullRequestDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.BookmarksApi;
import org.mian.gitnex.database.models.Bookmark;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class BookmarkHelper {

	public static boolean isBookmarked(
			Context context,
			String type,
			String owner,
			@Nullable String repo,
			@Nullable String bookmarkAction,
			@Nullable String identifier) {
		BookmarksApi api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return false;
		int accountId = ((BaseActivity) context).getAccount().getAccount().getAccountId();
		return api.findByKey(
						accountId,
						type,
						owner != null ? owner : "",
						bookmarkAction != null ? bookmarkAction : "",
						identifier != null ? identifier : "",
						repo != null ? repo : "")
				!= null;
	}

	public static void toggleBookmark(
			Context context,
			String type,
			String owner,
			@Nullable String repo,
			@Nullable String bookmarkAction,
			@Nullable String identifier,
			@Nullable String branch,
			String title,
			@Nullable String url) {
		BookmarksApi api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;
		int accountId = ((BaseActivity) context).getAccount().getAccount().getAccountId();

		Bookmark existing =
				api.findByKey(
						accountId,
						type,
						owner != null ? owner : "",
						bookmarkAction != null ? bookmarkAction : "",
						identifier != null ? identifier : "",
						repo != null ? repo : "");

		if (existing != null) {
			api.delete(existing.getBookmarkId());
			Toasty.show(context, context.getString(R.string.bookmark_removed));
		} else {
			Bookmark bookmark = new Bookmark();
			bookmark.setAccountId(accountId);
			bookmark.setType(type);
			bookmark.setOwner(owner != null ? owner : "");
			bookmark.setRepo(repo != null ? repo : "");
			bookmark.setBookmarkAction(bookmarkAction != null ? bookmarkAction : "");
			bookmark.setIdentifier(identifier != null ? identifier : "");
			bookmark.setBranch(branch != null ? branch : "");
			bookmark.setTitle(title != null ? title : "");
			bookmark.setUrl(url != null ? url : "");
			api.insert(bookmark);
			Toasty.show(context, context.getString(R.string.bookmark_added));
		}
	}

	public static void updateBookmarkTitle(
			Context context,
			String type,
			String owner,
			String repo,
			String bookmarkAction,
			String identifier,
			String newTitle) {
		BookmarksApi api = BaseApi.getInstance(context, BookmarksApi.class);
		if (api == null) return;
		int accountId = ((BaseActivity) context).getAccount().getAccount().getAccountId();
		Bookmark existing =
				api.findByKey(
						accountId,
						type,
						owner,
						bookmarkAction != null ? bookmarkAction : "",
						identifier != null ? identifier : "",
						repo != null ? repo : "");
		if (existing != null && !newTitle.equals(existing.getTitle())) {
			api.update(existing.getBookmarkId(), newTitle, existing.getUrl());
		}
	}

	public static void openBookmark(Context context, Bookmark bookmark) {
		String type = bookmark.getType();
		String owner = bookmark.getOwner();
		String repo = bookmark.getRepo();
		String action = bookmark.getBookmarkAction();
		String identifier = bookmark.getIdentifier();

		if (action != null && !action.isEmpty()) {
			openBookmarkAction(context, action, owner, repo, identifier);
			return;
		}

		switch (type) {
			case "repo":
				openRepo(context, owner, repo);
				break;
			case "issue":
				openIssue(context, owner, repo, identifier);
				break;
			case "pr":
				openPR(context, owner, repo, identifier);
				break;
			case "profile":
				openProfile(context, owner);
				break;
			case "org":
				openOrg(context, owner);
				break;
		}
	}

	private static void openRepo(Context context, String owner, String repo) {
		Intent intent =
				new RepositoryContext(owner, repo, context)
						.getIntent(context, RepoDetailActivity.class);
		context.startActivity(intent);
	}

	private static void openIssue(Context context, String owner, String repo, String identifier) {
		try {
			int number = Integer.parseInt(identifier);
			Intent intent = new Intent(context, IssueDetailActivity.class);
			intent.putExtra("owner", owner);
			intent.putExtra("repo", repo);
			intent.putExtra("issueNumber", number);
			intent.putExtra("fetchIssueObject", true);
			context.startActivity(intent);
		} catch (NumberFormatException ignored) {
		}
	}

	private static void openPR(Context context, String owner, String repo, String identifier) {
		try {
			int number = Integer.parseInt(identifier);
			Intent intent = new Intent(context, PullRequestDetailActivity.class);
			intent.putExtra("owner", owner);
			intent.putExtra("repo", repo);
			intent.putExtra("prNumber", number);
			context.startActivity(intent);
		} catch (NumberFormatException ignored) {
		}
	}

	private static void openProfile(Context context, String owner) {
		Intent intent = new Intent(context, ProfileActivity.class);
		intent.putExtra("username", owner);
		context.startActivity(intent);
	}

	private static void openOrg(Context context, String owner) {
		Intent intent = new Intent(context, OrganizationDetailActivity.class);
		intent.putExtra("orgName", owner);
		context.startActivity(intent);
	}

	private static void openBookmarkAction(
			Context context, String action, String owner, String repo, String identifier) {

		switch (action) {
			case "create_issue":
				Intent intent =
						new RepositoryContext(owner, repo, context)
								.getIntent(context, RepoDetailActivity.class);
				intent.putExtra("goToSection", "issueNew");
				intent.putExtra("goToSectionType", "issueNew");
				context.startActivity(intent);
				break;
			case "create_pr":
				Intent prIntent =
						new RepositoryContext(owner, repo, context)
								.getIntent(context, RepoDetailActivity.class);
				prIntent.putExtra("goToSection", "pullNew");
				prIntent.putExtra("goToSectionType", "pullNew");
				context.startActivity(prIntent);
				break;
			case "create_file":
				Intent fileIntent =
						new RepositoryContext(owner, repo, context)
								.getIntent(context, RepoDetailActivity.class);
				fileIntent.putExtra("goToSection", "fileNew");
				fileIntent.putExtra("goToSectionType", "fileNew");
				context.startActivity(fileIntent);
				break;
			case "wiki_page":
				Intent wikiIntent =
						new RepositoryContext(owner, repo, context)
								.getIntent(context, RepoDetailActivity.class);
				wikiIntent.putExtra("goToSection", "wiki");
				wikiIntent.putExtra("goToSectionType", "wiki");
				wikiIntent.putExtra("wikiPageName", identifier);
				context.startActivity(wikiIntent);
				break;
			default:
				openRepoTab(context, action, owner, repo);
				break;
		}
	}

	private static void openRepoTab(Context context, String tab, String owner, String repo) {
		String sectionType =
				switch (tab) {
					case "issues" -> "issue";
					case "pulls" -> "pull";
					case "files" -> "dir";
					case "milestones" -> "milestones";
					case "labels" -> "labels";
					case "wiki" -> "wiki";
					case "releases", "tags" -> "releases";
					default -> tab;
				};

		Intent intent =
				new RepositoryContext(owner, repo, context)
						.getIntent(context, RepoDetailActivity.class);
		intent.putExtra("goToSection", tab);
		intent.putExtra("goToSectionType", sectionType);
		if ("tags".equals(tab)) {
			intent.putExtra("showTags", true);
		}
		context.startActivity(intent);
	}
}
