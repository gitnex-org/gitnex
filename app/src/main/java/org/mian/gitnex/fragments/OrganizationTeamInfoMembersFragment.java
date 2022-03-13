package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.models.Teams;
import org.gitnex.tea4j.models.UserInfo;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationTeamInfoMembersBinding;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author opyale
 */

public class OrganizationTeamInfoMembersFragment extends Fragment {

	private Context ctx;

	private FragmentOrganizationTeamInfoMembersBinding binding;
	private Teams team;

	private UserGridAdapter adapter;
	private final List<UserInfo> teamUserInfo = new ArrayList<>();

	public OrganizationTeamInfoMembersFragment() {}

	public static OrganizationTeamInfoMembersFragment newInstance(Teams team) {
		OrganizationTeamInfoMembersFragment fragment = new OrganizationTeamInfoMembersFragment();

		Bundle bundle = new Bundle();
		bundle.putSerializable("team", team);
		fragment.setArguments(bundle);

		return fragment;
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationTeamInfoMembersBinding.inflate(inflater, container, false);
		ctx = getContext();

		team = (Teams) requireArguments().getSerializable("team");

		adapter = new UserGridAdapter(ctx, teamUserInfo);
		binding.members.setAdapter(adapter);
		fetchMembersAsync();

		return binding.getRoot();
	}

	private void fetchMembersAsync() {

		Call<List<UserInfo>> call = RetrofitClient
			.getApiInterface(ctx)
			.getTeamMembersByOrg(((BaseActivity) requireActivity()).getAccount().getAuthorization(), team.getId());

		binding.progressBar.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<List<UserInfo>>() {

			@Override
			public void onResponse(@NonNull Call<List<UserInfo>> call, @NonNull Response<List<UserInfo>> response) {
				if(response.isSuccessful() && response.body() != null && response.body().size() > 0) {
					teamUserInfo.clear();
					teamUserInfo.addAll(response.body());

					adapter.notifyDataSetChanged();

					binding.noDataMembers.setVisibility(View.GONE);
					binding.members.setVisibility(View.VISIBLE);
				} else {
					binding.members.setVisibility(View.GONE);
					binding.noDataMembers.setVisibility(View.VISIBLE);
				}

				binding.progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(@NonNull Call<List<UserInfo>> call, @NonNull Throwable t) {
				Log.i("onFailure", t.toString());
			}

		});
	}

}
