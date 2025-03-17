package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.gitnex.tea4j.v2.models.Issue;
import org.mian.gitnex.databinding.ListIssueDependencyBinding;

/**
 * @author mmarif
 */
public class DependencyAdapter
		extends RecyclerView.Adapter<DependencyAdapter.DependencyViewHolder> {

	private final List<Issue> dependenciesList;
	private final boolean showDeleteIcon;
	private OnItemClickListener itemClickListener;

	public DependencyAdapter(List<Issue> dependenciesList, boolean showDeleteIcon) {
		this.dependenciesList = dependenciesList;
		this.showDeleteIcon = showDeleteIcon;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemClickListener = listener;
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
							itemClickListener.onItemClick(issue, position);
						}
					});
			holder.binding.cardView.setOnClickListener(null);
		} else {
			holder.binding.cardView.setOnClickListener(
					v -> {
						if (itemClickListener != null) {
							itemClickListener.onItemClick(issue, position);
						}
					});
		}
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
