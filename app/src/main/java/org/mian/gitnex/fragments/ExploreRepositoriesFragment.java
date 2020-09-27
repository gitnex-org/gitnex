package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.ExploreRepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.CustomExploreRepositoriesDialogBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.ExploreRepositories;
import org.mian.gitnex.models.UserRepositories;
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Template Author M M Arif
 * Author 6543
 */

public class ExploreRepositoriesFragment extends Fragment {

	private Context ctx;
	private TinyDB tinyDb;
	private static String repoNameF = "param2";
	private static String repoOwnerF = "param1";
	private ProgressBar mProgressBar;
	private RecyclerView mRecyclerView;
	private TextView noData;
	private TextView searchKeyword;
	private Boolean repoTypeInclude = true;
	private String sort = "updated";
	private String order = "desc";
	private int limit = 50;

	private OnFragmentInteractionListener mListener;

	private Dialog dialogFilterOptions;
	private CustomExploreRepositoriesDialogBinding filterBinding;

	public ExploreRepositoriesFragment() {

	}

	public static ExploreRepositoriesFragment newInstance(String param1, String param2) {

		ExploreRepositoriesFragment fragment = new ExploreRepositoriesFragment();
		Bundle args = new Bundle();
		args.putString(repoOwnerF, param1);
		args.putString(repoNameF, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		if(getArguments() != null) {
			String repoName = getArguments().getString(repoNameF);
			String repoOwner = getArguments().getString(repoOwnerF);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_explore_repo, container, false);
		setHasOptionsMenu(true);
		ctx = getContext();

		tinyDb = new TinyDB(getContext());
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		tinyDb.putBoolean("exploreRepoIncludeTopic", false);
		tinyDb.putBoolean("exploreRepoIncludeDescription", false);
		tinyDb.putBoolean("exploreRepoIncludeTemplate", false);
		tinyDb.putBoolean("exploreRepoOnlyArchived", false);
		tinyDb.putBoolean("exploreRepoOnlyPrivate", false);

		searchKeyword = v.findViewById(R.id.searchKeyword);
		noData = v.findViewById(R.id.noData);
		mProgressBar = v.findViewById(R.id.progress_bar);
		mRecyclerView = v.findViewById(R.id.recyclerViewReposSearch);

		mProgressBar.setVisibility(View.VISIBLE);

		searchKeyword.setOnEditorActionListener((v1, actionId, event) -> {

			if(actionId == EditorInfo.IME_ACTION_SEND) {
				if(!searchKeyword.getText().toString().equals("")) {
					mProgressBar.setVisibility(View.VISIBLE);
					mRecyclerView.setVisibility(View.GONE);
					loadSearchReposList(instanceUrl, instanceToken, loginUid, searchKeyword.getText().toString(), repoTypeInclude, sort, order, getContext(), tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), tinyDb.getBoolean("exploreRepoOnlyPrivate"), limit);
				}
			}
			return false;
		});

		int limitDefault = 25;
		loadDefaultList(instanceUrl, instanceToken, loginUid, repoTypeInclude, sort, order, getContext(), limitDefault);

		return v;

	}

	private void loadDefaultList(String instanceUrl, String instanceToken, String loginUid, Boolean repoTypeInclude, String sort, String order, final Context context, int limit) {

		Call<ExploreRepositories> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryRepos(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), null, repoTypeInclude, sort, order, tinyDb.getBoolean("exploreRepoIncludeTopic"), tinyDb.getBoolean("exploreRepoIncludeDescription"), tinyDb.getBoolean("exploreRepoIncludeTemplate"), tinyDb.getBoolean("exploreRepoOnlyArchived"), tinyDb.getBoolean("exploreRepoOnlyPrivate"), limit);

