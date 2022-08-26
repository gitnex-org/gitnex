package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
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

	public CollaboratorsFragment() {
	}

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
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentCollaboratorsBinding = FragmentCollaboratorsBinding.inflate(inflater, container, false);

		fetchDataAsync(repository.getOwner(), repository.getName());
		return fragmentCollaboratorsBinding.getRoot();

	}

	private void fetchDataAsync(String owner, String repo) {

		CollaboratorsViewModel collaboratorsModel = new ViewModelProvider(this).get(CollaboratorsViewModel.class);

		collaboratorsModel.getCollaboratorsList(owner, repo, getContext()).observe(getViewLifecycleOwner(), collaboratorsListMain -> {
			adapter = new CollaboratorsAdapter(getContext(), collaboratorsListMain);
			if(adapter.getCount() > 0) {
				fragmentCollaboratorsBinding.gridView.setAdapter(adapter);
				fragmentCollaboratorsBinding.noDataCollaborators.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataSetChanged();
				fragmentCollaboratorsBinding.gridView.setAdapter(adapter);
				fragmentCollaboratorsBinding.noDataCollaborators.setVisibility(View.VISIBLE);
			}
			fragmentCollaboratorsBinding.progressBar.setVisibility(View.GONE);
		});

	}

	@Override
	public void onResume() {

		super.onResume();
		if(refreshCollaborators) {
			fetchDataAsync(repository.getOwner(), repository.getName());
			refreshCollaborators = false;
		}
	}

}
