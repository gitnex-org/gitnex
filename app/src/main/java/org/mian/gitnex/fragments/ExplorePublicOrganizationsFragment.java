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
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationsListAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */
public class ExplorePublicOrganizationsFragment extends Fragment {

	private final String TAG = "PublicOrganizations";
	private FragmentOrganizationsBinding fragmentPublicOrgBinding;
	private List<Organization> organizationsList;
	private OrganizationsListAdapter adapter;
	private Context context;
	private int pageSize;
	private int resultLimit;

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {

		fragmentPublicOrgBinding = FragmentOrganizationsBinding.inflate(inflater, container, false);
		context = getContext();

		resultLimit = Constants.getCurrentResultLimit(context);

		fragmentPublicOrgBinding.addNewOrganization.setVisibility(View.GONE);
		organizationsList = new ArrayList<>();

		fragmentPublicOrgBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											fragmentPublicOrgBinding.pullToRefresh.setRefreshing(
													false);
											loadInitial(resultLimit);
											adapter.notifyDataChanged();
										},
										200));

		adapter = new OrganizationsListAdapter(requireContext(), organizationsList);
		adapter.setLoadMoreListener(
				new OrganizationsListAdapter.OnLoadMoreListener() {

					@Override
					public void onLoadMore() {
						fragmentPublicOrgBinding.recyclerView.post(
								() -> {
									if (organizationsList.size() == resultLimit
											|| pageSize == resultLimit) {
										int page =
												(organizationsList.size() + resultLimit)
														/ resultLimit;
										loadMore(page, resultLimit);
									}
								});
					}
				});

		fragmentPublicOrgBinding.recyclerView.setHasFixedSize(true);
		fragmentPublicOrgBinding.recyclerView.setLayoutManager(new LinearLayoutManager(context));
		fragmentPublicOrgBinding.recyclerView.setAdapter(adapter);

		loadInitial(resultLimit);

		return fragmentPublicOrgBinding.getRoot();
	}

	private void loadInitial(int resultLimit) {

		Call<List<Organization>> call =
				RetrofitClient.getApiInterface(context)
						.orgGetAll(Constants.publicOrganizationsPageInit, resultLimit);
		call.enqueue(
				new Callback<List<Organization>>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Organization>> call,
							@NonNull Response<List<Organization>> response) {
						if (response.isSuccessful()) {
							if (response.body() != null && !response.body().isEmpty()) {
								organizationsList.clear();
								organizationsList.addAll(response.body());
								adapter.notifyDataChanged();
								fragmentPublicOrgBinding.noDataOrg.setVisibility(View.GONE);
							} else {
								organizationsList.clear();
								adapter.notifyDataChanged();
								fragmentPublicOrgBinding.noDataOrg.setVisibility(View.VISIBLE);
							}
							fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
						} else if (response.code() == 404) {
							fragmentPublicOrgBinding.noDataOrg.setVisibility(View.VISIBLE);
							fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
						} else {
							Log.e(TAG, String.valueOf(response.code()));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
						Log.e(TAG, t.toString());
					}
				});
	}

	private void loadMore(int page, int resultLimit) {

		fragmentPublicOrgBinding.progressBar.setVisibility(View.VISIBLE);
		Call<List<Organization>> call =
				RetrofitClient.getApiInterface(context).orgGetAll(page, resultLimit);
		call.enqueue(
				new Callback<List<Organization>>() {

					@Override
					public void onResponse(
							@NonNull Call<List<Organization>> call,
							@NonNull Response<List<Organization>> response) {
						if (response.isSuccessful()) {
							List<Organization> result = response.body();
							if (result != null) {
								if (!result.isEmpty()) {
									pageSize = result.size();
									organizationsList.addAll(result);
								} else {
									SnackBar.info(
											context,
											fragmentPublicOrgBinding.getRoot(),
											getString(R.string.noMoreData));
									adapter.setMoreDataAvailable(false);
								}
							}
							adapter.notifyDataChanged();
							fragmentPublicOrgBinding.progressBar.setVisibility(View.GONE);
						} else {
							Log.e(TAG, String.valueOf(response.code()));
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<List<Organization>> call, @NonNull Throwable t) {
						Log.e(TAG, t.toString());
					}
				});
	}
}
