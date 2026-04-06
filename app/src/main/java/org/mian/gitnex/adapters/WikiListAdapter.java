package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ListWikiBinding;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class WikiListAdapter extends RecyclerView.Adapter<WikiListAdapter.WikisHolder> {

	private final Context context;
	private List<WikiPageMetaData> wikiList;
	private final boolean canEdit;
	private final OnWikiAction onEdit;
	private final OnWikiAction onDelete;
	private final OnWikiAction onClick;
	private final OnWikiAction onMenuClick;

	public interface OnWikiAction {
		void run(WikiPageMetaData wikiPage);
	}

	public WikiListAdapter(
			Context ctx,
			List<WikiPageMetaData> list,
			boolean canEdit,
			OnWikiAction onClick,
			OnWikiAction onEdit,
			OnWikiAction onDelete,
			OnWikiAction onMenuClick) {
		this.context = ctx;
		this.wikiList = list;
		this.canEdit = canEdit;
		this.onClick = onClick;
		this.onEdit = onEdit;
		this.onDelete = onDelete;
		this.onMenuClick = onMenuClick;
	}

	@NonNull @Override
	public WikisHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		return new WikisHolder(
				ListWikiBinding.inflate(LayoutInflater.from(context), parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull WikisHolder holder, int position) {
		holder.bindData(wikiList.get(position));
		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return wikiList.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<WikiPageMetaData> newList) {
		this.wikiList = newList;
		notifyDataSetChanged();
	}

	public class WikisHolder extends RecyclerView.ViewHolder {
		private final ListWikiBinding binding;
		private WikiPageMetaData wikiPageMeta;

		WikisHolder(ListWikiBinding binding) {
			super(binding.getRoot());
			this.binding = binding;

			binding.getRoot().setOnClickListener(v -> onClick.run(wikiPageMeta));

			binding.itemMenu.setVisibility(canEdit ? View.VISIBLE : View.GONE);
			binding.itemMenu.setOnClickListener(v -> onMenuClick.run(wikiPageMeta));
		}

		void bindData(WikiPageMetaData wikiPageMetaData) {
			this.wikiPageMeta = wikiPageMetaData;

			Locale locale = Locale.getDefault();
			binding.pageName.setText(wikiPageMetaData.getTitle());

			Date date =
					TimeHelper.parseIso8601(wikiPageMetaData.getLastCommit().getAuthor().getDate());
			String relativeTime = TimeHelper.formatTime(date, locale);

			binding.wikiLastUpdatedBy.setText(
					context.getResources()
							.getString(
									R.string.wikiAuthor,
									wikiPageMetaData.getLastCommit().getAuthor().getName(),
									relativeTime));
			binding.wikiLastUpdatedBy.setOnClickListener(
					v -> Toasty.show(context, TimeHelper.getFullDateTime(date, locale)));

			binding.imageAvatar.setImageDrawable(
					AvatarGenerator.getLetterAvatar(context, wikiPageMetaData.getTitle(), 44));
		}
	}
}
