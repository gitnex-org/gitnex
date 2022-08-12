package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.OrganizationTeamRepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.AddNewTeamRepositoryBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
 */

public class AddNewTeamRepoActivity extends BaseActivity {

	private AddNewTeamRepositoryBinding addNewTeamRepositoryBinding;
	private View.OnClickListener onClickListener;
	private List<Repository> dataList;
	private OrganizationTeamRepositoriesAdapter adapter;
	private int resultLimit;

	private long teamId;
	private String teamName;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		addNewTeamRepositoryBinding = AddNewTeamRepositoryBinding.inflate(getLayoutInflater());
		setContentView(addNewTeamRepositoryBinding.getRoot());

		resultLimit = Constants.getCurrentResultLimit(ctx);
		initCloseListener();
		addNewTeamRepositoryBinding.close.setOnClickListener(onClickListener);

		teamId = getIntent().getLongExtra("teamId", 0);
		teamName = getIntent().getStringExtra("teamName");

		addNewTeamRepositoryBinding.recyclerViewTeamRepos.setHasFixedSize(true);
		addNewTeamRepositoryBinding.recyclerViewTeamRepos.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(addNewTeamRepositoryBinding.recyclerViewTeamRepos.getContext(),	DividerItemDecoration.VERTICAL);
		addNewTeamRepositoryBinding.recyclerViewTeamRepos.addItemDecoration(dividerItemDecoration);

		dataList = new ArrayList<>();

		loadRepos();
	}

	public void loadRepos() {

		Call<List<Repository>> call = RetrofitClient.getApiInterface(ctx).orgListRepos(getIntent().getStringExtra("orgName"), 1, resultLimit);

		addNewTeamRepositoryBinding.progressBar.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<List<Repository>> call, @NonNull Response<List<Repository>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					if(response.body().size() > 0) {

						dataList.clear();
						dataList.addAll(response.body());

						adapter = new OrganizationTeamRepositoriesAdapter(dataList, ctx, Math.toIntExact(teamId), getIntent().getStringExtra("orgName"), teamName);

						addNewTeamRepositoryBinding.recyclerViewTeamRepos.setAdapter(adapter);
						addNewTeamRepositoryBinding.noData.setVisibility(View.GONE);
					}
					else {

						addNewTeamRepositoryBinding.noData.setVisibility(View.VISIBLE);
					}

					addNewTeamRepositoryBinding.progressBar.setVisibility(View.GONE);
				}
				else {
					Toasty.error(ctx, getString(R.string.genericError));
				}
			}

			@Override
			public void onFailure(@NonNull Call<List<Repository>> call, @NonNull Throwable t) {
				Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
			}
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
