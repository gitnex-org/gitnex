package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.BottomsheetNotesPickerBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class BottomSheetNotesPicker extends BottomSheetDialogFragment {

	public interface OnNoteSelectedListener {
		void onNoteSelected(String noteContent);
	}

	private BottomsheetNotesPickerBinding binding;
	private OnNoteSelectedListener listener;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private final List<Notes> notesList = new ArrayList<>();

	public static BottomSheetNotesPicker newInstance() {
		return new BottomSheetNotesPicker();
	}

	public void setOnNoteSelectedListener(OnNoteSelectedListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		notesApi = BaseApi.getInstance(requireContext(), NotesApi.class);
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetNotesPickerBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupRecyclerView();

		if (notesApi.getCount() > 0) {
			fetchNotes();
		} else {
			Toasty.show(requireContext(), getString(R.string.noNotes));
			dismiss();
		}
	}

	private void setupRecyclerView() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setLayoutManager(layoutManager);

		adapter = new NotesAdapter(requireContext(), notesList, "insert", "issue");
		binding.recyclerView.setAdapter(adapter);

		adapter.setOnItemClickListener(
				note -> {
					if (listener != null && note.getContent() != null) {
						listener.onNoteSelected(note.getContent());
					}
					dismiss();
				});
	}

	private void fetchNotes() {
		notesApi.fetchAllNotes()
				.observe(
						getViewLifecycleOwner(),
						allNotes -> {
							if (allNotes != null && !allNotes.isEmpty()) {
								notesList.clear();
								notesList.addAll(allNotes);
								adapter.updateList(allNotes);
								binding.recyclerView.setVisibility(View.VISIBLE);
							} else {
								adapter.updateList(new ArrayList<>());
								binding.recyclerView.setVisibility(View.GONE);
							}
						});
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
