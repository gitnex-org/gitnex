package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author M M Arif
 */

public class RepositoriesByOrgFragment extends Fragment {

	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private final int resultLimit = Constants.resultLimitOldGiteaInstances;
	private static final String getOrgName = null;
	private String orgName;

    public RepositoriesByOrgFragment() { }

    public static RepositoriesByOrgFragment newInstance(String orgName) {
        RepositoriesByOrgFragment fragment = new RepositoriesByOrgFragment();
        Bundle args = new Bundle();
        args.putString(getOrgName, orgName);
        fragment.setArguments(args);
        return fragment;
    }

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orgName = getArguments().getString(getOrgName);
        }
    }

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding = FragmentRepositoriesBinding.inflate(inflater, container, false);

		setHasOptionsMenu(true);

		fragmentRepositoriesBinding.addNewRepo.setVisibility(View.GONE);

		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentRepositoriesBinding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		fragmentRepositoriesBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			fragmentRepositoriesBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());
			fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());

		return fragmentRepositoriesBinding.getRoot();
	};

	private void fetchDataAsync(String instanceToken) {

		RepositoriesViewModel reposModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		reposModel.getRepositories(instanceToken, page, resultLimit, "", "org", orgName, getContext()).observe(getViewLifecycleOwner(), reposListMain -> {

			adapter = new ReposListAdapter(reposListMain, getContext());
			adapter.setLoadMoreListener(new ReposListAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					RepositoriesViewModel.loadMoreRepos(instanceToken, page, resultLimit, "", "org", orgName, getContext(), adapter);
					fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
			}

			fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
		});
	}

    @Override
    public void onResume() {

        super.onResume();

        if(MainActivity.repoCreated) {
            RepositoriesViewModel.loadReposList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), page, resultLimit, null, "org", orgName, getContext());
	        MainActivity.repoCreated = false;
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(fragmentRepositoriesBinding.recyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}
