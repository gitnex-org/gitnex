package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.FileViewActivity;
import org.mian.gitnex.adapters.FilesAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.Files;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.FilesViewModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

/**
 * Author M M Arif
 */

public class FilesFragment extends Fragment implements FilesAdapter.FilesAdapterListener {

    private ProgressBar mProgressBar;
    private FilesAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataFiles;
    private LinearLayout filesFrame;
    private TextView fileStructure;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";
    private BreadcrumbsView mBreadcrumbsView;

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public FilesFragment() {
    }

    public static FilesFragment newInstance(String param1, String param2) {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        args.putString(repoOwnerF, param1);
        args.putString(repoNameF, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoName = getArguments().getString(repoNameF);
            repoOwner = getArguments().getString(repoOwnerF);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_files, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        noDataFiles = v.findViewById(R.id.noDataFiles);
        filesFrame = v.findViewById(R.id.filesFrame);

        fileStructure = v.findViewById(R.id.fileStructure);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = v.findViewById(R.id.progress_bar);

        mBreadcrumbsView = v.findViewById(R.id.breadcrumbs_view);
        mBreadcrumbsView.setItems(new ArrayList<>(Arrays.asList(
                BreadcrumbItem.createSimpleItem(getResources().getString(R.string.filesBreadcrumbRoot))
        )));

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName);

        return  v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private static BreadcrumbItem createItem(String title) {
        List<String> list = new ArrayList<>();
        list.add(title);
        return new BreadcrumbItem(list);
    }

    @Override
    public void onClickDir(String dirName) {

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        StringBuilder breadcrumbBuilder = new StringBuilder();

        breadcrumbBuilder.append(fileStructure.getText().toString()).append("/").append(dirName);

        fileStructure.setText(breadcrumbBuilder);

        mBreadcrumbsView.addItem(createItem(dirName));
        mBreadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {

                if(position == 0) {
                    fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName);
                    fileStructure.setText("");
                    return;
                }

                String filterDir = fileStructure.getText().toString();
                String result = filterDir.substring(0, filterDir.indexOf(item.getSelectedItem()));
                fileStructure.setText(result + item.getSelectedItem());
                fetchDataAsyncSub(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, fileStructure.getText().toString());

            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {

            }
        });

        fetchDataAsyncSub(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, fileStructure.getText().toString());

    }

    @Override
    public void onClickFile(String fileName) {

        Intent intent = new Intent(getContext(), FileViewActivity.class);

        if(!fileStructure.getText().toString().equals("Root")) {

            intent.putExtra("singleFileName", fileStructure.getText().toString()+"/"+fileName);
        }
        else {

            intent.putExtra("singleFileName", fileName);
        }

        Objects.requireNonNull(getContext()).startActivity(intent);
    }

    private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, String repo) {

        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        FilesViewModel filesModel = new ViewModelProvider(this).get(FilesViewModel.class);

        filesModel.getFilesList(instanceUrl, instanceToken, owner, repo).observe(this, new Observer<List<Files>>() {
            @Override
            public void onChanged(@Nullable List<Files> filesListMain) {
                adapter = new FilesAdapter(getContext(), filesListMain, FilesFragment.this);

                mBreadcrumbsView.removeItemAfter(1);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(adapter);
                    filesFrame.setVisibility(View.VISIBLE);
                    noDataFiles.setVisibility(View.GONE);
                }
                else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    filesFrame.setVisibility(View.VISIBLE);
                    noDataFiles.setVisibility(View.VISIBLE);
                }
                filesFrame.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    private void fetchDataAsyncSub(String instanceUrl, String instanceToken, String owner, String repo, String filesDir) {

        mRecyclerView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        FilesViewModel filesModel2 = new ViewModelProvider(this).get(FilesViewModel.class);

        filesModel2.getFilesList2(instanceUrl, instanceToken, owner, repo, filesDir).observe(this, new Observer<List<Files>>() {
            @Override
            public void onChanged(@Nullable List<Files> filesListMain2) {
                adapter = new FilesAdapter(getContext(), filesListMain2, FilesFragment.this);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(adapter);
                    filesFrame.setVisibility(View.VISIBLE);
                    noDataFiles.setVisibility(View.GONE);
                }
                else {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    filesFrame.setVisibility(View.VISIBLE);
                    noDataFiles.setVisibility(View.VISIBLE);
                }
                filesFrame.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setQueryHint(getContext().getString(R.string.strFilter));

        if(!connToInternet) {
            return;
        }

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(mRecyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

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
}
