package org.mian.gitnex.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import org.mian.gitnex.databinding.ListAdminUnadoptedReposBinding;

/**
 * @author mmarif
 * @author qwerty287
 */
public class AdminUnadoptedReposAdapter
		extends RecyclerView.Adapter<AdminUnadoptedReposAdapter.ViewHolder> {

	private final List<String> repos;
	private final OnRepoClickListener listener;

	public interface OnRepoClickListener {
		void onRepoClick(String repoName);
	}

	public AdminUnadoptedReposAdapter(List<String> repos, OnRepoClickListener listener) {
		this.repos = repos;
		this.listener = listener;
	}

	@NonNull @Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		ListAdminUnadoptedReposBinding binding =
				ListAdminUnadoptedReposBinding.inflate(
						LayoutInflater.from(parent.getContext()), parent, false);
		return new ViewHolder(binding);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
		String repo = repos.get(position);

		holder.binding.repoName.setText(repo);
		holder.binding.getRoot().setOnClickListener(v -> listener.onRepoClick(repo));

		holder.binding.getRoot().updateAppearance(position, getItemCount());
	}

	@Override
	public int getItemCount() {
		return repos.size();
	}

	@SuppressLint("NotifyDataSetChanged")
	public void updateList(List<String> newList) {
		this.repos.clear();
		this.repos.addAll(newList);
		notifyDataSetChanged();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		final ListAdminUnadoptedReposBinding binding;

		ViewHolder(ListAdminUnadoptedReposBinding binding) {
			super(binding.getRoot());
			this.binding = binding;
		}
	}
}
