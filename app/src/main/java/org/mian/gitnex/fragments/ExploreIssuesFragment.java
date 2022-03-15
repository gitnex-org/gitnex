package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.IssuesViewModel;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class ExploreIssuesFragment extends Fragment {

	private FragmentSearchIssuesBinding viewBinding;
	private ExploreIssuesAdapter adapter;
	private int page = 1;
	private final String TAG = Constants.exploreIssues;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), String.valueOf(viewBinding.searchKeyword.getText()));
				}
			}
			return false;
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			viewBinding.pullToRefresh.setRefreshing(false);
			if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
				fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), String.valueOf(viewBinding.searchKeyword.getText()));
			}
			else {
				fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), "");
			}
			viewBinding.progressBar.setVisibility(View.VISIBLE);

		}, 50));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(new LinearLayoutManager(requireActivity()));

		fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), "");

		return viewBinding.getRoot();
	}

	private void fetchDataAsync(String instanceToken, String searchKeyword) {

		IssuesViewModel issuesModel = new ViewModelProvider(this).get(IssuesViewModel.class);

		issuesModel.getIssuesList(instanceToken, searchKeyword, "issues", null, "open", getContext()).observe(getViewLifecycleOwner(), issuesListMain -> {

			adapter = new ExploreIssuesAdapter(issuesListMain, getContext());
			adapter.setLoadMoreListener(new ExploreIssuesAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					IssuesViewModel.loadMoreIssues(instanceToken, searchKeyword, "issues", null, "open", page, getContext(), adapter);
					viewBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					viewBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				viewBinding.recyclerViewSearchIssues.setAdapter(adapter);
				viewBinding.noData.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				viewBinding.recyclerViewSearchIssues.setAdapter(adapter);
				viewBinding.noData.setVisibility(View.VISIBLE);
			}

			viewBinding.progressBar.setVisibility(View.GONE);
		});
	}
}
