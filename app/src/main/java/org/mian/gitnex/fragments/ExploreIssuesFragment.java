package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreIssuesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentSearchIssuesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
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
	private Context context;

	private List<Issues> dataList;
	private ExploreIssuesAdapter adapter;
	private int pageSize;
	private final String TAG = Constants.exploreIssues;
	private final int resultLimit = Constants.resultLimitOldGiteaInstances; // search issues always return 10 records

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentSearchIssuesBinding.inflate(inflater, container, false);
		context = getContext();

		dataList = new ArrayList<>();
		adapter = new ExploreIssuesAdapter(dataList, context);

		viewBinding.searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {
			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!Objects.requireNonNull(viewBinding.searchKeyword.getText()).toString().equals("")) {
					InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(viewBinding.searchKeyword.getWindowToken(), 0);

					viewBinding.progressBar.setVisibility(View.VISIBLE);
					loadInitial(String.valueOf(viewBinding.searchKeyword.getText()), resultLimit);

					adapter.setLoadMoreListener(() -> viewBinding.recyclerViewSearchIssues.post(() -> {
						if(dataList.size() == resultLimit || pageSize == resultLimit) {
							int page = (dataList.size() + resultLimit) / resultLimit;
							loadMore(String.valueOf(viewBinding.searchKeyword.getText()), resultLimit, page);
						}
					}));
				}
			}
			return false;
		});

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial("", resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewSearchIssues.post(() -> {
			if(dataList.size() == resultLimit || pageSize == resultLimit) {
				int page = (dataList.size() + resultLimit) / resultLimit;
				loadMore(String.valueOf(viewBinding.searchKeyword.getText()), resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewSearchIssues.setHasFixedSize(true);
		viewBinding.recyclerViewSearchIssues.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewSearchIssues.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewSearchIssues.setAdapter(adapter);

		loadInitial("", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, int resultLimit) {

		Call<List<Issues>> call = RetrofitClient
			.getApiInterface(context).queryIssues(Authorization.get(getContext()), searchKeyword, "issues", "open", resultLimit, 1);
		call.enqueue(new Callback<List<Issues>>() {
			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().size() > 0) {
						dataList.clear();
						dataList.addAll(response.body());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);
					}
					else {
						dataList.clear();
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.VISIBLE);
					}
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					viewBinding.noData.setVisibility(View.VISIBLE);
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String searchKeyword, int resultLimit, int page) {

		viewBinding.loadingMoreView.setVisibility(View.VISIBLE);
		Call<List<Issues>> call = RetrofitClient.getApiInterface(context)
			.queryIssues(Authorization.get(getContext()), searchKeyword, "issues", "open", resultLimit, page);
		call.enqueue(new Callback<List<Issues>>() {
			@Override
			public void onResponse(@NonNull Call<List<Issues>> call, @NonNull Response<List<Issues>> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<Issues> result = response.body();
					if(result.size() > 0) {
						pageSize = result.size();
						dataList.addAll(result);
					}
					else {
						SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);
					}
					adapter.notifyDataChanged();
					viewBinding.loadingMoreView.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Issues>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}
}
