package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.LabelsAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.viewmodels.OrganizationLabelsViewModel;

/**
 * Author M M Arif
 */

public class OrganizationLabelsFragment extends Fragment {

	private ProgressBar mProgressBar;
	private RecyclerView mRecyclerView;
	private LabelsAdapter adapter;
	private TextView noData;
	private static final String repoOwnerF = "param1";
	private final String type = "org";

	private String repoOwner;

	private OnFragmentInteractionListener mListener;

	public static OrganizationLabelsFragment newInstance(String param1) {

		OrganizationLabelsFragment fragment = new OrganizationLabelsFragment();
		Bundle args = new Bundle();
		args.putString(repoOwnerF, param1);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (getArguments() != null) {

			repoOwner = getArguments().getString(repoOwnerF);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_labels, container, false);
		setHasOptionsMenu(true);

		final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);
		noData = v.findViewById(R.id.noData);

		mRecyclerView = v.findViewById(R.id.recyclerView);
		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		mProgressBar = v.findViewById(R.id.progress_bar);

		swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			swipeRefresh.setRefreshing(false);
			OrganizationLabelsViewModel.loadOrgLabelsList(Authorization.get(getContext()), repoOwner, getContext(), mProgressBar, noData);

		}, 200));

		fetchDataAsync(Authorization.get(getContext()), repoOwner);

		return v;

	}

	@Override
	public void onResume() {

		super.onResume();
		final TinyDB tinyDb = TinyDB.getInstance(getContext());

		if(tinyDb.getBoolean("labelsRefresh")) {

			OrganizationLabelsViewModel.loadOrgLabelsList(Authorization.get(getContext()), repoOwner, getContext(), mProgressBar, noData);
			tinyDb.putBoolean("labelsRefresh", false);
		}
	}

	public void onButtonPressed(Uri uri) {

		if (mListener != null) {

			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {

		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {

		void onFragmentInteraction(Uri uri);
	}

	private void fetchDataAsync(String instanceToken, String owner) {

		OrganizationLabelsViewModel organizationLabelsViewModel = new ViewModelProvider(this).get(OrganizationLabelsViewModel.class);

		organizationLabelsViewModel.getOrgLabelsList(instanceToken, owner, getContext(), mProgressBar, noData).observe(getViewLifecycleOwner(), labelsListMain -> {

			adapter = new LabelsAdapter(getContext(), labelsListMain, type, owner);

			if(adapter.getItemCount() > 0) {

				mRecyclerView.setAdapter(adapter);
				noData.setVisibility(View.GONE);
			}
			else {

				adapter.notifyDataSetChanged();
				mRecyclerView.setAdapter(adapter);
				noData.setVisibility(View.VISIBLE);
			}

			mProgressBar.setVisibility(View.GONE);
		});

	}
}
