package org.mian.gitnex.fragments;

import android.content.Context;
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
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.DraftsAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.database.models.DraftWithRepository;
import org.mian.gitnex.databinding.FragmentDraftsBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author M M Arif
 */
public class DraftsFragment extends Fragment {

	private FragmentDraftsBinding fragmentDraftsBinding;
	private Context ctx;
	private DraftsAdapter adapter;
	private DraftsApi draftsApi;
	private List<DraftWithRepository> draftsList_;
	private int currentActiveAccountId;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentDraftsBinding = FragmentDraftsBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.titleDrafts));

		TinyDB tinyDb = TinyDB.getInstance(ctx);

		draftsList_ = new ArrayList<>();
		draftsApi = BaseApi.getInstance(ctx, DraftsApi.class);

		fragmentDraftsBinding.recyclerView.setHasFixedSize(true);
		fragmentDraftsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new DraftsAdapter(getContext(), getChildFragmentManager(), draftsList_);
		currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		fragmentDraftsBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											draftsList_.clear();
											fetchDataAsync(currentActiveAccountId);
										},
										250));

		fetchDataAsync(currentActiveAccountId);

		return fragmentDraftsBinding.getRoot();
	}

	private void fetchDataAsync(int accountId) {

		draftsApi
				.getDrafts(accountId)
				.observe(
						getViewLifecycleOwner(),
						drafts -> {
							fragmentDraftsBinding.pullToRefresh.setRefreshing(false);
							assert drafts != null;
							if (drafts.size() > 0) {

								draftsList_.clear();
								fragmentDraftsBinding.noData.setVisibility(View.GONE);
								draftsList_.addAll(drafts);
								adapter.notifyDataChanged();
								fragmentDraftsBinding.recyclerView.setAdapter(adapter);
							} else {

								fragmentDraftsBinding.noData.setVisibility(View.VISIBLE);
							}
						});
	}

	@Override
	public void onResume() {
		super.onResume();
		draftsList_.clear();
		fetchDataAsync(currentActiveAccountId);
	}

	public void deleteAllDrafts(int accountId) {

		if (draftsList_.size() > 0) {

			Objects.requireNonNull(BaseApi.getInstance(ctx, DraftsApi.class))
					.deleteAllDrafts(accountId);
			draftsList_.clear();
			adapter.notifyDataChanged();
			Toasty.success(ctx, getResources().getString(R.string.draftsDeleteSuccess));
		} else {
			Toasty.warning(ctx, getResources().getString(R.string.draftsListEmpty));
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(
				new SearchView.OnQueryTextListener() {

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
	}

	private void filter(String text) {

		List<DraftWithRepository> arr = new ArrayList<>();

		for (DraftWithRepository d : draftsList_) {

			if (d == null
					|| d.getRepositoryOwner() == null
					|| d.getRepositoryName() == null
					|| d.getDraftText() == null) {
				continue;
			}

			if (d.getRepositoryOwner().toLowerCase().contains(text)
					|| d.getRepositoryName().toLowerCase().contains(text)
					|| d.getDraftText().toLowerCase().contains(text)
					|| String.valueOf(d.getIssueId()).contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}
}
