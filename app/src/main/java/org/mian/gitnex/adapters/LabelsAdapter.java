package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.ColorInverter;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author M M Arif
 */
public class LabelsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private final Context context;
	private List<Label> labelsList;
	private final String type;
	private final String orgName;
	private OnLoadMoreListener loadMoreListener;
	private boolean isLoading = false, isMoreDataAvailable = true;

	public LabelsAdapter(Context ctx, List<Label> labelsMain, String type, String orgName) {

		this.context = ctx;
		this.labelsList = labelsMain;
		this.type = type;
		this.orgName = orgName;
	}

	@NonNull @Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(context);
		return new LabelsAdapter.DataHolder(inflater.inflate(R.layout.list_labels, parent, false));
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
		((LabelsAdapter.DataHolder) holder).bindData(labelsList.get(position));
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public int getItemCount() {
		return labelsList.size();
	}

	private void updateAdapter(int position) {
		labelsList.remove(position);
		notifyItemRemoved(position);
		notifyItemRangeChanged(position, labelsList.size());
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

	public void updateList(List<Label> list) {
		labelsList = list;
		notifyDataChanged();
	}

	public interface OnLoadMoreListener {

		void onLoadMore();

		void onLoadFinished();
	}

	class DataHolder extends RecyclerView.ViewHolder {

		private final MaterialCardView labelView;
		private final ImageView labelIcon;
		private final TextView labelName;
		private Label labels;

		DataHolder(View itemView) {

			super(itemView);

			labelView = itemView.findViewById(R.id.labelView);
			labelIcon = itemView.findViewById(R.id.labelIcon);
			labelName = itemView.findViewById(R.id.labelName);
			ImageView labelsOptionsMenu = itemView.findViewById(R.id.labelsOptionsMenu);

			if ((type.equals("repo")
							&& !((RepoDetailActivity) itemView.getContext())
									.repository
									.getPermissions()
									.isPush())
					|| (type.equals("org")
							&& (((OrganizationDetailActivity) itemView.getContext()).permissions
											== null
									|| !((OrganizationDetailActivity) itemView.getContext())
											.permissions.isIsOwner()))) {
				labelsOptionsMenu.setVisibility(View.GONE);
			}
			labelsOptionsMenu.setOnClickListener(
					v -> {
						final Context context = v.getContext();

						@SuppressLint("InflateParams")
						View view =
								LayoutInflater.from(context)
										.inflate(R.layout.bottom_sheet_labels_in_list, null);

						TextView labelMenuEdit = view.findViewById(R.id.labelMenuEdit);
						TextView labelMenuDelete = view.findViewById(R.id.labelMenuDelete);
						TextView bottomSheetHeader = view.findViewById(R.id.bottomSheetHeader);

						bottomSheetHeader.setText(labels.getName());
						BottomSheetDialog dialog = new BottomSheetDialog(context);
						dialog.setContentView(view);
						dialog.show();

						labelMenuEdit.setOnClickListener(
								editLabel -> {
									Intent intent = new Intent(context, CreateLabelActivity.class);
									intent.putExtra("labelId", String.valueOf(labels.getId()));
									intent.putExtra("labelTitle", labels.getName());
									intent.putExtra("labelColor", labels.getColor());
									intent.putExtra("labelAction", "edit");
									intent.putExtra("type", type);
									intent.putExtra("orgName", orgName);
									if (type.equals("repo")) {
										intent.putExtra(
												RepositoryContext.INTENT_EXTRA,
												((RepoDetailActivity) itemView.getContext())
														.repository);
									}
									context.startActivity(intent);
									dialog.dismiss();
								});

						labelMenuDelete.setOnClickListener(
								deleteLabel -> {
									RepositoryContext repo;
									if (type.equals("repo")) {
										repo =
												((RepoDetailActivity) itemView.getContext())
														.repository;
									} else {
										repo = null;
									}

									AlertDialogs.labelDeleteDialog(
											context,
											labels.getName(),
											String.valueOf(labels.getId()),
											type,
											orgName,
											repo);
									dialog.dismiss();
								});
					});
		}

		@SuppressLint("SetTextI18n")
		void bindData(Label dataModel) {

			labels = dataModel;

			String labelColor_ = dataModel.getColor();
			String labelName_ = dataModel.getName();

			int color = Color.parseColor("#" + labelColor_);
			int contrastColor = new ColorInverter().getContrastColor(color);

			ImageViewCompat.setImageTintList(labelIcon, ColorStateList.valueOf(contrastColor));

			labelName.setTextColor(contrastColor);
			labelName.setText(labelName_);
			labelView.setCardBackgroundColor(color);
		}
	}
}
