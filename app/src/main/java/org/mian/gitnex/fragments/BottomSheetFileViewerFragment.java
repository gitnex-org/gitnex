package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import org.mian.gitnex.databinding.BottomSheetFileViewerBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.structs.BottomSheetListener;

/**
 * @author M M Arif
 */

public class BottomSheetFileViewerFragment extends BottomSheetDialogFragment {

	private BottomSheetListener bmListener;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		RepositoryContext repository = RepositoryContext.fromBundle(requireArguments());
		BottomSheetFileViewerBinding bottomSheetFileViewerBinding = BottomSheetFileViewerBinding.inflate(inflater, container, false);

		if(!repository.getPermissions().isPush()) {
			bottomSheetFileViewerBinding.deleteFile.setVisibility(View.GONE);
			bottomSheetFileViewerBinding.editFile.setVisibility(View.GONE);
		}
		else if(!requireArguments().getBoolean("editable")) {
			bottomSheetFileViewerBinding.editFile.setVisibility(View.GONE);
		}

		bottomSheetFileViewerBinding.downloadFile.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("downloadFile");
			dismiss();
		});

		bottomSheetFileViewerBinding.deleteFile.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("deleteFile");
			dismiss();
		});

		bottomSheetFileViewerBinding.editFile.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("editFile");
			dismiss();
		});

		bottomSheetFileViewerBinding.copyUrl.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("copyUrl");
			dismiss();
		});

		bottomSheetFileViewerBinding.share.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("share");
			dismiss();
		});

		bottomSheetFileViewerBinding.open.setOnClickListener(v1 -> {

			bmListener.onButtonClicked("open");
			dismiss();
		});

		return bottomSheetFileViewerBinding.getRoot();
	}

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

}
