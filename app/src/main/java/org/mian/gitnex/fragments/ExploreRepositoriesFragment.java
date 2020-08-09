package org.mian.gitnex.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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
 * Template Author Author M M Arif
 * Author 6543
 */

public class ExploreRepositoriesFragment extends Fragment {

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
		//setHasOptionsMenu(true);

		TinyDB tinyDb = new TinyDB(getContext());
		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

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
					loadSearchReposList(instanceUrl, instanceToken, loginUid, searchKeyword.getText().toString(), repoTypeInclude, sort, order, getContext(), limit);
				}
			}
			return false;
		});

		int limitDefault = 25;
		loadDefaultList(instanceUrl, instanceToken, loginUid, repoTypeInclude, sort, order, getContext(), limitDefault);

		return v;

	}

	private void loadDefaultList(String instanceUrl, String instanceToken, String loginUid, Boolean repoTypeInclude, String sort, String order, final Context context, int limit) {

		Call<ExploreRepositories> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryRepos(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), null, repoTypeInclude, sort, order, limit);

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

	private void loadSearchReposList(String instanceUrl, String instanceToken, String loginUid, String searchKeyword, Boolean repoTypeInclude, String sort, String order, final Context context, int limit) {

		Call<ExploreRepositories> call = RetrofitClient.getInstance(instanceUrl, getContext()).getApiInterface().queryRepos(Authorization.returnAuthentication(getContext(), loginUid, instanceToken), searchKeyword, repoTypeInclude, sort, order, limit);

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
