package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetReleasesTagsBinding;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author opyale
 */

public class BottomSheetReleasesTagsFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Override
	public void onAttach(@NonNull Context context) {

		super.onAttach(context);

		try {
			bmListener = (BottomSheetListener) context;
		}
		catch(ClassCastException e) {
			throw new ClassCastException(context + " must implement BottomSheetListener");
		}
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		BottomSheetReleasesTagsBinding binding = BottomSheetReleasesTagsBinding.inflate(inflater, container, false);

		binding.tags.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("tags");
			dismiss();
		});

		binding.releases.setOnClickListener(v12 -> {

			bmListener.onButtonClicked("releases");
			dismiss();
		});

		return binding.getRoot();
	}

}
