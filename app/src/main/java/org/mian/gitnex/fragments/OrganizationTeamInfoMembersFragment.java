package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.OrganizationPermissions;
import org.gitnex.tea4j.v2.models.Team;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.activities.AddNewTeamMemberActivity;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.FragmentOrganizationTeamInfoMembersBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.MembersByOrgTeamViewModel;

/**
 * @author opyale
 */
public class OrganizationTeamInfoMembersFragment extends Fragment {

	private final List<User> teamUserInfo = new ArrayList<>();
	private Context ctx;
	private FragmentOrganizationTeamInfoMembersBinding binding;
	private MembersByOrgTeamViewModel membersByOrgTeamViewModel;
	private Team team;
	private UserGridAdapter adapter;
	public static boolean refreshMembers = false;
	private int page = 1;
	private int resultLimit;

	public OrganizationTeamInfoMembersFragment() {}

	public static OrganizationTeamInfoMembersFragment newInstance(Team team) {
		OrganizationTeamInfoMembersFragment fragment = new OrganizationTeamInfoMembersFragment();

		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationTeamInfoMembersBinding.inflate(inflater, container, false);
		ctx = getContext();

		team = (Team) requireArguments().getSerializable("team");

		membersByOrgTeamViewModel =
				new ViewModelProvider(this).get(MembersByOrgTeamViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(ctx);

		adapter = new UserGridAdapter(ctx, teamUserInfo);
		binding.members.setAdapter(adapter);
		fetchDataAsync();

		OrganizationPermissions permissions =
				(OrganizationPermissions)
						requireActivity().getIntent().getSerializableExtra("permissions");

		assert permissions != null;
		if (!permissions.isIsOwner()) {
			binding.addNewMember.setVisibility(View.GONE);
		}
		binding.addNewMember.setOnClickListener(
				v1 -> {
					Intent intent = new Intent(getContext(), AddNewTeamMemberActivity.class);
					intent.putExtra("teamId", team.getId());
					startActivity(intent);
				});

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (refreshMembers) {
			fetchDataAsync();
			refreshMembers = false;
		}
	}

	private void fetchDataAsync() {

		membersByOrgTeamViewModel
				.getMembersList(team.getId(), ctx, page, resultLimit)
				.observe(
						getViewLifecycleOwner(),
						mainList -> {
							adapter = new UserGridAdapter(ctx, mainList);

							adapter.setLoadMoreListener(
									new UserGridAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											membersByOrgTeamViewModel.loadMore(
													team.getId(),
													ctx,
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
							binding.members.setLayoutManager(layoutManager);

							if (adapter.getItemCount() > 0) {
								binding.members.setAdapter(adapter);
								binding.noDataMembers.setVisibility(View.GONE);
							} else {
								adapter.notifyDataChanged();
								binding.members.setAdapter(adapter);
								binding.noDataMembers.setVisibility(View.VISIBLE);
							}

							binding.progressBar.setVisibility(View.GONE);
						});
	}
}
