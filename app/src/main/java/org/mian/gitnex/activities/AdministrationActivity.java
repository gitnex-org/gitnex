package org.mian.gitnex.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.Cron;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminCronTasksAdapter;
import org.mian.gitnex.adapters.AdminUnadoptedReposAdapter;
import org.mian.gitnex.api.models.settings.RepositoryGlobal;
import org.mian.gitnex.databinding.ActivityAdministrationBinding;
import org.mian.gitnex.databinding.BottomsheetAdminCronTasksBinding;
import org.mian.gitnex.databinding.BottomsheetAdministrationRepositorySettingsBinding;
import org.mian.gitnex.databinding.ItemAdministrationRepoSettingRowBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.viewmodels.AdministrationViewModel;

/**
 * @author mmarif
 */
public class AdministrationActivity extends BaseActivity {

	private ActivityAdministrationBinding binding;
	private AdministrationViewModel viewModel;
	private int resultLimit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAdministrationBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, binding.mainFrame);

		resultLimit = Constants.getCurrentResultLimit(this);

		viewModel = new ViewModelProvider(this).get(AdministrationViewModel.class);

		initCards();
		setupListeners();
		observeViewModel();
	}

	private void observeViewModel() {
		viewModel
				.getErrorMessage()
				.observe(
						this,
						error -> {
							if (error != null) Toasty.show(this, error);
						});

		viewModel
				.getTaskSuccessMessage()
				.observe(
						this,
						taskName -> {
							if (taskName != null) {
								Toasty.show(
										this,
										getString(R.string.adminCronTaskSuccessMsg, taskName));
							}
						});
	}

	private void setupListeners() {
		binding.btnBack.setOnClickListener(v -> finish());

		binding.cardUsers
				.getRoot()
				.setOnClickListener(
						v -> startActivity(new Intent(this, AdminGetUsersActivity.class)));

		binding.cardCron.getRoot().setOnClickListener(v -> showCronTasksSheet());

		binding.cardUnadopted.getRoot().setOnClickListener(v -> showUnadoptedReposSheet());

		binding.cardRepoSettings.getRoot().setOnClickListener(v -> showRepositorySettings());
	}

	private void showCronTasksSheet() {

		BottomsheetAdminCronTasksBinding sheetBinding =
				BottomsheetAdminCronTasksBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		dialog.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(dialog, true);

		viewModel.resetCronPagination();

		AdminCronTasksAdapter adapter =
				new AdminCronTasksAdapter(
						new ArrayList<>(),
						new AdminCronTasksAdapter.OnCronTaskListener() {
							@Override
							public void onRunTask(String taskName) {
								viewModel.runCronTask(AdministrationActivity.this, taskName);
							}

							@Override
							public void onShowDetails(Cron task) {
								showCronDetailDialog(task);
							}
						});

		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		sheetBinding.recyclerView.setLayoutManager(layoutManager);
		sheetBinding.recyclerView.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchCronTasks(
								AdministrationActivity.this, page, resultLimit, false);
					}
				};
		sheetBinding.recyclerView.addOnScrollListener(scrollListener);

		viewModel
				.getCronTasks()
				.observe(
						this,
						list -> {
							if (list != null) {
								adapter.updateList(list);
							}
						});

		viewModel
				.getIsCronLoading()
				.observe(
						this,
						loading ->
								sheetBinding.expressiveLoader.setVisibility(
										loading ? View.VISIBLE : View.GONE));

		viewModel.fetchCronTasks(this, 1, resultLimit, true);
		dialog.show();
	}

	private void showCronDetailDialog(Cron task) {

		View view = LayoutInflater.from(this).inflate(R.layout.layout_cron_task_info, null);

		TextView taskScheduleContent = view.findViewById(R.id.taskScheduleContent);
		TextView nextRunContent = view.findViewById(R.id.nextRunContent);
		TextView lastRunContent = view.findViewById(R.id.lastRunContent);
		TextView execTimeContent = view.findViewById(R.id.execTimeContent);

		Locale locale = Locale.getDefault();
		String nextRun =
				(task.getNext() != null) ? TimeHelper.getFullDateTime(task.getNext(), locale) : "";
		String lastRun =
				(task.getPrev() != null) ? TimeHelper.getFullDateTime(task.getPrev(), locale) : "";

		taskScheduleContent.setText(task.getSchedule());
		nextRunContent.setText(nextRun);
		lastRunContent.setText(lastRun);
		execTimeContent.setText(String.valueOf(task.getExecTimes()));

		String name = task.getName().replace("_", " ");
		new MaterialAlertDialogBuilder(this)
				.setTitle(name.substring(0, 1).toUpperCase() + name.substring(1))
				.setView(view)
				.setPositiveButton(R.string.close, null)
				.show();
	}

	private void showRepositorySettings() {

		BottomsheetAdministrationRepositorySettingsBinding sheetBinding =
				BottomsheetAdministrationRepositorySettingsBinding.inflate(getLayoutInflater());

		BottomSheetDialog dialog = new BottomSheetDialog(this);
		dialog.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(dialog, true);

		sheetBinding.itemForks.settingLabel.setText(R.string.forks);
		sheetBinding.itemMigrations.settingLabel.setText(R.string.migrations);
		sheetBinding.itemHttpGit.settingLabel.setText(R.string.http_git);
		sheetBinding.itemLfs.settingLabel.setText(R.string.lfs);
		sheetBinding.itemMirrors.settingLabel.setText(R.string.mirrors);
		sheetBinding.itemTime.settingLabel.setText(R.string.time_tracking);
		sheetBinding.itemStars.settingLabel.setText(R.string.stars);

		viewModel
				.getRepositorySettings()
				.observe(
						this,
						settings -> {
							if (settings != null) {
								updateSettingsUI(sheetBinding, settings);
								sheetBinding.settingsContainer.setVisibility(View.VISIBLE);
							}
						});

		viewModel
				.getIsSettingsLoading()
				.observe(
						this,
						loading -> {
							sheetBinding.loadingIndicator.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							if (loading) sheetBinding.settingsContainer.setVisibility(View.GONE);
						});

		viewModel.fetchRepositoryGlobalSettings(this);
		dialog.show();
	}

	private void updateSettingsUI(
			BottomsheetAdministrationRepositorySettingsBinding binding, RepositoryGlobal settings) {
		applyStatusStyle(binding.itemForks, !settings.isForksDisabled());
		applyStatusStyle(binding.itemMigrations, !settings.isMigrationsDisabled());
		applyStatusStyle(binding.itemHttpGit, !settings.isHttpGitDisabled());
		applyStatusStyle(binding.itemLfs, !settings.isLfsDisabled());
		applyStatusStyle(binding.itemMirrors, !settings.isMirrorsDisabled());
		applyStatusStyle(binding.itemTime, !settings.isTimeTrackingDisabled());
		applyStatusStyle(binding.itemStars, !settings.isStarsDisabled());
	}

	private void applyStatusStyle(
			ItemAdministrationRepoSettingRowBinding itemBinding, boolean isEnabled) {
		itemBinding.settingStatus.setText(isEnabled ? R.string.enabled : R.string.disabled);

		if (isEnabled) {
			itemBinding.statusContainer.setCardBackgroundColor(Color.parseColor("#1A2E7D32"));
			itemBinding.settingStatus.setTextColor(ContextCompat.getColor(this, R.color.darkGreen));
		} else {
			itemBinding.statusContainer.setCardBackgroundColor(Color.parseColor("#1AC62828"));
			itemBinding.settingStatus.setTextColor(ContextCompat.getColor(this, R.color.darkRed));
		}
	}

	private void showUnadoptedReposSheet() {

		BottomsheetAdminCronTasksBinding sheetBinding =
				BottomsheetAdminCronTasksBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(this);
		dialog.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(dialog, true);

		sheetBinding.sheetTitle.setText(R.string.unadoptedRepos);
		viewModel.resetUnadoptedPagination();

		AdminUnadoptedReposAdapter adapter =
				new AdminUnadoptedReposAdapter(new ArrayList<>(), this::showUnadoptedActionDialog);
		LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		sheetBinding.recyclerView.setLayoutManager(layoutManager);
		sheetBinding.recyclerView.setAdapter(adapter);

		EndlessRecyclerViewScrollListener scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						viewModel.fetchUnadoptedRepos(
								AdministrationActivity.this, page, resultLimit, false);
					}
				};
		sheetBinding.recyclerView.addOnScrollListener(scrollListener);

		viewModel
				.getUnadoptedRepos()
				.observe(
						this,
						list -> {
							adapter.updateList(list);
							sheetBinding
									.layoutEmpty
									.getRoot()
									.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
						});

		viewModel
				.getIsUnadoptedLoading()
				.observe(
						this,
						loading ->
								sheetBinding.expressiveLoader.setVisibility(
										loading ? View.VISIBLE : View.GONE));

		viewModel
				.getRepoActionSuccess()
				.observe(
						this,
						result -> {
							if (result != null) {
								String message;
								if (result.isDelete()) {
									message = getString(R.string.repoDeletionSuccess);
								} else {
									message = getString(R.string.repoAdopted, result.repoName());
								}
								Toasty.show(this, message);
							}
						});

		viewModel.fetchUnadoptedRepos(this, 1, resultLimit, true);
		dialog.show();
	}

	private void showUnadoptedActionDialog(String repoName) {
		String[] parts = repoName.split("/");
		new MaterialAlertDialogBuilder(this)
				.setTitle(repoName)
				.setMessage(getString(R.string.unadoptedReposMessage, parts[1], parts[0]))
				.setNeutralButton(R.string.close, null)
				.setPositiveButton(
						R.string.menuDeleteText,
						(d, w) -> viewModel.performRepoAction(this, repoName, true))
				.setNegativeButton(
						R.string.adoptRepo,
						(d, w) -> viewModel.performRepoAction(this, repoName, false))
				.show();
	}

	private void initCards() {
		binding.cardUsers.cardIcon.setImageResource(R.drawable.ic_people);
		binding.cardUsers.cardTitle.setText(R.string.adminUsers);
		binding.cardUsers.cardSubtext.setText(R.string.adminUsersSubtext);

		binding.cardCron.cardIcon.setImageResource(R.drawable.ic_tasks);
		binding.cardCron.cardTitle.setText(R.string.adminCron);
		binding.cardCron.cardSubtext.setText(R.string.adminCronSubtext);

		binding.cardUnadopted.cardIcon.setImageResource(R.drawable.ic_directory_2);
		binding.cardUnadopted.cardTitle.setText(R.string.unadoptedRepos);
		binding.cardUnadopted.cardSubtext.setText(R.string.unadoptedReposSubtext);

		binding.cardRepoSettings.cardIcon.setImageResource(R.drawable.ic_repo);
		binding.cardRepoSettings.cardTitle.setText(R.string.repoSettingsTitle);
		binding.cardRepoSettings.cardSubtext.setText(R.string.repoSettingsSubtext);
	}
}
