package org.mian.gitnex.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.mian.gitnex.activities.CreateLabelActivity;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.databinding.FragmentLabelsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.LabelsViewModel;

/**
 * @author M M Arif
 */
public class OrganizationLabelsFragment extends Fragment {

	private FragmentLabelsBinding fragmentLabelsBinding;
	private OrganizationPermissions permissions;
	private LabelsViewModel labelsViewModel;
	private LabelsAdapter adapter;
	private static final String getOrgName = null;
	private String repoOwner;
	private final String type = "org";
	private int page = 1;
	private int resultLimit;

	public static OrganizationLabelsFragment newInstance(
			String repoOwner, OrganizationPermissions permissions) {

		OrganizationLabelsFragment fragment = new OrganizationLabelsFragment();
		Bundle args = new Bundle();
		args.putString(getOrgName, repoOwner);
		args.putSerializable("permissions", permissions);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (getArguments() != null) {

			repoOwner = getArguments().getString(getOrgName);
			permissions = (OrganizationPermissions) getArguments().getSerializable("permissions");
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentLabelsBinding = FragmentLabelsBinding.inflate(inflater, container, false);
		labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);

		final SwipeRefreshLayout swipeRefresh = fragmentLabelsBinding.pullToRefresh;

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		fragmentLabelsBinding.recyclerView.setHasFixedSize(true);
		fragmentLabelsBinding.recyclerView.setLayoutManager(
				new LinearLayoutManager(requireContext()));

		swipeRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											page = 1;
											swipeRefresh.setRefreshing(false);
											LabelsViewModel.loadLabelsList(
													repoOwner,
													null,
													"org",
													requireContext(),
													fragmentLabelsBinding,
													page,
													resultLimit);
										},
										200));

		fetchDataAsync(repoOwner);

		if (!permissions.isIsOwner()) {
			fragmentLabelsBinding.createLabel.setVisibility(View.GONE);
		}

		fragmentLabelsBinding.createLabel.setOnClickListener(
				v1 -> {
					Intent intent = new Intent(requireContext(), CreateLabelActivity.class);
					intent.putExtra("orgName", repoOwner);
					intent.putExtra("type", "org");
					startActivity(intent);
				});

		return fragmentLabelsBinding.getRoot();
	}

	@Override
	public void onResume() {

		super.onResume();

		if (CreateLabelActivity.refreshLabels) {

			page = 1;
			LabelsViewModel.loadLabelsList(
					repoOwner,
					null,
					"org",
					requireContext(),
					fragmentLabelsBinding,
					page,
					resultLimit);
			CreateLabelActivity.refreshLabels = false;
		}
	}

	private void fetchDataAsync(String owner) {

		labelsViewModel
				.getLabelsList(
						owner,
						null,
						"org",
						requireContext(),
						fragmentLabelsBinding,
						page,
						resultLimit)
				.observe(
						getViewLifecycleOwner(),
						mainList -> {
							adapter = new LabelsAdapter(requireContext(), mainList, type, owner);
							adapter.setLoadMoreListener(
									new LabelsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											labelsViewModel.loadMore(
													owner,
													null,
													"org",
													requireContext(),
													fragmentLabelsBinding,
													page,
													resultLimit,
													adapter);
											fragmentLabelsBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentLabelsBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							if (adapter.getItemCount() > 0) {
								fragmentLabelsBinding.recyclerView.setAdapter(adapter);
								fragmentLabelsBinding.noData.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentLabelsBinding.recyclerView.setAdapter(adapter);
								fragmentLabelsBinding.noData.setVisibility(View.VISIBLE);
							}

							fragmentLabelsBinding.progressBar.setVisibility(View.GONE);
						});
	}
}
