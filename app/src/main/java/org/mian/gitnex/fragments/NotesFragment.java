package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.FragmentNotesBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;

/**
 * @author M M Arif
 */

public class NotesFragment extends Fragment {

	private FragmentNotesBinding fragmentNotesBinding;
	private Context ctx;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private List<Notes> notesList;
	private Intent noteIntent;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentNotesBinding = FragmentNotesBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navNotes));

		noteIntent = new Intent(ctx, CreateNoteActivity.class);

		fragmentNotesBinding.newNote.setOnClickListener(view -> {
			noteIntent.putExtra("action", "add");
			ctx.startActivity(noteIntent);
		});

		notesList = new ArrayList<>();
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		fragmentNotesBinding.recyclerView.setHasFixedSize(true);
		fragmentNotesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		fragmentNotesBinding.recyclerView.setPadding(0, 0, 0, 220);
		fragmentNotesBinding.recyclerView.setClipToPadding(false);

		adapter = new NotesAdapter(ctx, notesList);

		fragmentNotesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			notesList.clear();
			fragmentNotesBinding.pullToRefresh.setRefreshing(false);
			fragmentNotesBinding.progressBar.setVisibility(View.VISIBLE);
			fetchDataAsync();
		}, 250));

		fetchDataAsync();

		return fragmentNotesBinding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		notesList.clear();
		fetchDataAsync();
	}

	private void fetchDataAsync() {

		notesApi.fetchAllNotes().observe(getViewLifecycleOwner(), allNotes -> {

			fragmentNotesBinding.pullToRefresh.setRefreshing(false);
			assert allNotes != null;
			if(allNotes.size() > 0) {

				notesList.clear();
				fragmentNotesBinding.noData.setVisibility(View.GONE);
				notesList.addAll(allNotes);
				adapter.notifyDataChanged();
				fragmentNotesBinding.recyclerView.setAdapter(adapter);
			}
			else {

				fragmentNotesBinding.noData.setVisibility(View.VISIBLE);
			}
			fragmentNotesBinding.progressBar.setVisibility(View.GONE);
		});
	}

	private void filter(String text) {

		List<Notes> arr = new ArrayList<>();

		for(Notes d : notesList) {

			if(d == null || d.getContent() == null) {
				continue;
			}

			if(d.getContent().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

	public void deleteAllNotes() {

		if(notesList.size() > 0) {

			notesApi.deleteAllNotes();
			notesList.clear();
			adapter.notifyDataChanged();
			Toasty.success(ctx, ctx.getResources().getQuantityString(R.plurals.noteDeleteMessage, 2));
		}
		else {
			Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.reset_menu, menu);
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				filter(newText);
				return false;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.reset_menu_item) {

			if(notesList.size() == 0) {
				Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
			}
			else {
				new MaterialAlertDialogBuilder(ctx).setTitle(R.string.menuDeleteText)
					.setMessage(R.string.notesAllDeletionMessage)
					.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

					deleteAllNotes();
					dialog.dismiss();
				}).setNeutralButton(R.string.cancelButton, null).show();
			}
		}

		return super.onOptionsItemSelected(item);
	}
}
