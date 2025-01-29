package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import org.mian.gitnex.activities.AddCollaboratorToRepositoryActivity;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.CollaboratorsViewModel;

/**
 * @author M M Arif
 */
public class CollaboratorsFragment extends Fragment {

	public static boolean refreshCollaborators = false;
	private FragmentCollaboratorsBinding fragmentCollaboratorsBinding;
	private CollaboratorsAdapter adapter;
	private RepositoryContext repository;
	private CollaboratorsViewModel collaboratorsModel;
	private int page = 1;
	private int resultLimit;

	public CollaboratorsFragment() {}

	public static CollaboratorsFragment newInstance(RepositoryContext repository) {
		CollaboratorsFragment fragment = new CollaboratorsFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentCollaboratorsBinding =
				FragmentCollaboratorsBinding.inflate(inflater, container, false);

		collaboratorsModel = new ViewModelProvider(this).get(CollaboratorsViewModel.class);
		resultLimit = Constants.getCurrentResultLimit(requireContext());

		if (repository.getPermissions().isAdmin()) {

			fragmentCollaboratorsBinding.addCollaborator.setOnClickListener(
					v1 -> {
						startActivity(
								repository.getIntent(
										getContext(), AddCollaboratorToRepositoryActivity.class));
					});
		} else {

			fragmentCollaboratorsBinding.addCollaborator.setVisibility(View.GONE);
		}

		fetchDataAsync(repository.getOwner(), repository.getName());
		return fragmentCollaboratorsBinding.getRoot();
	}

	private void fetchDataAsync(String repoOwner, String repoName) {

		collaboratorsModel
				.getCollaboratorsList(repoOwner, repoName, requireContext(), page, resultLimit)
				.observe(
						getViewLifecycleOwner(),
						mainList -> {
							adapter = new CollaboratorsAdapter(requireContext(), mainList);

							adapter.setLoadMoreListener(
									new CollaboratorsAdapter.OnLoadMoreListener() {

										@Override
										public void onLoadMore() {

											page += 1;
											collaboratorsModel.loadMore(
													repoOwner,
													repoName,
													requireContext(),
													page,
													resultLimit,
													adapter,
													fragmentCollaboratorsBinding);
											fragmentCollaboratorsBinding.progressBar.setVisibility(
													View.VISIBLE);
										}

										@Override
										public void onLoadFinished() {

											fragmentCollaboratorsBinding.progressBar.setVisibility(
													View.GONE);
										}
									});

							GridLayoutManager layoutManager =
									new GridLayoutManager(requireContext(), 2);
							fragmentCollaboratorsBinding.gridView.setLayoutManager(layoutManager);

							if (adapter.getItemCount() > 0) {
								fragmentCollaboratorsBinding.gridView.setAdapter(adapter);
								fragmentCollaboratorsBinding.noDataCollaborators.setVisibility(
										View.GONE);
							} else {
								adapter.notifyDataChanged();
								fragmentCollaboratorsBinding.gridView.setAdapter(adapter);
								fragmentCollaboratorsBinding.noDataCollaborators.setVisibility(
										View.VISIBLE);
							}

							fragmentCollaboratorsBinding.progressBar.setVisibility(View.GONE);
						});
	}

	@Override
	public void onResume() {

		super.onResume();
		if (refreshCollaborators) {
			fetchDataAsync(repository.getOwner(), repository.getName());
			refreshCollaborators = false;
		}
	}
}
