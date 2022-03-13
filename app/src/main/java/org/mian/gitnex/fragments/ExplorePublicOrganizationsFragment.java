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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.ExplorePublicOrganizationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
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

public class ExplorePublicOrganizationsFragment extends Fragment {

	private FragmentOrganizationsBinding fragmentPublicOrgBinding;
	private List<Organization> organizationsList;
	private ExplorePublicOrganizationsAdapter adapter;
	private Context context;
	private int pageSize;
	private final String TAG = Constants.publicOrganizations;
	private int resultLimit;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		fragmentPublicOrgBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);

		fragmentPublicOrgBinding.addNewOrganization.setVisibility(View.GONE);
		organizationsList = new ArrayList<>();

		fragmentPublicOrgBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
			fragmentPublicOrgBinding.pullToRefresh.setRefreshing(false);
			loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), resultLimit);
			adapter.notifyDataChanged();
		}, 200));

		adapter = new ExplorePublicOrganizationsAdapter(getContext(), organizationsList);
		adapter.setLoadMoreListener(() -> fragmentPublicOrgBinding.recyclerView.post(() -> {
			if(organizationsList.size() == resultLimit || pageSize == resultLimit) {
				int page = (organizationsList.size() + resultLimit) / resultLimit;
				loadMore(((BaseActivity) requireActivity()).getAccount().getAuthorization(), page, resultLimit);
			}
		}));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentPublicOrgBinding.recyclerView.getContext(), DividerItemDecoration.VERTICAL);
		fragmentPublicOrgBinding.recyclerView.setHasFixedSize(true);
		fragmentPublicOrgBinding.recyclerView.addItemDecoration(dividerItemDecoration);
		fragmentPublicOrgBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentPublicOrgBinding.recyclerView.setAdapter(adapter);

		loadInitial(((BaseActivity) requireActivity()).getAccount().getAuthorization(), resultLimit);

		return fragmentPublicOrgBinding.getRoot();
	}

	private void loadInitial(String token, int resultLimit) {

		Call<List<Organization>> call = RetrofitClient
			.getApiInterface(context).getAllOrgs(token, Constants.publicOrganizationsPageInit, resultLimit);
		call.enqueue(new Callback<List<Organization>>() {
			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {
				if(response.isSuccessful()) {
					if(response.body() != null && response.body().size() > 0) {
						organizationsList.clear();
						organizationsList.addAll(response.body());
						adapter.notifyDataChanged();
						fragmentPublicOrgBinding.noDataOrg.setVisibility(View.GONE);
					}
					else {
						organizationsList.clear();
						adapter.notifyDataChanged();
						fragmentPublicOrgBinding.noDataOrg.setVisibility(View.VISIBLE);
					}
					fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
				}
				else if(response.code() == 404) {
					fragmentPublicOrgBinding.noDataOrg.setVisibility(View.VISIBLE);
					fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}

	private void loadMore(String token, int page, int resultLimit) {

		fragmentPublicOrgBinding.progressBar.setVisibility(View.VISIBLE);
		Call<List<Organization>> call = RetrofitClient.getApiInterface(context).getAllOrgs(token, page, resultLimit);
		call.enqueue(new Callback<List<Organization>>() {
			@Override
			public void onResponse(@NonNull Call<List<Organization>> call, @NonNull Response<List<Organization>> response) {
				if(response.isSuccessful()) {
					List<Organization> result = response.body();
					if(result != null) {
						if(result.size() > 0) {
							pageSize = result.size();
							organizationsList.addAll(result);
						}
						else {
							SnackBar.info(context, fragmentPublicOrgBinding.getRoot(), getString(R.string.noMoreData));
							adapter.setMoreDataAvailable(false);
						}
					}
					adapter.notifyDataChanged();
					fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Log.e(TAG, String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
				Log.e(TAG, t.toString());
			}
		});
	}
}
