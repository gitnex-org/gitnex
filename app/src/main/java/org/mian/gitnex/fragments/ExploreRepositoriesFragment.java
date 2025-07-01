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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import org.gitnex.tea4j.v2.models.Repository;
import org.gitnex.tea4j.v2.models.SearchResults;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ExploreRepositoriesAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.BottomSheetExploreFiltersBinding;
import org.mian.gitnex.databinding.FragmentExploreRepoBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SnackBar;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class ExploreRepositoriesFragment extends Fragment {

	private final boolean repoTypeInclude = true;
	private final String sort = "updated";
	private final String order = "desc";
	private FragmentExploreRepoBinding viewBinding;
	private Context context;
	private int pageSize;
	private int resultLimit;
	private List<Repository> dataList;
	private ExploreRepositoriesAdapter adapter;
	private boolean includeTopic = false;
	private boolean includeDescription = false;
	private boolean includeTemplate = false;
	private boolean onlyArchived = false;
	private String searchQuery = "";

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		viewBinding = FragmentExploreRepoBinding.inflate(inflater, container, false);

		context = getContext();
		dataList = new ArrayList<>();
		adapter = new ExploreRepositoriesAdapter(dataList, context);

		resultLimit = Constants.getCurrentResultLimit(context);

		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menu.clear();
								menuInflater.inflate(R.menu.search_menu, menu);
								menuInflater.inflate(R.menu.generic_nav_dotted_menu, menu);

								MenuItem filterItem = menu.findItem(R.id.genericMenu);
								filterItem.setOnMenuItemClickListener(
										item -> {
											showFilterBottomSheet();
											return true;
										});

								MenuItem searchItem = menu.findItem(R.id.action_search);
								androidx.appcompat.widget.SearchView searchView =
										(androidx.appcompat.widget.SearchView)
												searchItem.getActionView();
								assert searchView != null;
								searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

								searchView.setOnQueryTextListener(
										new androidx.appcompat.widget.SearchView
												.OnQueryTextListener() {
											@Override
											public boolean onQueryTextSubmit(String query) {
												viewBinding.progressBar.setVisibility(View.VISIBLE);
												loadInitial(query, resultLimit);
												adapter.setLoadMoreListener(
														() ->
																viewBinding.recyclerViewReposSearch
																		.post(
																				() -> {
																					if (dataList
																											.size()
																									== resultLimit
																							|| pageSize
																									== resultLimit) {
																						int page =
																								(dataList
																														.size()
																												+ resultLimit)
																										/ resultLimit;
																						loadMore(
																								query,
																								resultLimit,
																								page);
																					}
																				}));
												searchQuery = query;
												searchView.setQuery(null, false);
												searchItem.collapseActionView();
												return true;
											}

											@Override
											public boolean onQueryTextChange(String newText) {
												return false;
											}
										});
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		viewBinding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											viewBinding.pullToRefresh.setRefreshing(false);
											loadInitial("", resultLimit);
											adapter.notifyDataChanged();
										},
										200));

		adapter.setLoadMoreListener(
				() ->
						viewBinding.recyclerViewReposSearch.post(
								() -> {
									if (dataList.size() == resultLimit || pageSize == resultLimit) {
										int page = (dataList.size() + resultLimit) / resultLimit;
										loadMore("", resultLimit, page);
									}
								}));

		viewBinding.recyclerViewReposSearch.setHasFixedSize(true);
		viewBinding.recyclerViewReposSearch.setLayoutManager(new LinearLayoutManager(context));
		viewBinding.recyclerViewReposSearch.setAdapter(adapter);

		loadInitial("", resultLimit);

		return viewBinding.getRoot();
	}

	private void loadInitial(String searchKeyword, int resultLimit) {
		Call<SearchResults> call =
				RetrofitClient.getApiInterface(context)
						.repoSearch(
								searchKeyword,
								includeTopic,
								includeDescription,
								null,
								null,
								null,
								null,
								repoTypeInclude,
								null,
								includeTemplate,
								onlyArchived,
								null,
								null,
								sort,
								order,
								1,
								resultLimit);
		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<SearchResults> call,
							@NonNull Response<SearchResults> response) {
						if (response.isSuccessful()) {
							if (response.body() != null && !response.body().getData().isEmpty()) {
								dataList.clear();
								dataList.addAll(response.body().getData());
								adapter.notifyDataChanged();
								viewBinding.noData.setVisibility(View.GONE);
							} else {
								dataList.clear();
								adapter.notifyDataChanged();
								viewBinding.noData.setVisibility(View.VISIBLE);
							}
							viewBinding.progressBar.setVisibility(View.GONE);
						} else if (response.code() == 404) {
							viewBinding.noData.setVisibility(View.VISIBLE);
							viewBinding.progressBar.setVisibility(View.GONE);
						} else {
							Toasty.error(requireActivity(), getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {
						Toasty.error(
								requireActivity(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void loadMore(String searchKeyword, int resultLimit, int page) {
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		Call<SearchResults> call =
				RetrofitClient.getApiInterface(context)
						.repoSearch(
								searchKeyword,
								includeTopic,
								includeDescription,
								null,
								null,
								null,
								null,
								repoTypeInclude,
								null,
								includeTemplate,
								onlyArchived,
								null,
								null,
								sort,
								order,
								page,
								resultLimit);

		call.enqueue(
				new Callback<>() {
					@Override
					public void onResponse(
							@NonNull Call<SearchResults> call,
							@NonNull Response<SearchResults> response) {
						if (response.isSuccessful()) {
							assert response.body() != null;
							List<Repository> result = response.body().getData();
							if (!result.isEmpty()) {
								pageSize = result.size();
								dataList.addAll(result);
							} else {
								SnackBar.info(
										context,
										viewBinding.getRoot(),
										getString(R.string.noMoreData));
								adapter.setMoreDataAvailable(false);
							}
							adapter.notifyDataChanged();
							viewBinding.progressBar.setVisibility(View.GONE);
						} else {
							Toasty.error(requireActivity(), getString(R.string.genericError));
						}
					}

					@Override
					public void onFailure(@NonNull Call<SearchResults> call, @NonNull Throwable t) {
						Toasty.error(
								requireActivity(), getString(R.string.genericServerResponseError));
					}
				});
	}

	private void showFilterBottomSheet() {
		BottomSheetFilterFragment bottomSheet =
				BottomSheetFilterFragment.newInstance(
						includeTopic,
						includeDescription,
						includeTemplate,
						onlyArchived,
						(topic, desc, template, archived) -> {
							includeTopic = topic;
							includeDescription = desc;
							includeTemplate = template;
							onlyArchived = archived;
							loadInitial(searchQuery, resultLimit);
						});
		bottomSheet.show(getChildFragmentManager(), "exploreFiltersBottomSheet");
	}

	@Override
	public void onResume() {
		super.onResume();
		if (MainActivity.reloadRepos) {
			dataList.clear();
			loadInitial(searchQuery, resultLimit);
			MainActivity.reloadRepos = false;
		}
	}

	public static class BottomSheetFilterFragment extends BottomSheetDialogFragment {

		private static final String ARG_INCLUDE_TOPIC = "includeTopic";
		private static final String ARG_INCLUDE_DESC = "includeDescription";
		private static final String ARG_INCLUDE_TEMPLATE = "includeTemplate";
		private static final String ARG_ONLY_ARCHIVED = "onlyArchived";
		private FilterCallback callback;

		public static BottomSheetFilterFragment newInstance(
				boolean includeTopic,
				boolean includeDescription,
				boolean includeTemplate,
				boolean onlyArchived,
				FilterCallback callback) {
			BottomSheetFilterFragment fragment = new BottomSheetFilterFragment();
			Bundle args = new Bundle();
			args.putBoolean(ARG_INCLUDE_TOPIC, includeTopic);
			args.putBoolean(ARG_INCLUDE_DESC, includeDescription);
			args.putBoolean(ARG_INCLUDE_TEMPLATE, includeTemplate);
			args.putBoolean(ARG_ONLY_ARCHIVED, onlyArchived);
			fragment.setArguments(args);
			fragment.callback = callback;
			return fragment;
		}

		@Override
		public View onCreateView(
				@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			BottomSheetExploreFiltersBinding binding =
					BottomSheetExploreFiltersBinding.inflate(inflater, container, false);

			Bundle args = getArguments();
			boolean includeTopic = args != null && args.getBoolean(ARG_INCLUDE_TOPIC, false);
			boolean includeDescription = args != null && args.getBoolean(ARG_INCLUDE_DESC, false);
			boolean includeTemplate = args != null && args.getBoolean(ARG_INCLUDE_TEMPLATE, false);
			boolean onlyArchived = args != null && args.getBoolean(ARG_ONLY_ARCHIVED, false);

			binding.includeTopicChip.setChecked(includeTopic);
			binding.includeDescChip.setChecked(includeDescription);
			binding.includeTemplateChip.setChecked(includeTemplate);
			binding.onlyArchivedChip.setChecked(onlyArchived);

			binding.filterChipGroup.setOnCheckedStateChangeListener(
					(group, checkedIds) -> {
						boolean newIncludeTopic = checkedIds.contains(R.id.includeTopicChip);
						boolean newIncludeDescription = checkedIds.contains(R.id.includeDescChip);
						boolean newIncludeTemplate = checkedIds.contains(R.id.includeTemplateChip);
						boolean newOnlyArchived = checkedIds.contains(R.id.onlyArchivedChip);
						if (callback != null) {
							callback.onFiltersApplied(
									newIncludeTopic,
									newIncludeDescription,
									newIncludeTemplate,
									newOnlyArchived);
						}
					});

			return binding.getRoot();
		}

		public interface FilterCallback {
			void onFiltersApplied(
					boolean includeTopic,
					boolean includeDescription,
					boolean includeTemplate,
					boolean onlyArchived);
		}
	}
}
