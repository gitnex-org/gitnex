package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.WikiListAdapter;
import org.mian.gitnex.databinding.FragmentWikiBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.DividerItemDecorator;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.WikiViewModel;

/**
 * @author M M Arif
 */

public class WikiFragment extends Fragment {

	private WikiViewModel wikiViewModel;
	private FragmentWikiBinding fragmentWikiBinding;
	private WikiListAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private RepositoryContext repository;

	public static WikiFragment newInstance(RepositoryContext repository) {
		WikiFragment fragment = new WikiFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentWikiBinding = FragmentWikiBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		wikiViewModel = new ViewModelProvider(this).get(WikiViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(getContext());

		fragmentWikiBinding.recyclerView.setHasFixedSize(true);
		fragmentWikiBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.shape_list_divider));
		fragmentWikiBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		fragmentWikiBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			fragmentWikiBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync(repository.getOwner(), repository.getName());
			fragmentWikiBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync(repository.getOwner(), repository.getName());

		return fragmentWikiBinding.getRoot();
	};

	private void fetchDataAsync(String owner, String repo) {

		wikiViewModel.getWiki(owner, repo, page, resultLimit, getContext(), fragmentWikiBinding).observe(getViewLifecycleOwner(), wikiListMain -> {

			adapter = new WikiListAdapter(wikiListMain, getContext(), owner, repo, fragmentWikiBinding);
			adapter.setLoadMoreListener(new WikiListAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					wikiViewModel.loadMoreWiki(repository.getOwner(), repository.getName(), page, resultLimit, getContext(), fragmentWikiBinding, adapter);
					fragmentWikiBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentWikiBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentWikiBinding.recyclerView.setAdapter(adapter);
				fragmentWikiBinding.noData.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentWikiBinding.recyclerView.setAdapter(adapter);
				fragmentWikiBinding.noData.setVisibility(View.VISIBLE);
			}

			fragmentWikiBinding.progressBar.setVisibility(View.GONE);
		});
	}
}
