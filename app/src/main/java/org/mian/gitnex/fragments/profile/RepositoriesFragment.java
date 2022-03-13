package org.mian.gitnex.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.UserRepositories;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.profile.RepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
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
 * Author M M Arif
 */

public class RepositoriesFragment extends Fragment {

	private Context context;
	private FragmentRepositoriesBinding fragmentRepositoriesBinding;

	private List<UserRepositories> reposList;
	private RepositoriesAdapter adapter;

	private int pageSize;
	private int resultLimit;

	private static final String usernameBundle = "";
	private String username;

	public RepositoriesFragment() {}

	public static RepositoriesFragment newInstance(String username) {
		RepositoriesFragment fragment = new RepositoriesFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentRepositoriesBinding = FragmentRepositoriesBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);
		reposList = new ArrayList<>();

		fragmentRepositoriesBinding.addNewRepo.setVisibility(View.GONE);

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentRepositoriesBinding.pullToRefresh.setRefreshing(false);
			loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), username, resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new RepositoriesAdapter(context, reposList);
		adapter.setLoadMoreListener(() -> fragmentRepositoriesBinding.recyclerView.post(() -> {
			if(reposList.size() == resultLimit || pageSize == resultLimit) {
				int page = (reposList.size() + resultLimit) / resultLimit;
				loadMore(((BaseActivity) requireActivity()).getAccount().getAuthorization(), username, page, resultLimit);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);

		loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), username, resultLimit);

		return fragmentRepositoriesBinding.getRoot();
	}

	private void loadInitial(String token, String username, int resultLimit) {

		Call<List<UserRepositories>> call = RetrofitClient
			.getApiInterface(context).getUserProfileRepositories(token, username, 1, resultLimit);

		call.enqueue(new Callback<List<UserRepositories>>() {
			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							assert response.body() != null;
							if(response.body().size() > 0) {
								reposList.clear();
								reposList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
							}
							else {
								reposList.clear();
								adapter.notifyDataChanged();
								fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
							}
							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.cancelButton),
								getResources().getString(R.string.navLogout));
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserRepositories>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	private void loadMore(String token, String username, int page, int resultLimit) {

		fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<UserRepositories>> call = RetrofitClient.getApiInterface(context).getUserProfileRepositories(token, username, page, resultLimit);

		call.enqueue(new Callback<List<UserRepositories>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserRepositories>> call, @NonNull Response<List<UserRepositories>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							List<UserRepositories> result = response.body();
							assert result != null;
							if(result.size() > 0) {
								pageSize = result.size();
								reposList.addAll(result);
							}
							else {
								SnackBar.info(context, fragmentRepositoriesBinding.getRoot(), getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
							break;

						case 401:
							AlertDialogs.authorizationTokenRevokedDialog(context, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.cancelButton),
								getResources().getString(R.string.navLogout));
							break;

						case 403:
							Toasty.error(context, context.getString(R.string.authorizeError));
							break;

						case 404:
							fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
							fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<UserRepositories>> call, @NonNull Throwable t) {
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

		List<UserRepositories> arr = new ArrayList<>();

		for(UserRepositories d : reposList) {
			if(d == null || d.getFullName() == null || d.getDescription() == null) {
				continue;
			}
			if(d.getFullName().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}
		adapter.updateList(arr);
	}
}
