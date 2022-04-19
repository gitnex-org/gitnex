package org.mian.gitnex.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminGetUsersAdapter;
import org.mian.gitnex.databinding.ActivityAdminGetUsersBinding;
import org.mian.gitnex.fragments.BottomSheetAdminUsersFragment;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.structs.BottomSheetListener;
import org.mian.gitnex.viewmodels.AdminGetUsersViewModel;

/**
 * @author M M Arif
 */

public class AdminGetUsersActivity extends BaseActivity implements BottomSheetListener {

	private AdminGetUsersViewModel adminGetUsersViewModel;
	private View.OnClickListener onClickListener;
	private ActivityAdminGetUsersBinding activityAdminGetUsersBinding;
	private AdminGetUsersAdapter adapter;
	private int page = 1;
	private int resultLimit;
	private Boolean searchFilter = false;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityAdminGetUsersBinding = ActivityAdminGetUsersBinding.inflate(getLayoutInflater());
		setContentView(activityAdminGetUsersBinding.getRoot());
		adminGetUsersViewModel = new ViewModelProvider(this).get(AdminGetUsersViewModel.class);

		Toolbar toolbar = activityAdminGetUsersBinding.toolbar;
		setSupportActionBar(toolbar);

		initCloseListener();
		activityAdminGetUsersBinding.close.setOnClickListener(onClickListener);

		resultLimit = Constants.getCurrentResultLimit(ctx);

		activityAdminGetUsersBinding.recyclerView.setHasFixedSize(true);
		activityAdminGetUsersBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));
		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activityAdminGetUsersBinding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		activityAdminGetUsersBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		activityAdminGetUsersBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			activityAdminGetUsersBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync();
			activityAdminGetUsersBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync();
	};

	private void fetchDataAsync() {

		AdminGetUsersViewModel adminUsersModel = new ViewModelProvider(this).get(AdminGetUsersViewModel.class);

		adminUsersModel.getUsersList(page, resultLimit, ctx).observe(this, adminUsersListMain -> {

			adapter = new AdminGetUsersAdapter(adminUsersListMain, ctx);
			adapter.setLoadMoreListener(new AdminGetUsersAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					adminGetUsersViewModel.loadMoreUsersList(page, resultLimit, ctx, adapter);
					activityAdminGetUsersBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					activityAdminGetUsersBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				activityAdminGetUsersBinding.recyclerView.setAdapter(adapter);
				activityAdminGetUsersBinding.noDataUsers.setVisibility(View.GONE);
				searchFilter = true;
			}
			else {
				adapter.notifyDataChanged();
				activityAdminGetUsersBinding.recyclerView.setAdapter(adapter);
				activityAdminGetUsersBinding.noDataUsers.setVisibility(View.VISIBLE);
			}

			activityAdminGetUsersBinding.progressBar.setVisibility(View.GONE);
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

	    if("newUser".equals(text)) {
		    startActivity(new Intent(AdminGetUsersActivity.this, CreateNewUserActivity.class));
	    }
    }

    private void initCloseListener() {
        onClickListener = view -> finish();
    }
}
