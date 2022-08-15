package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.v2.models.Team;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationTeamInfoMembersBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author opyale
 */

public class OrganizationTeamInfoMembersFragment extends Fragment {

	private final List<User> teamUserInfo = new ArrayList<>();
	private Context ctx;
	private FragmentOrganizationTeamInfoMembersBinding binding;
	private Team team;
	private UserGridAdapter adapter;

	public OrganizationTeamInfoMembersFragment() {
	}

	public static OrganizationTeamInfoMembersFragment newInstance(Team team) {
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

		team = (Team) requireArguments().getSerializable("team");

		adapter = new UserGridAdapter(ctx, teamUserInfo);
		binding.members.setAdapter(adapter);
		fetchMembersAsync();

		return binding.getRoot();
	}

	private void fetchMembersAsync() {

		Call<List<User>> call = RetrofitClient.getApiInterface(ctx).orgListTeamMembers(team.getId(), null, null);

		binding.progressBar.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<User>> call, @NonNull Response<List<User>> response) {
				if(response.isSuccessful() && response.body() != null && response.body().size() > 0) {
					teamUserInfo.clear();
					teamUserInfo.addAll(response.body());

					adapter.notifyDataSetChanged();

					binding.noDataMembers.setVisibility(View.GONE);
					binding.members.setVisibility(View.VISIBLE);
				}
				else {
					binding.members.setVisibility(View.GONE);
					binding.noDataMembers.setVisibility(View.VISIBLE);
				}

				binding.progressBar.setVisibility(View.GONE);
			}

			@Override
			public void onFailure(@NonNull Call<List<User>> call, @NonNull Throwable t) {

				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

}
