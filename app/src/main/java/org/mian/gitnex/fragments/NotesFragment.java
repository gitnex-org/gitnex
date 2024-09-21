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
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.CreateNoteActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.NotesApi;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.FragmentNotesBinding;
import org.mian.gitnex.helpers.Toasty;

/**
 * @author M M Arif
 */
public class NotesFragment extends Fragment {

	private FragmentNotesBinding binding;
	private Context ctx;
	private NotesAdapter adapter;
	private NotesApi notesApi;
	private List<Notes> notesList;
	private Intent noteIntent;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentNotesBinding.inflate(inflater, container, false);

		ctx = getContext();

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
								assert searchView != null;
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
												return false;
											}
										});
							}

							@Override
							public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {

								if (menuItem.getItemId() == R.id.reset_menu_item) {

									if (notesList.isEmpty()) {
										Toasty.warning(
												ctx,
												getResources().getString(R.string.noDataFound));
									} else {
										new MaterialAlertDialogBuilder(ctx)
												.setTitle(R.string.menuDeleteText)
												.setMessage(R.string.notesAllDeletionMessage)
												.setPositiveButton(
														R.string.menuDeleteText,
														(dialog, which) -> {
															deleteAllNotes();
															dialog.dismiss();
														})
												.setNeutralButton(R.string.cancelButton, null)
												.show();
									}
								}
								return false;
							}
						},
						getViewLifecycleOwner(),
						Lifecycle.State.RESUMED);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.navNotes));

		noteIntent = new Intent(ctx, CreateNoteActivity.class);

		binding.newNote.setOnClickListener(
				view -> {
					noteIntent.putExtra("action", "add");
					ctx.startActivity(noteIntent);
				});

		notesList = new ArrayList<>();
		notesApi = BaseApi.getInstance(ctx, NotesApi.class);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		binding.recyclerView.setPadding(0, 0, 0, 220);
		binding.recyclerView.setClipToPadding(false);

		adapter = new NotesAdapter(ctx, notesList, "", "");

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											notesList.clear();
											binding.pullToRefresh.setRefreshing(false);
											binding.progressBar.setVisibility(View.VISIBLE);
											fetchDataAsync();
										},
										250));

		fetchDataAsync();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		fetchDataAsync();
	}

	private void fetchDataAsync() {

		notesApi.fetchAllNotes()
				.observe(
						getViewLifecycleOwner(),
						allNotes -> {
							binding.pullToRefresh.setRefreshing(false);
							assert allNotes != null;
							if (!allNotes.isEmpty()) {

								notesList.clear();
								binding.noData.setVisibility(View.GONE);
								notesList.addAll(allNotes);
								adapter.notifyDataChanged();
								binding.recyclerView.setAdapter(adapter);
							} else {

								binding.noData.setVisibility(View.VISIBLE);
							}
							binding.progressBar.setVisibility(View.GONE);
						});
	}

	private void filter(String text) {

		List<Notes> arr = new ArrayList<>();

		for (Notes d : notesList) {

			if (d == null || d.getContent() == null) {
				continue;
			}

			if (d.getContent().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

	public void deleteAllNotes() {

		if (!notesList.isEmpty()) {

			notesApi.deleteAllNotes();
			notesList.clear();
			adapter.clearAdapter();
			Toasty.success(
					ctx, ctx.getResources().getQuantityString(R.plurals.noteDeleteMessage, 2));
		} else {
			Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
		}
	}
}
