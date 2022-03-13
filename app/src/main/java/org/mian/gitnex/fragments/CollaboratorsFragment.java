package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import org.gitnex.tea4j.models.Collaborators;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.CollaboratorsViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class CollaboratorsFragment extends Fragment {

	public static boolean refreshCollaborators = false;

    private ProgressBar mProgressBar;
    private CollaboratorsAdapter adapter;
    private GridView mGridView;
    private TextView noDataCollaborators;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

	    FragmentCollaboratorsBinding fragmentCollaboratorsBinding = FragmentCollaboratorsBinding.inflate(inflater, container, false);

        noDataCollaborators = fragmentCollaboratorsBinding.noDataCollaborators;
        mProgressBar = fragmentCollaboratorsBinding.progressBar;
        mGridView = fragmentCollaboratorsBinding.gridView;

        fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName());
        return fragmentCollaboratorsBinding.getRoot();

    }

    private void fetchDataAsync(String instanceToken, String owner, String repo) {

        CollaboratorsViewModel collaboratorsModel = new ViewModelProvider(this).get(CollaboratorsViewModel.class);

        collaboratorsModel.getCollaboratorsList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), new Observer<List<Collaborators>>() {
            @Override
            public void onChanged(@Nullable List<Collaborators> collaboratorsListMain) {
                adapter = new CollaboratorsAdapter(getContext(), collaboratorsListMain);
                if(adapter.getCount() > 0) {
                    mGridView.setAdapter(adapter);
                    noDataCollaborators.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mGridView.setAdapter(adapter);
                    noDataCollaborators.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

	@Override
	public void onResume() {

		super.onResume();
		if(refreshCollaborators) {
			fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization(), repository.getOwner(), repository.getName());
			refreshCollaborators = false;
		}
	}

}
