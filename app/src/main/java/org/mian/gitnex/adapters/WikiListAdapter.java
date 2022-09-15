package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.gitnex.tea4j.v2.models.WikiPageMetaData;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.WikiActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentWikiBinding;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class WikiListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context ctx;
	private final FragmentWikiBinding fragmentWikiBinding;
	private final String repoOwner;
	private final String repoName;
	private List<WikiPageMetaData> wikiList;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public WikiListAdapter(List<WikiPageMetaData> wikiListMain, Context ctx, String repoOwner, String repoName, FragmentWikiBinding fragmentWikiBinding) {
		this.ctx = ctx;
		this.wikiList = wikiListMain;
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.fragmentWikiBinding = fragmentWikiBinding;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(ctx);
		return new WikiListAdapter.WikisHolder(inflater.inflate(R.layout.list_wiki, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		if(position >= getItemCount() - 1 && isMoreDataAvailable && !isLoading && loadMoreListener != null) {
			isLoading = true;
			loadMoreListener.onLoadMore();
		}

		((WikiListAdapter.WikisHolder) holder).bindData(wikiList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return wikiList.size();
	}

	private void updateAdapter(int position) {
		wikiList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, wikiList.size());
	}

	public void setMoreDataAvailable(boolean moreDataAvailable) {
		isMoreDataAvailable = moreDataAvailable;
		if(!isMoreDataAvailable) {
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

	public void updateList(List<WikiPageMetaData> list) {
		wikiList = list;
		notifyDataChanged();
	}

	private void deleteWiki(final String owner, final String repo, final String pageName, int position, final Context context) {

		MaterialAlertDialogBuilder materialAlertDialogBuilder = new MaterialAlertDialogBuilder(context, R.style.ThemeOverlay_Material3_Dialog_Alert);

		materialAlertDialogBuilder.setTitle(String.format(context.getString(R.string.deleteGenericTitle), pageName)).setMessage(context.getString(R.string.deleteWikiPageMessage, pageName))
			.setPositiveButton(R.string.menuDeleteText, (dialog, whichButton) -> RetrofitClient.getApiInterface(context).repoDeleteWikiPage(owner, repo, pageName).enqueue(new Callback<>() {

				@Override
				public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {

					if(response.isSuccessful()) {
						updateAdapter(position);
						Toasty.success(context, context.getString(R.string.wikiPageDeleted));
						if(getItemCount() == 0) {
							fragmentWikiBinding.noData.setVisibility(View.VISIBLE);
						}
					}
					else if(response.code() == 403) {
						Toasty.error(context, context.getString(R.string.authorizeError));
					}
					else {
						Toasty.error(context, context.getString(R.string.genericError));
					}
				}

				@Override
				public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {

					Toasty.error(context, context.getString(R.string.genericError));
				}
			})).setNeutralButton(R.string.cancelButton, null).show();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();

	}

	class WikisHolder extends RecyclerView.ViewHolder {

		private final ImageView avatar;
		private final TextView pageName;
		private final TextView wikiLastUpdatedBy;
		private final ImageView wikiMenu;
		private WikiPageMetaData wikiPageMeta;

		WikisHolder(View itemView) {

			super(itemView);
			pageName = itemView.findViewById(R.id.page_name);
			avatar = itemView.findViewById(R.id.image_avatar);
			wikiLastUpdatedBy = itemView.findViewById(R.id.wiki_last_updated_by);
			wikiMenu = itemView.findViewById(R.id.wiki_menu);

			itemView.setOnClickListener(v -> {

				Intent intent = new Intent(ctx, WikiActivity.class);
				intent.putExtra("pageName", wikiPageMeta.getTitle());
				intent.putExtra(RepositoryContext.INTENT_EXTRA, ((RepoDetailActivity) itemView.getContext()).repository);
				ctx.startActivity(intent);
			});

			wikiMenu.setOnClickListener(v -> {

				Context ctx = v.getContext();

				View view = LayoutInflater.from(ctx).inflate(R.layout.bottom_sheet_wiki_in_list, itemView.findViewById(android.R.id.content), false);

				TextView bottomSheetHeader = view.findViewById(R.id.bottom_sheet_header);
				TextView editWiki = view.findViewById(R.id.edit_wiki);
				TextView deleteWiki = view.findViewById(R.id.delete_wiki);

				bottomSheetHeader.setText(wikiPageMeta.getTitle());

				BottomSheetDialog dialog = new BottomSheetDialog(ctx);
				dialog.setContentView(view);
				dialog.show();

				editWiki.setOnClickListener(v12 -> {

					Intent intent = new Intent(ctx, WikiActivity.class);
					intent.putExtra("pageName", wikiPageMeta.getTitle());
					intent.putExtra("action", "edit");
					intent.putExtra(RepositoryContext.INTENT_EXTRA, ((RepoDetailActivity) itemView.getContext()).repository);
					ctx.startActivity(intent);
					dialog.dismiss();
				});

				deleteWiki.setOnClickListener(v12 -> {

					deleteWiki(repoOwner, repoName, wikiPageMeta.getTitle(), getAbsoluteAdapterPosition(), ctx);
					dialog.dismiss();
				});
			});
		}

		@SuppressLint("SetTextI18n")
		void bindData(WikiPageMetaData wikiPageMetaData) {

			this.wikiPageMeta = wikiPageMetaData;

			pageName.setText(wikiPageMetaData.getTitle());
			wikiLastUpdatedBy.setText(HtmlCompat.fromHtml(ctx.getResources().getString(R.string.wikiAuthor, wikiPageMetaData.getLastCommit().getAuthor().getName(),
				TimeHelper.formatTime(TimeHelper.parseIso8601(wikiPageMetaData.getLastCommit().getAuthor().getDate()), ctx.getResources().getConfiguration().locale)), HtmlCompat.FROM_HTML_MODE_COMPACT));
			this.wikiLastUpdatedBy.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(TimeHelper.parseIso8601(wikiPageMetaData.getLastCommit().getAuthor().getDate())), ctx));

			ColorGenerator generator = ColorGenerator.Companion.getMATERIAL();
			int color = generator.getColor(wikiPageMetaData.getTitle());
			String firstCharacter = String.valueOf(wikiPageMetaData.getTitle().charAt(0));

			TextDrawable drawable = TextDrawable.builder().beginConfig().useFont(Typeface.DEFAULT).fontSize(18).toUpperCase().width(28).height(28).endConfig().buildRoundRect(firstCharacter, color, 14);
			avatar.setImageDrawable(drawable);

			if(!((RepoDetailActivity) ctx).repository.getPermissions().isPush()) {
				wikiMenu.setVisibility(View.GONE);
			}
		}

	}

}
