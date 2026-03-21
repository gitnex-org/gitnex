package org.mian.gitnex.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.FragmentNotesBinding;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author mmarif
 */
public class NotesFragment extends Fragment {

	private FragmentNotesBinding binding;
	private Context ctx;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private final List<Notes> notesList = new ArrayList<>();

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentNotesBinding.inflate(inflater, container, false);
		ctx = getContext();
		notesApi = BaseApi.getInstance(requireContext(), NotesApi.class);

		setupRecyclerView();
		setupMenu();
		setupFab();

		binding.pullToRefresh.setOnRefreshListener(
				() -> {
					binding.pullToRefresh.setRefreshing(false);
					fetchDataAsync();
				});

		fetchDataAsync();
		return binding.getRoot();
	}

	private void setupRecyclerView() {
		adapter = new NotesAdapter(ctx, notesList, "", "");
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		binding.recyclerView.setAdapter(adapter);

		binding.recyclerView.setPadding(0, 0, 0, 220);
		binding.recyclerView.setClipToPadding(false);
	}

	private void setupFab() {
		binding.newNote.setOnClickListener(
				v -> {
					Intent intent = new Intent(ctx, CreateNoteActivity.class);
					intent.putExtra("action", "add");
					startActivity(intent);
				});
	}

	@SuppressLint("NotifyDataSetChanged")
	private void fetchDataAsync() {
		binding.expressiveLoader.setVisibility(View.VISIBLE);
		notesApi.fetchAllNotes()
				.observe(
						getViewLifecycleOwner(),
						allNotes -> {
							binding.expressiveLoader.setVisibility(View.GONE);
							notesList.clear();
							if (allNotes != null && !allNotes.isEmpty()) {
								notesList.addAll(allNotes);
								binding.layoutEmpty.getRoot().setVisibility(View.GONE);
							} else {
								binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
							}
							adapter.notifyDataSetChanged();
						});
	}

	private void setupMenu() {
		requireActivity()
				.addMenuProvider(
						new MenuProvider() {
							@Override
							public void onCreateMenu(
									@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
								menuInflater.inflate(R.menu.reset_menu, menu);
								menuInflater.inflate(R.menu.search_menu, menu);

								MenuItem searchItem = menu.findItem(R.id.action_search);
								SearchView searchView = (SearchView) searchItem.getActionView();
								if (searchView != null) {
									searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
									searchView.setOnQueryTextListener(
											new SearchView.OnQueryTextListener() {
												@Override
												public boolean onQueryTextSubmit(String query) {
													return false;
												}

												@Override
												public boolean onQueryTextChange(String newText) {
													filter(newText);
													return true;
												}
											});
								}
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
								if (menuItem.getItemId() == R.id.reset_menu_item) {
									handleDeleteAll();
									return true;
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);
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
	}

	private void handleDeleteAll() {
		if (notesList.isEmpty()) {
			Toasty.show(ctx, getString(R.string.empty_state_title));
			return;
		}
		new MaterialAlertDialogBuilder(ctx)
				.setTitle(R.string.menuDeleteText)
				.setMessage(R.string.notesAllDeletionMessage)
				.setPositiveButton(
						R.string.menuDeleteText,
						(dialog, which) -> {
							notesApi.deleteAllNotes();
							fetchDataAsync();
							Toasty.show(
									ctx,
									ctx.getResources()
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
