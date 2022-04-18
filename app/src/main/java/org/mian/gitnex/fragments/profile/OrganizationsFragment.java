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
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.profile.OrganizationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
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

public class OrganizationsFragment extends Fragment {

	private Context context;
	private FragmentOrganizationsBinding fragmentOrganizationsBinding;

	private List<Organization> organizationsList;
	private OrganizationsAdapter adapter;

	private int pageSize;
	private int resultLimit;

	private static final String usernameBundle = "";
	private String username;

	public OrganizationsFragment() {}

	public static OrganizationsFragment newInstance(String username) {
		OrganizationsFragment fragment = new OrganizationsFragment();
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

		fragmentOrganizationsBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);
		organizationsList = new ArrayList<>();

		fragmentOrganizationsBinding.addNewOrganization.setVisibility(View.GONE);

		fragmentOrganizationsBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentOrganizationsBinding.pullToRefresh.setRefreshing(false);
			loadInitial(username, resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new OrganizationsAdapter(context, organizationsList);
		adapter.setLoadMoreListener(() -> fragmentOrganizationsBinding.recyclerView.post(() -> {
			if(organizationsList.size() == resultLimit || pageSize == resultLimit) {
				int page = (organizationsList.size() + resultLimit) / resultLimit;
				loadMore(username, page, resultLimit);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
		fragmentOrganizationsBinding.recyclerView.setHasFixedSize(true);
		fragmentOrganizationsBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentOrganizationsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentOrganizationsBinding.recyclerView.setAdapter(adapter);

		loadInitial(username, resultLimit);

		return fragmentOrganizationsBinding.getRoot();
	}

	private void loadInitial(String username, int resultLimit) {

		Call<List<Organization>> call = RetrofitClient
			.getApiInterface(context)
			.orgListUserOrgs(username, 1, resultLimit);

		call.enqueue(new Callback<List<Organization>>() {
			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							assert response.body() != null;
							if(response.body().size() > 0) {
								organizationsList.clear();
								organizationsList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentOrganizationsBinding.noDataOrg.setVisibility(View.GONE);
							}
							else {
								organizationsList.clear();
								adapter.notifyDataChanged();
								fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							}
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
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
							fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
				Toasty.error(context, getString(R.string.genericError));
			}
		});
	}

	private void loadMore(String username, int page, int resultLimit) {

		fragmentOrganizationsBinding.progressBar.setVisibility(View.VISIBLE);

		Call<List<Organization>> call = RetrofitClient
			.getApiInterface(context)
			.orgListUserOrgs(username, page, resultLimit);

		call.enqueue(new Callback<List<Organization>>() {

			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {

				if(response.isSuccessful()) {

					switch(response.code()) {
						case 200:
							List<Organization> result = response.body();
							assert result != null;
							if(result.size() > 0) {
								pageSize = result.size();
								organizationsList.addAll(result);
							}
							else {
								SnackBar.info(context, fragmentOrganizationsBinding.getRoot(), getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
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
							fragmentOrganizationsBinding.noDataOrg.setVisibility(View.VISIBLE);
							fragmentOrganizationsBinding.progressBar.setVisibility(View.GONE);
							break;

						default:
							Toasty.error(context, getString(R.string.genericError));
							break;
					}
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
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

		List<Organization> arr = new ArrayList<>();

		for(Organization d : organizationsList) {
			if(d == null || d.getUsername() == null || d.getDescription() == null) {
				continue;
			}
			if(d.getUsername().toLowerCase().contains(text) || d.getDescription().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}
		adapter.updateList(arr);
	}
}
