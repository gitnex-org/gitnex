package org.mian.gitnex.fragments;

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
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.adapters.MyProfileEmailsAdapter;
import org.mian.gitnex.databinding.FragmentProfileEmailsBinding;
import org.mian.gitnex.viewmodels.ProfileEmailsViewModel;

/**
 * Author M M Arif
 */

public class MyProfileEmailsFragment extends Fragment {

	public static boolean refreshEmails = false;

    private ProgressBar mProgressBar;
    private MyProfileEmailsAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataEmails;

    public MyProfileEmailsFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

	    FragmentProfileEmailsBinding fragmentProfileEmailsBinding = FragmentProfileEmailsBinding.inflate(inflater, container, false);

        final SwipeRefreshLayout swipeRefresh = fragmentProfileEmailsBinding.pullToRefresh;

        noDataEmails = fragmentProfileEmailsBinding.noDataEmails;
        mRecyclerView = fragmentProfileEmailsBinding.recyclerView;

	    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), DividerItemDecoration.VERTICAL);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = fragmentProfileEmailsBinding.progressBar;

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            ProfileEmailsViewModel.loadEmailsList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), getContext());

        }, 200));

        fetchDataAsync(((BaseActivity) requireActivity()).getAccount().getAuthorization());

        return fragmentProfileEmailsBinding.getRoot();

    }

    private void fetchDataAsync(String instanceToken) {

        ProfileEmailsViewModel profileEmailModel = new ViewModelProvider(this).get(ProfileEmailsViewModel.class);

        profileEmailModel.getEmailsList(instanceToken, getContext()).observe(getViewLifecycleOwner(), emailsListMain -> {
            adapter = new MyProfileEmailsAdapter(getContext(), emailsListMain);
            if(adapter.getItemCount() > 0) {
                mRecyclerView.setAdapter(adapter);
                noDataEmails.setVisibility(View.GONE);
            }
            else {
                adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);
                noDataEmails.setVisibility(View.VISIBLE);
            }
            mProgressBar.setVisibility(View.GONE);
        });

    }

	@Override
	public void onResume() {

		super.onResume();

		if(refreshEmails) {
			ProfileEmailsViewModel.loadEmailsList(((BaseActivity) requireActivity()).getAccount().getAuthorization(), getContext());
			refreshEmails = false;
		}
	}

}
