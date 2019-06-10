package org.mian.gitnex.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.fragments.AdminUsersBottomSheetFragment;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.AdminGetUsersViewModel;
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class AdminGetUsersActivity extends AppCompatActivity implements AdminUsersBottomSheetFragment.BottomSheetListener {

    private View.OnClickListener onClickListener;
    final Context ctx = this;
    private AdminGetUsersAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataUsers;
    private Boolean searchFilter = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_get_users);

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        noDataUsers = findViewById(R.id.noDataUsers);
        mRecyclerView = findViewById(R.id.recyclerView);

        final SwipeRefreshLayout swipeRefresh = findViewById(R.id.pullToRefresh);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        AdminGetUsersViewModel.loadUsersList(getApplicationContext(), instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken));
                    }
                }, 500);
            }
        });

        fetchDataAsync(getApplicationContext(), instanceUrl, Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken));

    }

    private void fetchDataAsync(Context ctx, String instanceUrl, String instanceToken) {

        AdminGetUsersViewModel usersModel = ViewModelProviders.of(this).get(AdminGetUsersViewModel.class);

        usersModel.getUsersList(ctx, instanceUrl, instanceToken).observe(this, new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> usersListMain) {
                adapter = new AdminGetUsersAdapter(getApplicationContext(), usersListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAdapter(adapter);
                    noDataUsers.setVisibility(View.GONE);
                    searchFilter = true;
                }
                else {
                    //adapter.notifyDataSetChanged();
                    //mRecyclerView.setAdapter(adapter);
                    mRecyclerView.setVisibility(View.GONE);
                    noDataUsers.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.generic_nav_dotted_menu, menu);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(searchFilter) {

                    boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getApplicationContext()));

                    inflater.inflate(R.menu.search_menu, menu);

                    MenuItem searchItem = menu.findItem(R.id.action_search);
                    androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
                    searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

                    if(!connToInternet) {
                        return;
                    }

                    searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            return true;
                        }

                        @Override
                        public boolean onQueryTextChange(String newText) {
                            adapter.getFilter().filter(newText);
                            return false;
                        }
                    });
                }
            }
        }, 500);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.genericMenu:
                AdminUsersBottomSheetFragment bottomSheet = new AdminUsersBottomSheetFragment();
                bottomSheet.show(getSupportFragmentManager(), "usersBottomSheet");
                return true;
            default:
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
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

}
