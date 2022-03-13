package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.databinding.ActivityAdminGetUsersBinding;
import org.mian.gitnex.fragments.BottomSheetAdminUsersFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.viewmodels.AdminGetUsersViewModel;

/**
 * Author M M Arif
 */

public class AdminGetUsersActivity extends BaseActivity implements BottomSheetListener {

    private View.OnClickListener onClickListener;
    private AdminGetUsersAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataUsers;
    private Boolean searchFilter = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityAdminGetUsersBinding activityAdminGetUsersBinding = ActivityAdminGetUsersBinding.inflate(getLayoutInflater());
	    setContentView(activityAdminGetUsersBinding.getRoot());

        ImageView closeActivity = activityAdminGetUsersBinding.close;
        noDataUsers = activityAdminGetUsersBinding.noDataUsers;
        mRecyclerView = activityAdminGetUsersBinding.recyclerView;

        final SwipeRefreshLayout swipeRefresh = activityAdminGetUsersBinding.pullToRefresh;

        Toolbar toolbar = activityAdminGetUsersBinding.toolbar;
        setSupportActionBar(toolbar);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            AdminGetUsersViewModel.loadUsersList(ctx, getAccount().getAuthorization());

        }, 500));

        fetchDataAsync(ctx, getAccount().getAuthorization());

    }

    private void fetchDataAsync(Context ctx, String instanceToken) {

        AdminGetUsersViewModel usersModel = new ViewModelProvider(this).get(AdminGetUsersViewModel.class);

        usersModel.getUsersList(ctx, instanceToken).observe(this, usersListMain -> {

            adapter = new AdminGetUsersAdapter(ctx, usersListMain);
            if(adapter.getItemCount() > 0) {

                mRecyclerView.setVisibility(View.VISIBLE);
                mRecyclerView.setAdapter(adapter);
                noDataUsers.setVisibility(View.GONE);
                searchFilter = true;
            }
            else {

                mRecyclerView.setVisibility(View.GONE);
                noDataUsers.setVisibility(View.VISIBLE);
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.generic_nav_dotted_menu, menu);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {

            if(searchFilter) {

                boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

                inflater.inflate(R.menu.search_menu, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();
                searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

                if(!connToInternet) {
                    return;
                }

                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) { return true; }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        adapter.getFilter().filter(newText);
                        return false;
                    }

                });
            }

        }, 500);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if(id == android.R.id.home) {

	        finish();
	        return true;
        }
        else if(id == R.id.genericMenu) {

	        BottomSheetAdminUsersFragment bottomSheet = new BottomSheetAdminUsersFragment();
	        bottomSheet.show(getSupportFragmentManager(), "usersBottomSheet");
	        return true;
        }
        else {

	        return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onButtonClicked(String text) {

        switch (text) {
            case "newUser":
                startActivity(new Intent(AdminGetUsersActivity.this, CreateNewUserActivity.class));
                break;
        }

    }

    private void initCloseListener() {
        onClickListener = view -> finish();
    }

}
