package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.database.models.Bookmark;
import org.mian.gitnex.databinding.ListBookmarksBinding;

/**
 * @author mmarif
 */
public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.BookmarkHolder> {

	private final Context context;
	private List<Bookmark> bookmarks;
	private final OnBookmarkAction listener;

	public interface OnBookmarkAction {
		void onOpen(Bookmark bookmark);

		void onRemove(Bookmark bookmark);
	}

	public BookmarksAdapter(Context context, List<Bookmark> bookmarks, OnBookmarkAction listener) {
		this.context = context;
		this.bookmarks = bookmarks;
		this.listener = listener;
	}

	@NonNull @Override
	public BookmarkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new BookmarkHolder(
				ListBookmarksBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull BookmarkHolder holder, int position) {
		holder.bind(bookmarks.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return bookmarks.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Bookmark> newList) {
		this.bookmarks = newList;
		notifyDataSetChanged();
	}

	public class BookmarkHolder extends RecyclerView.ViewHolder {
		private final ListBookmarksBinding binding;

		BookmarkHolder(ListBookmarksBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(Bookmark bookmark) {
			String type = bookmark.getType();
			String action = bookmark.getBookmarkAction();
			String identifier = bookmark.getIdentifier();
			String title = bookmark.getTitle();
			String subtitle;
			int iconRes;

			if (action != null && !action.isEmpty()) {
				if ("wiki_page".equals(action)) {
					title = bookmark.getTitle();
					subtitle =
							context.getString(R.string.wiki_page)
									+ " · "
									+ bookmark.getOwner()
									+ "/"
									+ bookmark.getRepo();
					iconRes = R.drawable.ic_wiki;
				} else if (action.startsWith("create_")) {
					title = getActionLabel(action);
					subtitle = bookmark.getOwner() + "/" + bookmark.getRepo();
					iconRes = R.drawable.ic_action;
				} else {
					title = getActionLabel(action);
					subtitle = bookmark.getOwner() + "/" + bookmark.getRepo();
					iconRes = R.drawable.ic_bookmark;
				}
			} else {
				switch (type) {
					case "repo" -> {
						subtitle =
								context.getString(R.string.repository)
										+ " · "
										+ bookmark.getOwner()
										+ "/"
										+ bookmark.getRepo();
						iconRes = R.drawable.ic_repo;
					}
					case "issue" -> {
						if (identifier != null && !identifier.isEmpty()) {
							title = "#" + identifier + " " + title;
						}
						subtitle =
								context.getString(R.string.issue)
										+ " · "
										+ bookmark.getOwner()
										+ "/"
										+ bookmark.getRepo();
						iconRes = R.drawable.ic_issue;
					}
					case "pr" -> {
						if (identifier != null && !identifier.isEmpty()) {
							title = "#" + identifier + " " + title;
						}
						subtitle =
								context.getString(R.string.pullRequest)
										+ " · "
										+ bookmark.getOwner()
										+ "/"
										+ bookmark.getRepo();
						iconRes = R.drawable.ic_pull_request;
					}
					case "profile" -> {
						subtitle =
								context.getString(R.string.profile) + " · " + bookmark.getOwner();
						iconRes = R.drawable.ic_person;
					}
					case "org" -> {
						subtitle =
								context.getString(R.string.organization)
										+ " · "
										+ bookmark.getOwner();
						iconRes = R.drawable.ic_organization;
					}
					default -> {
						subtitle = "";
						iconRes = R.drawable.ic_bookmarks;
					}
				}
			}

			binding.bookmarkTitle.setText(title);
			binding.bookmarkSubtitle.setText(subtitle);
			binding.bookmarkIcon.setImageResource(iconRes);

			binding.getRoot().setOnClickListener(v -> listener.onOpen(bookmark));
			binding.btnRemove.setOnClickListener(v -> listener.onRemove(bookmark));
		}

		private String getActionLabel(String action) {
			return switch (action) {
				case "create_issue" -> context.getString(R.string.create_issue);
				case "create_pr" -> context.getString(R.string.create_pr);
				case "create_file" -> context.getString(R.string.pageTitleNewFile);
				case "issues" -> context.getString(R.string.pageTitleIssues);
				case "pulls" -> context.getString(R.string.tabPullRequests);
				case "milestones" -> context.getString(R.string.milestones);
				case "labels" -> context.getString(R.string.newIssueLabelsTitle);
				case "files" -> context.getString(R.string.tabTextFiles);
				case "wiki" -> context.getString(R.string.wiki);
				case "releases" -> context.getString(R.string.tabTextReleases);
				case "tags" -> context.getString(R.string.tags);
				case "wiki_page" -> context.getString(R.string.wiki_page);
				default -> action;
			};
		}
	}
}
