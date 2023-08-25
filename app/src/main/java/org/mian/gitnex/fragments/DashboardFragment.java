package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.NotesAdapter;
import org.mian.gitnex.database.models.Notes;
import org.mian.gitnex.databinding.FragmentNotesBinding;

/**
 * @author M M Arif
 */
public class DashboardFragment extends Fragment {

	private FragmentNotesBinding binding;
	private Context ctx;
	private NotesAdapter adapter;
	private List<Notes> notesList;

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentNotesBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity())
				.setActionBarTitle(getResources().getString(R.string.dashboard));

		notesList = new ArrayList<>();

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		binding.recyclerView.setPadding(0, 0, 0, 220);
		binding.recyclerView.setClipToPadding(false);

		adapter = new NotesAdapter(ctx, notesList);

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											notesList.clear();
											binding.pullToRefresh.setRefreshing(false);
											binding.progressBar.setVisibility(View.VISIBLE);
											// fetchDataAsync();
										},
										250));

		// fetchDataAsync();

		return binding.getRoot();
	}

	@Override
	public void onResume() {
		super.onResume();
		// fetchDataAsync();
	}
}
