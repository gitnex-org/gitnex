package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.databinding.ListIssueDependencyBinding;

/**
 * @author mmarif
 */
public class DependencyAdapter
		extends RecyclerView.Adapter<DependencyAdapter.DependencyViewHolder> {

	private List<Issue> dependenciesList;
	private final boolean showDeleteIcon;
	private OnItemClickListener itemClickListener;

	public DependencyAdapter(List<Issue> dependenciesList, boolean showDeleteIcon) {
		this.dependenciesList = dependenciesList != null ? dependenciesList : new ArrayList<>();
		this.showDeleteIcon = showDeleteIcon;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemClickListener = listener;
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<Issue> list) {
		this.dependenciesList = list != null ? list : new ArrayList<>();
		notifyDataSetChanged();
	}

	public List<Issue> getItems() {
		return dependenciesList;
	}

	@NonNull @Override
	public DependencyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListIssueDependencyBinding binding =
				ListIssueDependencyBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new DependencyViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull DependencyViewHolder holder, int position) {
		Issue issue = dependenciesList.get(position);
		holder.binding.dependencyTitle.setText(issue.getTitle());
		holder.binding.deleteDependency.setVisibility(showDeleteIcon ? View.VISIBLE : View.GONE);

		if (showDeleteIcon) {
			holder.binding.deleteDependency.setOnClickListener(
					v -> {
						if (itemClickListener != null) {
							itemClickListener.onItemClick(
									issue, holder.getBindingAdapterPosition());
						}
					});
			holder.binding.layoutFrame.setOnClickListener(null);
		} else {
			holder.binding.layoutFrame.setOnClickListener(
					v -> {
						if (itemClickListener != null) {
							itemClickListener.onItemClick(
									issue, holder.getBindingAdapterPosition());
						}
					});
		}

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return dependenciesList.size();
	}

	public static class DependencyViewHolder extends RecyclerView.ViewHolder {
		private final ListIssueDependencyBinding binding;

		DependencyViewHolder(ListIssueDependencyBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}

	public interface OnItemClickListener {
		void onItemClick(Issue issue, int position);
	}
}
