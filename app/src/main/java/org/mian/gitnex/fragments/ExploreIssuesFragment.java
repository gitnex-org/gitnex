package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.Issues;
import org.mian.gitnex.adapters.SearchIssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.InfiniteScrollListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ExploreIssuesFragment extends Fragment {

	private FragmentSearchIssuesBinding viewBinding;
	private SearchIssuesAdapter adapter;
	private List<Issues> dataList;
	Context ctx;

	private int apiCallCurrentValue = 10;
	private int pageCurrentIndex = 1;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		ctx = getContext();

		dataList = new ArrayList<>();
		adapter = new SearchIssuesAdapter(dataList, ctx);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ctx);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerViewSearchIssues.getContext(), DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewSearchIssues.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(linearLayoutManager);
		viewBinding.recyclerViewSearchIssues.setAdapter(adapter);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					pageCurrentIndex = 1;
					apiCallCurrentValue = 10;
					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadData(false, viewBinding.searchKeyword.getText().toString());
				}
			}
			return false;
		});

		viewBinding.recyclerViewSearchIssues.addOnScrollListener(new InfiniteScrollListener(pageCurrentIndex, linearLayoutManager) {
			@Override
			public void onScrolledToEnd(int firstVisibleItemPosition) {
				pageCurrentIndex++;
				loadData(true, Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString());
			}
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> {
			pageCurrentIndex = 1;
			apiCallCurrentValue = 10;
			loadData(false, Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString());
		});

		loadData(false, "");

		return viewBinding.getRoot();
	}

	private void loadData(boolean append, String searchKeyword) {

		viewBinding.noData.setVisibility(View.GONE);

		int apiCallDefaultLimit = 10;
		if(apiCallDefaultLimit > apiCallCurrentValue) {
			return;
		}

		if(pageCurrentIndex == 1 || !append) {
			dataList.clear();
			adapter.notifyDataSetChanged();
			viewBinding.pullToRefresh.setRefreshing(false);
			viewBinding.progressBar.setVisibility(View.VISIBLE);
		}
		else {
			viewBinding.loadingMoreView.setVisibility(View.VISIBLE);
		}

		Call<List<Issues>> call = RetrofitClient.getApiInterface(getContext())
			.queryIssues(Authorization.get(getContext()), searchKeyword, "issues", "open", pageCurrentIndex);

		call.enqueue(new Callback<List<Issues>>() {
			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {
				if(response.code() == 200) {
					assert response.body() != null;
					apiCallCurrentValue = response.body().size();
					if(!append) {
						dataList.clear();
					}
					dataList.addAll(response.body());
					adapter.notifyDataSetChanged();
				}
				else {
					dataList.clear();
					adapter.notifyDataChanged();
					viewBinding.noData.setVisibility(View.VISIBLE);
				}
				onCleanup();
			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
				Log.e("onFailure", Objects.requireNonNull(t.getMessage()));
				onCleanup();
			}

			private void onCleanup() {
				AppUtil.setMultiVisibility(View.GONE, viewBinding.loadingMoreView, viewBinding.progressBar);
				if(dataList.isEmpty()) {
					viewBinding.noData.setVisibility(View.VISIBLE);
				}
			}
		});
	}
}
