package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationMembersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.MembersByOrgViewModel;

/**
 * @author M M Arif
 */
public class OrganizationMembersFragment extends Fragment {

	private FragmentOrganizationMembersBinding binding;
	private MembersByOrgViewModel membersModel;
	private static final String orgNameF = "param2";
	private String orgName;
	private UserGridAdapter adapter;
	private int page = 1;
	private int resultLimit;

	public OrganizationMembersFragment() {}

	public static OrganizationMembersFragment newInstance(String param1) {
		OrganizationMembersFragment fragment = new OrganizationMembersFragment();
		Bundle args = new Bundle();
		args.putString(orgNameF, param1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			orgName = getArguments().getString(orgNameF);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentOrganizationMembersBinding.inflate(inflater, container, false);

		membersModel = new ViewModelProvider(this).get(MembersByOrgViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		fetchDataAsync(orgName);

		return binding.getRoot();
	}

	private void fetchDataAsync(String owner) {

		membersModel
				.getMembersList(owner, requireContext(), page, resultLimit)
				.observe(
						getViewLifecycleOwner(),
						mainList -> {
							adapter = new UserGridAdapter(requireContext(), mainList);

							adapter.setLoadMoreListener(
									new UserGridAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											membersModel.loadMore(
													owner,
													requireContext(),
													page,
													resultLimit,
													adapter,
													binding);
											binding.progressBar.setVisibility(View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											binding.progressBar.setVisibility(View.GONE);
										}
									});

							GridLayoutManager layoutManager =
									new GridLayoutManager(requireContext(), 2);
							binding.gridView.setLayoutManager(layoutManager);

							if (adapter.getItemCount() > 0) {
								binding.gridView.setAdapter(adapter);
								binding.noDataMembers.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.gridView.setAdapter(adapter);
								binding.noDataMembers.setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}
}
