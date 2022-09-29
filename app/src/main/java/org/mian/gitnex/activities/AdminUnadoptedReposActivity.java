package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminUnadoptedReposAdapter;
import org.mian.gitnex.databinding.ActivityAdminCronTasksBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.viewmodels.AdminUnadoptedReposViewModel;

/**
 * @author M M Arif
 * @author qwerty287
 */
public class AdminUnadoptedReposActivity extends BaseActivity {

	private AdminUnadoptedReposViewModel viewModel;
	private View.OnClickListener onClickListener;
	private AdminUnadoptedReposAdapter adapter;

	private ActivityAdminCronTasksBinding binding;

	private int PAGE = 1;
	private int resultLimit;
	private boolean reload = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityAdminCronTasksBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		viewModel = new ViewModelProvider(this).get(AdminUnadoptedReposViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(ctx);
		initCloseListener();
		binding.close.setOnClickListener(onClickListener);

		Toolbar toolbar = binding.toolbar;
		setSupportActionBar(toolbar);

		binding.toolbarTitle.setText(R.string.unadoptedRepos);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		binding.pullToRefresh.setOnRefreshListener(
				() ->
						new Handler(Looper.getMainLooper())
								.postDelayed(
										() -> {
											binding.pullToRefresh.setRefreshing(false);
											PAGE = 1;
											binding.progressBar.setVisibility(View.VISIBLE);
											reload = true;
											viewModel.loadRepos(ctx, PAGE, resultLimit, null);
										},
										500));

		adapter =
				new AdminUnadoptedReposAdapter(
						new ArrayList<>(),
						() -> {
							PAGE = 1;
							binding.progressBar.setVisibility(View.VISIBLE);
							reload = true;
							viewModel.loadRepos(ctx, PAGE, resultLimit, null);
						},
						() -> {
							PAGE += 1;
							binding.progressBar.setVisibility(View.VISIBLE);
							viewModel.loadRepos(ctx, PAGE, resultLimit, null);
						},
						binding);

		binding.recyclerView.setAdapter(adapter);

		fetchDataAsync(ctx);
	}

	private void fetchDataAsync(Context ctx) {

		AtomicInteger prevSize = new AtomicInteger();

		viewModel
				.getUnadoptedRepos(ctx, PAGE, resultLimit, null)
				.observe(
						this,
						list -> {
							binding.progressBar.setVisibility(View.GONE);

							boolean hasMore = reload || list.size() > prevSize.get();
							reload = false;

							prevSize.set(list.size());

							if (list.size() > 0) {
								adapter.updateList(list);
								adapter.setHasMore(hasMore);
								binding.noData.setVisibility(View.GONE);
							} else {
								binding.noData.setVisibility(View.VISIBLE);
							}
						});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
