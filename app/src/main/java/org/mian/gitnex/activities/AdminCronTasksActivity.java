package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.adapters.AdminCronTasksAdapter;
import org.mian.gitnex.databinding.ActivityAdminCronTasksBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.AdminCronTasksViewModel;

/**
 * @author M M Arif
 */

public class AdminCronTasksActivity extends BaseActivity {

	private AdminCronTasksViewModel adminCronTasksViewModel;
	private View.OnClickListener onClickListener;
	private AdminCronTasksAdapter adapter;

	private ActivityAdminCronTasksBinding activityAdminCronTasksBinding;

	private final int PAGE = 1;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityAdminCronTasksBinding = ActivityAdminCronTasksBinding.inflate(getLayoutInflater());
		setContentView(activityAdminCronTasksBinding.getRoot());
		adminCronTasksViewModel = new ViewModelProvider(this).get(AdminCronTasksViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(ctx);
		initCloseListener();
		activityAdminCronTasksBinding.close.setOnClickListener(onClickListener);

		Toolbar toolbar = activityAdminCronTasksBinding.toolbar;
		setSupportActionBar(toolbar);

		activityAdminCronTasksBinding.recyclerView.setHasFixedSize(true);
		activityAdminCronTasksBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		activityAdminCronTasksBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			activityAdminCronTasksBinding.progressBar.setVisibility(View.VISIBLE);
			activityAdminCronTasksBinding.pullToRefresh.setRefreshing(false);
			adminCronTasksViewModel.loadCronTasksList(ctx, PAGE, resultLimit);

		}, 500));

		fetchDataAsync(ctx);
	}

	private void fetchDataAsync(Context ctx) {

		adminCronTasksViewModel.getCronTasksList(ctx, PAGE, resultLimit).observe(this, cronTasksListMain -> {

			adapter = new AdminCronTasksAdapter(cronTasksListMain);

			if(adapter.getItemCount() > 0) {
				activityAdminCronTasksBinding.recyclerView.setAdapter(adapter);
				activityAdminCronTasksBinding.noData.setVisibility(View.GONE);
				activityAdminCronTasksBinding.progressBar.setVisibility(View.GONE);
			}
			else {
				activityAdminCronTasksBinding.noData.setVisibility(View.VISIBLE);
			}
		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
