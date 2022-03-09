package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.models.FileDiffView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DiffAdapter;
import org.mian.gitnex.databinding.FragmentDiffBinding;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author opyale
 */

public class DiffFragment extends Fragment {

	private FragmentDiffBinding binding;
	private Context ctx;

	private FileDiffView fileDiffView;
	private String type;

	public DiffFragment() {}

	public void setFileDiffView(FileDiffView fileDiffView) {
		this.fileDiffView = fileDiffView;
	}

	public static DiffFragment newInstance(FileDiffView fileDiffView, String type) {

		DiffFragment fragment = new DiffFragment();
		fragment.setFileDiffView(fileDiffView);
		fragment.type = type;
		return fragment;

	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentDiffBinding.inflate(inflater, container, false);
		ctx = requireContext();

		if(Objects.equals(type, "pull")) {
			binding.close.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, DiffFilesFragment.newInstance()).commit());
		} else {
			binding.close.setOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, CommitDetailFragment.newInstance()).commit());
		}

		binding.toolbarTitle.setText(fileDiffView.getFileName());
		binding.diff.setDivider(null);
		binding.diff.setAdapter(new DiffAdapter(ctx, getChildFragmentManager(), Arrays.asList(fileDiffView.toString().split("\\R")), type));

		return binding.getRoot();

	}
}
