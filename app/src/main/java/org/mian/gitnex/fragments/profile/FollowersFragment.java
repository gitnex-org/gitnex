package org.mian.gitnex.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UsersAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentProfileFollowersFollowingBinding;
import org.mian.gitnex.helpers.AlertDialogs;
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

public class FollowersFragment extends Fragment {

	private static final String usernameBundle = "";
	private Context context;
	private FragmentProfileFollowersFollowingBinding fragmentProfileFollowersFollowingBinding;
	private List<User> usersList;
	private UsersAdapter adapter;
	private int pageSize;
	private int resultLimit;
	private String username;

	public FollowersFragment() {
	}

	public static FollowersFragment newInstance(String username) {
		FollowersFragment fragment = new FollowersFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentProfileFollowersFollowingBinding = FragmentProfileFollowersFollowingBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);
		usersList = new ArrayList<>();

		fragmentProfileFollowersFollowingBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentProfileFollowersFollowingBinding.pullToRefresh.setRefreshing(false);
			loadInitial(username, resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new UsersAdapter(usersList, context);
		adapter.setLoadMoreListener(() -> fragmentProfileFollowersFollowingBinding.recyclerView.post(() -> {
			if(usersList.size() == resultLimit || pageSize == resultLimit) {
				int page = (usersList.size() + resultLimit) / resultLimit;
				loadMore(username, page, resultLimit);
			}
		}));

		fragmentProfileFollowersFollowingBinding.recyclerView.setHasFixedSize(true);
		fragmentProfileFollowersFollowingBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentProfileFollowersFollowingBinding.recyclerView.setAdapter(adapter);

		loadInitial(username, resultLimit);

		return fragmentProfileFollowersFollowingBinding.getRoot();
	}

	private void loadInitial(String username, int resultLimit) {

		Call<List<User>> call = RetrofitClient.getApiInterface(context).userListFollowers(username, 1, resultLimit);

		call.enqueue(new Callback<List<User>>() {

			@Override
			public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							assert response.body() != null;
							if(response.body().size() > 0) {
								usersList.clear();
								usersList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentProfileFollowersFollowingBinding.noData.setVisibility(View.GONE);
							}
							else {
								usersList.clear();
								adapter.notifyDataChanged();
								fragmentProfileFollowersFollowingBinding.noData.setVisibility(View.VISIBLE);
							}
							fragmentProfileFollowersFollowingBinding.progressBar.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context);
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentProfileFollowersFollowingBinding.noData.setVisibility(View.VISIBLE);
							fragmentProfileFollowersFollowingBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	private void loadMore(String username, int page, int resultLimit) {

		fragmentProfileFollowersFollowingBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<User>> call = RetrofitClient.getApiInterface(context).userListFollowers(username, page, resultLimit);

		call.enqueue(new Callback<List<User>>() {

			@Override
			public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							List<User> result = response.body();
							assert result != null;
							if(result.size() > 0) {
								pageSize = result.size();
								usersList.addAll(result);
							}
							else {
								SnackBar.info(context, fragmentProfileFollowersFollowingBinding.getRoot(), getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							fragmentProfileFollowersFollowingBinding.progressBar.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context);
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentProfileFollowersFollowingBinding.noData.setVisibility(View.VISIBLE);
							fragmentProfileFollowersFollowingBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {
				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				filter(newText);
				return false;
			}
		});
	}

	private void filter(String text) {

		List<User> arr = new ArrayList<>();

		for(User d : usersList) {
			if(d == null || d.getLogin() == null || d.getFullName() == null) {
				continue;
			}
			if(d.getLogin().toLowerCase().contains(text) || d.getFullName().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}
		adapter.updateList(arr);
	}

}
