package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.activities.CreateOrganizationActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentNotesBinding;
import org.mian.gitnex.helpers.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class NotesFragment extends Fragment {

	private FragmentNotesBinding fragmentNotesBinding;
	private Context ctx;
	private MostVisitedReposAdapter adapter;
	private RepositoriesApi repositoriesApi;
	private List<Repository> notesList;
	private int currentActiveAccountId;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentNotesBinding = FragmentNotesBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navNotes));

		TinyDB tinyDb = TinyDB.getInstance(ctx);

		fragmentNotesBinding.newNote.setOnClickListener(view -> {
			Intent intent = new Intent(view.getContext(), CreateNoteActivity.class);
			startActivity(intent);
		});

		notesList = new ArrayList<>();
		repositoriesApi = BaseApi.getInstance(ctx, RepositoriesApi.class);

		fragmentNotesBinding.recyclerView.setHasFixedSize(true);
		fragmentNotesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		fragmentNotesBinding.recyclerView.setPadding(0, 0, 0, 220);
		fragmentNotesBinding.recyclerView.setClipToPadding(false);

		adapter = new MostVisitedReposAdapter(ctx, notesList);
		currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		fragmentNotesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			notesList.clear();
			fragmentNotesBinding.pullToRefresh.setRefreshing(false);
			fragmentNotesBinding.progressBar.setVisibility(View.VISIBLE);
			fetchDataAsync(currentActiveAccountId);
		}, 250));

		fetchDataAsync(currentActiveAccountId);

		return fragmentNotesBinding.getRoot();
	}

	private void fetchDataAsync(int accountId) {

		repositoriesApi.fetchAllMostVisited(accountId).observe(getViewLifecycleOwner(), mostVisitedRepos -> {

			fragmentNotesBinding.pullToRefresh.setRefreshing(false);
			assert mostVisitedRepos != null;
			if(mostVisitedRepos.size() > 0) {

				notesList.clear();
				fragmentNotesBinding.noData.setVisibility(View.GONE);
				notesList.addAll(mostVisitedRepos);
				adapter.notifyDataChanged();
				fragmentNotesBinding.recyclerView.setAdapter(adapter);
			}
			else {

				fragmentNotesBinding.noData.setVisibility(View.VISIBLE);
			}
			fragmentNotesBinding.progressBar.setVisibility(View.GONE);
		});
	}

	/*public void resetAllRepositoryCounter(int accountId) {

		if(mostVisitedReposList.size() > 0) {

			Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class)).resetAllRepositoryMostVisited(accountId);
			mostVisitedReposList.clear();
			adapter.notifyDataChanged();
			Toasty.success(ctx, getResources().getString(R.string.resetMostReposCounter));
		}
		else {
			Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
		}
	}*/

	/*@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.reset_menu, menu);
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				filter(newText);
				return false;
			}
		});
	}*/

	/*@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.reset_menu_item) {

			if(mostVisitedReposList.size() == 0) {
				Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
			}
			else {
				new MaterialAlertDialogBuilder(ctx).setTitle(R.string.reset).setMessage(R.string.resetCounterAllDialogMessage).setPositiveButton(R.string.reset, (dialog, which) -> {

					resetAllRepositoryCounter(currentActiveAccountId);
					dialog.dismiss();
				}).setNeutralButton(R.string.cancelButton, null).show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void filter(String text) {

		List<Repository> arr = new ArrayList<>();

		for(Repository d : mostVisitedReposList) {

			if(d == null || d.getRepositoryOwner() == null || d.getRepositoryName() == null) {
				continue;
			}

			if(d.getRepositoryOwner().toLowerCase().contains(text) || d.getRepositoryName().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}*/
}
