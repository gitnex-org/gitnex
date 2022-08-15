package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentExploreUsersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class ExploreUsersFragment extends Fragment {

	private FragmentExploreUsersBinding viewBinding;
	private Context context;

	private List<User> usersList;
	private UsersAdapter adapter;
	private int pageSize;
	private int resultLimit;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreUsersBinding.inflate(inflater, container, false);
		context = getContext();
		setHasOptionsMenu(true);

		resultLimit = Constants.getCurrentResultLimit(context);

		usersList = new ArrayList<>();
		adapter = new UsersAdapter(usersList, context);

		viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			loadInitial("", resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
			if(usersList.size() == resultLimit || pageSize == resultLimit) {
				int page = (usersList.size() + resultLimit) / resultLimit;
				loadMore("", resultLimit, page);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		viewBinding.recyclerViewExploreUsers.setHasFixedSize(true);
		viewBinding.recyclerViewExploreUsers.addItemDecoration(dividerItemDecoration);
		viewBinding.recyclerViewExploreUsers.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewExploreUsers.setAdapter(adapter);

		loadInitial("", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, int resultLimit) {

		Call<InlineResponse2001> call = RetrofitClient.getApiInterface(context).userSearch(searchKeyword, null, 1, resultLimit);
		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<InlineResponse2001> call, @NonNull Response<InlineResponse2001> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().getData().size() > 0) {
						usersList.clear();
						usersList.addAll(response.body().getData());
						adapter.notifyDataChanged();
						viewBinding.noData.setVisibility(View.GONE);
					}
					else {
						usersList.clear();
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
					Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	private void loadMore(String searchKeyword, int resultLimit, int page) {

		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<InlineResponse2001> call = RetrofitClient.getApiInterface(context).userSearch(searchKeyword, null, page, resultLimit);
		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<InlineResponse2001> call, @NonNull Response<InlineResponse2001> response) {
				if(response.isSuccessful()) {
					assert response.body() != null;
					List<User> result = response.body().getData();
					if(result != null) {
						if(result.size() > 0) {
							pageSize = result.size();
							usersList.addAll(result);
						}
						else {
							SnackBar.info(context, viewBinding.getRoot(), getString(R.string.noMoreData));
							adapter.setMoreDataAvailable(false);
						}
					}
					adapter.notifyDataChanged();
					viewBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {

				Toasty.error(requireActivity(), requireActivity().getResources().getString(R.string.genericServerResponseError));
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				viewBinding.progressBar.setVisibility(View.VISIBLE);
				loadInitial(query, resultLimit);
				adapter.setLoadMoreListener(() -> viewBinding.recyclerViewExploreUsers.post(() -> {
					if(usersList.size() == resultLimit || pageSize == resultLimit) {
						int page = (usersList.size() + resultLimit) / resultLimit;
						loadMore(query, resultLimit, page);
					}
				}));
				searchView.setQuery(null, false);
				searchItem.collapseActionView();
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				return false;
			}
		});
	}

}
