package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.ActivityNotesBinding;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;

/**
 * @author mmarif
 */
public class NotesActivity extends BaseActivity {

	private ActivityNotesBinding binding;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private final List<Notes> notesList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityNotesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(
				this, binding.dockedToolbar, binding.recyclerView, binding.pullToRefresh, null);

		notesApi = BaseApi.getInstance(this, NotesApi.class);

		setupRecyclerView();
		setupDockActions();
		setupSearchOverlay();

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					fetchDataAsync();
				});

		fetchDataAsync();
	}

	private void setupRecyclerView() {
		adapter = new NotesAdapter(this, notesList, "", "");
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setAdapter(adapter);
	}

	private void setupDockActions() {
		binding.close.setOnClickListener(v -> finish());

		binding.newNote.setOnClickListener(
				v -> {
					Intent intent = new Intent(this, CreateNoteActivity.class);
					intent.putExtra("action", "add");
					startActivity(intent);
				});

		binding.actionSearchDock.setOnClickListener(v -> binding.searchView.show());
		binding.deleteAllNotes.setOnClickListener(v -> handleDeleteAll());
	}

	private void setupSearchOverlay() {
		binding.searchResultsRecycler.setAdapter(adapter);

		binding.searchView
				.getEditText()
				.addTextChangedListener(
						new TextWatcher() {
							@Override
							public void onTextChanged(
									CharSequence s, int start, int before, int count) {
								filter(s.toString().trim());
							}

							@Override
							public void beforeTextChanged(
									CharSequence s, int start, int count, int after) {}

							@Override
							public void afterTextChanged(Editable s) {}
						});

		binding.searchView.addTransitionListener(
				(searchView, previousState, newState) -> {
					if (newState
							== com.google.android.material.search.SearchView.TransitionState
									.HIDDEN) {
						adapter.updateList(notesList);
						binding.recyclerView.scrollToPosition(0);
					}
				});
	}

	@SuppressLint("NotifyDataSetChanged")
	private void fetchDataAsync() {
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		notesApi.fetchAllNotes()
				.observe(
						this,
						allNotes -> {
							binding.expressiveLoader.setVisibility(View.GONE);
							notesList.clear();

							if (allNotes != null && !allNotes.isEmpty()) {
								notesList.addAll(allNotes);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
								binding.recyclerView.setVisibility(View.VISIBLE);
							} else {
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
								binding.recyclerView.setVisibility(View.GONE);
							}

							adapter.notifyDataSetChanged();
						});
	}

	private void filter(String text) {
		List<Notes> filtered = new ArrayList<>();
		for (Notes n : notesList) {
			if (n.getContent() != null
					&& n.getContent().toLowerCase().contains(text.toLowerCase())) {
				filtered.add(n);
			}
		}
		adapter.updateList(filtered);

		binding.layoutEmpty.getRoot().setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
	}

	private void handleDeleteAll() {
		if (notesList.isEmpty()) {
			Toasty.show(this, getString(R.string.empty_state_title));
			return;
		}
		new MaterialAlertDialogBuilder(this)
				.setTitle(R.string.menuDeleteText)
				.setMessage(R.string.notesAllDeletionMessage)
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) -> {
							notesApi.deleteAllNotes();
							fetchDataAsync();
							Toasty.show(
									this,
									getResources()
											.getQuantityString(R.plurals.noteDeleteMessage, 2));
						})
				.setNeutralButton(R.string.cancelButton, null)
				.show();
	}

	@Override
	public void onResume() {
		super.onResume();
		fetchDataAsync();
	}
}
