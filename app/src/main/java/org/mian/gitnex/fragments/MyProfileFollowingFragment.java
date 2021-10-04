package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MyProfileFollowingAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentProfileFollowersFollowingBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MyProfileFollowingFragment extends Fragment {

	private FragmentProfileFollowersFollowingBinding viewBinding;
	private Context context;

	private List<UserInfo> dataList;
	private MyProfileFollowingAdapter adapter;
	private int pageSize;
	private final String TAG = Constants.tagFollowing;
	private int resultLimit;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentProfileFollowersFollowingBinding.inflate(inflater, container, false);
		context = getContext();

		dataList = new ArrayList<>();
		adapter = new MyProfileFollowingAdapter(dataList, context);
		resultLimit = Constants.getCurrentResultLimit(context);

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial(resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerView.post(() -> {
			if(dataList.size() == resultLimit || pageSize == resultLimit) {
				int page = (dataList.size() + resultLimit) / resultLimit;
				loadMore(resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerView.setHasFixedSize(true);
		viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerView.setAdapter(adapter);

		loadInitial(resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(int resultLimit) {

		Call<List<UserInfo>> call = RetrofitClient
			.getApiInterface(context)
			.getFollowing(Authorization.get(getContext()), 1, resultLimit);
		call.enqueue(new Callback<List<UserInfo>>() {
			@Override
			public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
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
			public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(int resultLimit, int page) {

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<List<UserInfo>> call = RetrofitClient.getApiInterface(context)
			.getFollowing(Authorization.get(getContext()), page, resultLimit);
		call.enqueue(new Callback<List<UserInfo>>() {
			@Override
			public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<UserInfo> result = response.body();
					if(result.size() > 0) {
						pageSize = result.size();
						dataList.addAll(result);
					}
					else {
						SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
						adapter.setMoreDataAvailable(false);
					}
					adapter.notifyDataChanged();
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}
}