		call.enqueue(new Callback<ExploreRepositories>() {

			@Override
			public void onResponse(@NonNull Call<ExploreRepositories> call, @NonNull Response<ExploreRepositories> response) {

				if(response.isSuccessful()) {
					assert response.body() != null;
					getReposList(response.body().getSearchedData(), context);
				}
				else {
					Log.i("onResponse", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {

				Log.i("onFailure", Objects.requireNonNull(t.getMessage()));
			}

		});

	}

	private void loadSearchReposList(String instanceUrl, String instanceToken, String loginUid, String searchKeyword, Boolean repoTypeInclude, String sort, String order, final Context context, boolean topic, boolean includeDesc, boolean template, boolean onlyArchived, boolean onlyPrivate, int limit) {

		Call<ExploreRepositories> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryRepos(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword, repoTypeInclude, sort, order, topic, includeDesc, template, onlyArchived, onlyPrivate, limit);

		call.enqueue(new Callback<ExploreRepositories>() {

			@Override
			public void onResponse(@NonNull Call<ExploreRepositories> call, @NonNull Response<ExploreRepositories> response) {

				if(response.isSuccessful()) {
					assert response.body() != null;
					getReposList(response.body().getSearchedData(), context);
				}
				else {
					Log.i("onResponse", String.valueOf(response.code()));
				}

			}

			@Override
			public void onFailure(@NonNull Call<ExploreRepositories> call, @NonNull Throwable t) {

				Log.i("onFailure", Objects.requireNonNull(t.getMessage()));
			}

		});

	}

	private void getReposList(List<UserRepositories> dataList, Context context) {

		ExploreRepositoriesAdapter adapter = new ExploreRepositoriesAdapter(dataList, context);

		mRecyclerView.setVisibility(View.VISIBLE);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		if(adapter.getItemCount() > 0) {

			mRecyclerView.setAdapter(adapter);
			noData.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);

		}
		else {

			noData.setVisibility(View.VISIBLE);
			mProgressBar.setVisibility(View.GONE);

		}

	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		menu.clear();
		inflater.inflate(R.menu.filter_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem filter = menu.findItem(R.id.filter);

		filter.setOnMenuItemClickListener(filter_ -> {

			showFilterOptions();
			return false;
		});

	}

	private void showFilterOptions() {

		dialogFilterOptions = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialogFilterOptions.getWindow() != null) {

			dialogFilterOptions.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		filterBinding = CustomExploreRepositoriesDialogBinding.inflate(LayoutInflater.from(ctx));

		View view = filterBinding.getRoot();
		dialogFilterOptions.setContentView(view);

		filterBinding.includeTopic.setOnClickListener(includeTopic -> {

			if(filterBinding.includeTopic.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeTopic", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeTopic", false);
			}
		});

		filterBinding.includeDesc.setOnClickListener(includeDesc -> {

			if(filterBinding.includeDesc.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeDescription", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeDescription", false);
			}
		});

		filterBinding.includeTemplate.setOnClickListener(includeTemplate -> {

			if(filterBinding.includeTemplate.isChecked()) {

				tinyDb.putBoolean("exploreRepoIncludeTemplate", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoIncludeTemplate", false);
			}
		});

		filterBinding.onlyArchived.setOnClickListener(onlyArchived -> {

			if(filterBinding.onlyArchived.isChecked()) {

				tinyDb.putBoolean("exploreRepoOnlyArchived", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoOnlyArchived", false);
			}
		});

		filterBinding.onlyPrivate.setOnClickListener(onlyPrivate -> {

			if(filterBinding.onlyPrivate.isChecked()) {

				tinyDb.putBoolean("exploreRepoOnlyPrivate", true);
			}
			else {

				tinyDb.putBoolean("exploreRepoOnlyPrivate", false);
			}
		});

		filterBinding.includeTopic.setChecked(tinyDb.getBoolean("exploreRepoIncludeTopic"));
		filterBinding.includeDesc.setChecked(tinyDb.getBoolean("exploreRepoIncludeDescription"));
		filterBinding.includeTemplate.setChecked(tinyDb.getBoolean("exploreRepoIncludeTemplate"));
		filterBinding.onlyArchived.setChecked(tinyDb.getBoolean("exploreRepoOnlyArchived"));
		filterBinding.onlyPrivate.setChecked(tinyDb.getBoolean("exploreRepoOnlyPrivate"));

		filterBinding.cancel.setOnClickListener(editProperties -> {
			dialogFilterOptions.dismiss();
		});

		dialogFilterOptions.show();
	}

	public void onButtonPressed(Uri uri) {

		if(mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {

		void onFragmentInteraction(Uri uri);

	}

}
