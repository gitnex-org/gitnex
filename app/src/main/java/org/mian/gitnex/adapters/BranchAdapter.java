package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Branch;
import org.mian.gitnex.databinding.ListBranchesBinding;

/**
 * @author mmarif
 */
public class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {

	private final List<Branch> branches = new ArrayList<>();
	private final OnBranchClickListener listener;

	public interface OnBranchClickListener {
		void onBranchClick(String branchName);
	}

	public BranchAdapter(OnBranchClickListener listener) {
		this.listener = listener;
	}

	@NonNull @Override
	public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListBranchesBinding binding =
				ListBranchesBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new BranchViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
		holder.bind(branches.get(position));
	}

	@Override
	public int getItemCount() {
		return branches.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void setBranches(List<Branch> newBranches) {
		this.branches.clear();
		this.branches.addAll(newBranches);
		notifyDataSetChanged();
	}

	public void addBranches(List<Branch> newBranches) {
		int startPosition = branches.size();
		branches.addAll(newBranches);
		notifyItemRangeInserted(startPosition, newBranches.size());
	}

	public class BranchViewHolder extends RecyclerView.ViewHolder {
		private final ListBranchesBinding binding;

		BranchViewHolder(ListBranchesBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}

		void bind(Branch branch) {
			binding.branchName.setText(branch.getName());
			binding.branchName.setOnClickListener(
					v -> {
						if (listener != null) {
							listener.onBranchClick(branch.getName());
						}
					});
		}
	}
}
