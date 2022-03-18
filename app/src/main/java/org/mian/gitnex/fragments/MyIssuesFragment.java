package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.FragmentIssuesBinding;
import org.mian.gitnex.viewmodels.IssuesViewModel;

/**
 * @author M M Arif
 */

public class MyIssuesFragment extends Fragment {

	private FragmentIssuesBinding fragmentIssuesBinding;
	private ExploreIssuesAdapter adapter;
	private int page = 1;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentIssuesBinding = FragmentIssuesBinding.inflate(inflater, container, false);

		fragmentIssuesBinding.recyclerView.setHasFixedSize(true);
		fragmentIssuesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentIssuesBinding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		fragmentIssuesBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		fragmentIssuesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			fragmentIssuesBinding.pullToRefresh.setRefreshing(false);
			IssuesViewModel.loadIssuesList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), null, "issues", true, "open", getContext());
			fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());

		return fragmentIssuesBinding.getRoot();
	};

	private void fetchDataAsync(String instanceToken) {

		IssuesViewModel issuesModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		issuesModel.getIssuesList(instanceToken, "", "issues", true, "open", getContext()).observe(getViewLifecycleOwner(), issuesListMain -> {

			adapter = new ExploreIssuesAdapter(issuesListMain, getContext());
			adapter.setLoadMoreListener(new ExploreIssuesAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					IssuesViewModel.loadMoreIssues(instanceToken, "", "issues", true, "open", page, getContext(), adapter);
					fragmentIssuesBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentIssuesBinding.recyclerView.setAdapter(adapter);
				fragmentIssuesBinding.noDataIssues.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentIssuesBinding.recyclerView.setAdapter(adapter);
				fragmentIssuesBinding.noDataIssues.setVisibility(View.VISIBLE);
			}

			fragmentIssuesBinding.progressBar.setVisibility(View.GONE);
		});
	}
}
