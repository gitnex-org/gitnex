package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.api.models.contents.RepoGetContentsList;
import org.mian.gitnex.databinding.ListFilesBinding;
import org.mian.gitnex.helpers.FileIcon;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class FilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
		implements Filterable {

	private static final int TYPE_BREADCRUMB = 0;
	private static final int TYPE_FILE = 1;

	private final List<RepoGetContentsList> originalFiles = new ArrayList<>();
	private final List<RepoGetContentsList> alteredFiles = new ArrayList<>();
	private final Context context;
	private final FilesAdapterListener filesListener;

	private String repoName;
	private String[] pathSegments = new String[0];
	private PathNavigationListener pathNavigationListener;

	public interface FilesAdapterListener {
		void onClickFile(RepoGetContentsList file);

		void onMenuClick(RepoGetContentsList file);

		void onSearchFilterCompleted(int count);
	}

	public interface PathNavigationListener {
		void onNavigateToPath(String path);

		void onNavigateToRoot();
	}

	public FilesAdapter(Context ctx, FilesAdapterListener filesListener) {
		this.context = ctx;
		this.filesListener = filesListener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setPathData(String repoName, String[] segments, PathNavigationListener listener) {
		this.repoName = repoName;
		this.pathSegments = segments != null ? segments : new String[0];
		this.pathNavigationListener = listener;
		notifyDataSetChanged();
	}

	private boolean hasBreadcrumb() {
		return pathSegments.length > 0 && repoName != null;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setFiles(List<RepoGetContentsList> newList) {
		this.originalFiles.clear();
		this.originalFiles.addAll(newList);
		this.alteredFiles.clear();
		this.alteredFiles.addAll(newList);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if (hasBreadcrumb() && position == 0) {
			return TYPE_BREADCRUMB;
		}
		return TYPE_FILE;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		if (viewType == TYPE_BREADCRUMB) {
			View view =
					LayoutInflater.from(parent.getContext())
							.inflate(R.layout.item_files_breadcrumb, parent, false);
			return new BreadcrumbViewHolder(view);
		}
		ListFilesBinding binding =
				ListFilesBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
		return new FilesViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if (holder instanceof BreadcrumbViewHolder) {
			((BreadcrumbViewHolder) holder).bind();
		} else if (holder instanceof FilesViewHolder) {
			int fileIndex = hasBreadcrumb() ? position - 1 : position;
			((FilesViewHolder) holder).bind(alteredFiles.get(fileIndex));
			((FilesViewHolder) holder).binding.getRoot().updateAppearance(position, getItemCount());
		}
	}

	@Override
	public int getItemCount() {
		int count = alteredFiles.size();
		if (hasBreadcrumb()) count++;
		return count;
	}

	@Override
	public Filter getFilter() {
		return new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				List<RepoGetContentsList> filteredList = new ArrayList<>();
				if (constraint == null || constraint.length() == 0) {
					filteredList.addAll(originalFiles);
				} else {
					String filterPattern = constraint.toString().toLowerCase().trim();
					for (RepoGetContentsList item : originalFiles) {
						if (item.getName().toLowerCase().contains(filterPattern)
								|| (item.getPath() != null
										&& item.getPath().toLowerCase().contains(filterPattern))) {
							filteredList.add(item);
						}
					}
				}
				FilterResults results = new FilterResults();
				results.values = filteredList;
				return results;
			}

			@SuppressLint("NotifyDataSetChanged")
			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				alteredFiles.clear();
				if (results.values != null) {
					alteredFiles.addAll((List<RepoGetContentsList>) results.values);
				}
				notifyDataSetChanged();
				if (filesListener != null) {
					filesListener.onSearchFilterCompleted(alteredFiles.size());
				}
			}
		};
	}

	class BreadcrumbViewHolder extends RecyclerView.ViewHolder {
		private final TextView breadcrumbText;
		private final com.google.android.material.listitem.ListItemLayout listItemLayout;

		BreadcrumbViewHolder(View itemView) {
			super(itemView);
			breadcrumbText = itemView.findViewById(R.id.breadcrumb_text);
			listItemLayout = (com.google.android.material.listitem.ListItemLayout) itemView;
		}

		void bind() {
			SpannableStringBuilder builder = new SpannableStringBuilder();
			builder.append(repoName);

			builder.setSpan(
					new ClickableSpan() {
						@Override
						public void onClick(@NonNull View widget) {
							if (pathNavigationListener != null) {
								pathNavigationListener.onNavigateToRoot();
							}
						}
					},
					0,
					repoName.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

			for (int i = 0; i < pathSegments.length; i++) {
				String segment = pathSegments[i];
				builder.append(" / ");
				int start = builder.length();
				builder.append(segment);

				if (i < pathSegments.length - 1) {
					final String pathUpToHere = buildPathUpTo(i);
					builder.setSpan(
							new ClickableSpan() {
								@Override
								public void onClick(@NonNull View widget) {
									if (pathNavigationListener != null) {
										pathNavigationListener.onNavigateToPath(pathUpToHere);
									}
								}
							},
							start,
							builder.length(),
							Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}

			breadcrumbText.setText(builder);
			breadcrumbText.setMovementMethod(LinkMovementMethod.getInstance());
			breadcrumbText.setHighlightColor(Color.TRANSPARENT);

			listItemLayout.updateAppearance(0, getItemCount());
		}

		private String buildPathUpTo(int index) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i <= index; i++) {
				if (sb.length() > 0) sb.append("/");
				sb.append(pathSegments[i]);
			}
			return sb.toString();
		}
	}

	public class FilesViewHolder extends RecyclerView.ViewHolder {
		private final ListFilesBinding binding;

		private FilesViewHolder(ListFilesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(RepoGetContentsList item) {
			Locale locale = Locale.getDefault();
			String type = item.getType();
			boolean isFile = "file".equals(type);
			boolean isDir = "dir".equals(type);
			boolean isSymlink = "symlink".equals(type);
			boolean isClickableFile = isFile || isSymlink;
			Date committerDate = item.getCompatibleCommitDate();
			boolean hasDate = committerDate != null;

			binding.fileName.setText(item.getName());
			binding.fileTypeIs.setImageDrawable(
					AppCompatResources.getDrawable(
							context, FileIcon.getIconResource(item.getName(), type)));

			if (isFile || isSymlink) {
				binding.fileInfo.setVisibility(View.VISIBLE);
				binding.fileInfo.setText(
						FileUtils.byteCountToDisplaySize(Math.toIntExact(item.getSize())));
			} else if (isDir) {
				binding.fileInfo.setVisibility(View.VISIBLE);
				binding.fileInfo.setText(context.getString(R.string.directory));
			} else {
				binding.fileInfo.setVisibility(View.GONE);
			}

			if (isDir) {
				binding.ivChevron.setVisibility(View.VISIBLE);
				binding.ivChevron.setImageResource(R.drawable.ic_chevron_right);
				binding.ivChevron.setOnClickListener(null);
				binding.ivChevron.setBackground(null);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
			} else if (isClickableFile) {
				binding.ivChevron.setVisibility(View.VISIBLE);
				binding.ivChevron.setImageResource(R.drawable.ic_dotted_menu);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
				binding.ivChevron.setOnClickListener(v -> filesListener.onMenuClick(item));
			} else {
				binding.ivChevron.setVisibility(View.GONE);
				binding.fileFrame.setOnClickListener(v -> filesListener.onClickFile(item));
			}

			if (hasDate) {
				binding.fileDate.setVisibility(View.VISIBLE);
				binding.fileDate.setText(TimeHelper.formatTime(committerDate, locale));
				binding.fileDate.setOnClickListener(
						v ->
								Toasty.show(
										context,
										TimeHelper.getFullDateTime(
												committerDate, Locale.getDefault())));
			} else {
				binding.fileDate.setVisibility(View.GONE);
			}
		}
	}
}
