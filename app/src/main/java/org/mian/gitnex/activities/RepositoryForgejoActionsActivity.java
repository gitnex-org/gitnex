package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.MaterialButton;
import org.mian.gitnex.R;
import org.mian.gitnex.bottomsheets.CreateActionVariableBottomSheet;
import org.mian.gitnex.databinding.ActivityRepositoryActionsBinding;
import org.mian.gitnex.fragments.RepositoryForgejoActionsRunnersFragment;
import org.mian.gitnex.fragments.RepositoryForgejoActionsTasksFragment;
import org.mian.gitnex.fragments.RepositoryForgejoActionsVariablesFragment;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;

/**
 * @author mmarif
 */
public class RepositoryForgejoActionsActivity extends BaseActivity {

	private static final String STATE_ACTIVE_TAB = "active_tab";
	private static final String TAB_RUNNERS = "runners";
	private static final String TAB_TASKS = "tasks";
	private static final String TAB_VARIABLES = "variables";

	private ActivityRepositoryActionsBinding binding;
	private final FragmentManager fm = getSupportFragmentManager();
	private Fragment runnersFrag, tasksFrag, variablesFrag;
	private Fragment activeFragment;
	private RepositoryContext repositoryContext;

	private View detachedDivider;
	private MaterialButton detachedAddBtn;
	private String currentActiveTab = TAB_RUNNERS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityRepositoryActionsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		repositoryContext = RepositoryContext.fromIntent(getIntent());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		binding.btnNavWorkflows.setContentDescription(getString(R.string.tasks));
		binding.btnNavWorkflows.setTooltipText(getString(R.string.tasks));

		detachedDivider = binding.dockDivider;
		detachedAddBtn = binding.btnNavAdd;

		if (savedInstanceState != null) {
			currentActiveTab = savedInstanceState.getString(STATE_ACTIVE_TAB, TAB_RUNNERS);
		}

		findOrCreateFragments();
		setupFragments();
		setupDockListeners();
		restoreActiveTab();
	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_ACTIVE_TAB, currentActiveTab);
	}

	@Override
	protected void onGlobalRefresh() {
		RepositoryForgejoActionsVariablesFragment fragmentVariables =
				(RepositoryForgejoActionsVariablesFragment)
						getSupportFragmentManager().findFragmentByTag(TAB_VARIABLES);
		if (fragmentVariables != null) fragmentVariables.refreshFromGlobal();
	}

	private void findOrCreateFragments() {
		runnersFrag = fm.findFragmentByTag(TAB_RUNNERS);
		tasksFrag = fm.findFragmentByTag(TAB_TASKS);
		variablesFrag = fm.findFragmentByTag(TAB_VARIABLES);

		if (runnersFrag == null) runnersFrag = new RepositoryForgejoActionsRunnersFragment();
		if (tasksFrag == null) tasksFrag = new RepositoryForgejoActionsTasksFragment();
		if (variablesFrag == null) variablesFrag = new RepositoryForgejoActionsVariablesFragment();

		activeFragment = runnersFrag;
	}

	private void setupFragments() {
		runnersFrag.setArguments(repositoryContext.getBundle());
		tasksFrag.setArguments(repositoryContext.getBundle());
		variablesFrag.setArguments(repositoryContext.getBundle());

		if (runnersFrag.isAdded() && tasksFrag.isAdded() && variablesFrag.isAdded()) return;

		fm.beginTransaction()
				.add(R.id.repo_actions_container, variablesFrag, TAB_VARIABLES)
				.hide(variablesFrag)
				.add(R.id.repo_actions_container, tasksFrag, TAB_TASKS)
				.hide(tasksFrag)
				.add(R.id.repo_actions_container, runnersFrag, TAB_RUNNERS)
				.commitNow();
	}

	private void restoreActiveTab() {
		Fragment targetFragment;
		int activeBtnId =
				switch (currentActiveTab) {
					case TAB_TASKS -> {
						targetFragment = tasksFrag;
						yield R.id.btn_nav_workflows;
					}
					case TAB_VARIABLES -> {
						targetFragment = variablesFrag;
						yield R.id.btn_nav_variables;
					}
					default -> {
						targetFragment = runnersFrag;
						yield R.id.btn_nav_runners;
					}
				};

		if (runnersFrag != targetFragment) fm.beginTransaction().hide(runnersFrag).commitNow();
		if (tasksFrag != targetFragment) fm.beginTransaction().hide(tasksFrag).commitNow();
		if (variablesFrag != targetFragment) fm.beginTransaction().hide(variablesFrag).commitNow();

		fm.beginTransaction().show(targetFragment).commitNow();
		activeFragment = targetFragment;
		updateDockUI(activeBtnId);
	}

	private void setupDockListeners() {
		prepareNavButton(binding.btnNavRunners);
		prepareNavButton(binding.btnNavWorkflows);
		prepareNavButton(binding.btnNavVariables);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnNavRunners.setOnClickListener(
				v -> switchTab(runnersFrag, R.id.btn_nav_runners, TAB_RUNNERS));
		binding.btnNavWorkflows.setOnClickListener(
				v -> switchTab(tasksFrag, R.id.btn_nav_workflows, TAB_TASKS));
		binding.btnNavVariables.setOnClickListener(
				v -> switchTab(variablesFrag, R.id.btn_nav_variables, TAB_VARIABLES));

		detachedAddBtn.setOnClickListener(
				v -> {
					if (activeFragment instanceof RepositoryForgejoActionsVariablesFragment) {
						showCreateVariableBottomSheet();
					}
				});
	}

	private void switchTab(Fragment target, int btnId, String tabId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
		currentActiveTab = tabId;
		updateDockUI(btnId);
	}

	private void updateDockUI(int activeBtnId) {
		resetPill(binding.btnNavRunners);
		resetPill(binding.btnNavWorkflows);
		resetPill(binding.btnNavVariables);

		if (activeBtnId == R.id.btn_nav_runners) activatePill(binding.btnNavRunners);
		else if (activeBtnId == R.id.btn_nav_workflows) activatePill(binding.btnNavWorkflows);
		else if (activeBtnId == R.id.btn_nav_variables) activatePill(binding.btnNavVariables);

		updateContextualDockActions();
	}

	private void updateContextualDockActions() {
		ViewGroup parent = binding.dockedToolbarChild;
		parent.removeView(detachedDivider);
		parent.removeView(detachedAddBtn);

		boolean showAdd = activeFragment instanceof RepositoryForgejoActionsVariablesFragment;
		if (showAdd) {
			parent.addView(detachedDivider);
			detachedDivider.setVisibility(View.VISIBLE);
			parent.addView(detachedAddBtn);
			detachedAddBtn.setVisibility(View.VISIBLE);
		}

		binding.dockedToolbar.requestLayout();

		LinearLayout.LayoutParams params =
				(LinearLayout.LayoutParams) binding.btnNavVariables.getLayoutParams();
		params.setMarginEnd((int) getResources().getDimension(R.dimen.dimen12dp));
		binding.btnNavVariables.setLayoutParams(params);
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn) {
		btn.setSelected(true);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(255);
	}

	private void resetPill(MaterialButton btn) {
		btn.setSelected(false);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void showCreateVariableBottomSheet() {
		CreateActionVariableBottomSheet.newInstance(
						repositoryContext.getOwner(), repositoryContext.getName(), true)
				.show(getSupportFragmentManager(), "CREATE_VARIABLE");
	}
}
