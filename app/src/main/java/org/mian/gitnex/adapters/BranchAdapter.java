package org.mian.gitnex.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Branch;
import org.mian.gitnex.R;

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
	public BranchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view =
				LayoutInflater.from(parent.getContext())
						.inflate(R.layout.list_branches, parent, false);
		return new BranchViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return branches.size();
	}

	public void addBranches(List<Branch> newBranches) {
		int startPosition = branches.size();
		branches.addAll(newBranches);
		notifyItemRangeInserted(startPosition, newBranches.size());
	}

	public void clear() {
		int oldSize = branches.size();
		branches.clear();
		notifyItemRangeRemoved(0, oldSize);
	}

	public static class BranchViewHolder extends RecyclerView.ViewHolder {
		TextView textView;

		BranchViewHolder(View itemView) {
			super(itemView);
			textView = itemView.findViewById(R.id.branch_name);
		}
	}

	@Override
	public void onBindViewHolder(BranchViewHolder holder, int position) {

		Branch branch = branches.get(position);
		holder.textView.setText(branch.getName());
		holder.textView.setOnClickListener(
				v -> {
					if (listener != null) {
						listener.onBranchClick(branch.getName());
					}
				});
	}
}
