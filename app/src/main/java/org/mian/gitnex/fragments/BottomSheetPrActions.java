package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.gitnex.tea4j.v2.models.PullRequest;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.databinding.BottomsheetPrActionsBinding;
import org.mian.gitnex.helpers.AppUIStateManager;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.TokenAuthorizationDialog;
import org.mian.gitnex.viewmodels.IssueActionsViewModel;
import org.mian.gitnex.viewmodels.PullRequestDetailsViewModel;

/**
 * @author mmarif
 */
public class BottomSheetPrActions extends BottomSheetDialogFragment {

	private BottomsheetPrActionsBinding binding;
	private PullRequestDetailsViewModel prViewModel;
	private IssueActionsViewModel issueActionsViewModel;
	private PullRequest pullRequest;
	private String owner;
	private String repo;
	private long prNumber;
	private boolean isPinned;
	private String selectedDoStrategy;

	public static BottomSheetPrActions newInstance(
			String owner, String repo, long prNumber, PullRequest pullRequest, boolean isPinned) {
		BottomSheetPrActions sheet = new BottomSheetPrActions();
		Bundle args = new Bundle();
		args.putString("owner", owner);
		args.putString("repo", repo);
		args.putLong("pr_number", prNumber);
		args.putSerializable("pull_request", pullRequest);
		args.putBoolean("is_pinned", isPinned);
		sheet.setArguments(args);
		return sheet;
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = BottomsheetPrActionsBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			owner = args.getString("owner");
			repo = args.getString("repo");
			prNumber = args.getLong("pr_number");
			pullRequest = (PullRequest) args.getSerializable("pull_request");
			isPinned = args.getBoolean("is_pinned");
		}

		prViewModel =
				new ViewModelProvider(requireActivity()).get(PullRequestDetailsViewModel.class);
		issueActionsViewModel =
				new ViewModelProvider(requireActivity()).get(IssueActionsViewModel.class);

		binding.btnClose.setOnClickListener(v -> dismiss());

		setupMergeSection();
		setupUpdateSection();
		setupToggleCards();
		observeViewModels();
	}

	private void setupMergeSection() {
		Repository repository =
				pullRequest.getBase() != null ? pullRequest.getBase().getRepo() : null;
		boolean isOpen = "open".equalsIgnoreCase(pullRequest.getState());

		if (repository == null || !isOpen) {
			hideMergeSection();
			return;
		}

		if (!pullRequest.isMergeable()) {
			hideMergeSection();
			binding.mergeNotAvailableMessage.setVisibility(View.VISIBLE);
			return;
		}

		List<MergeStrategy> strategies = new ArrayList<>();
		if (repository.isAllowMergeCommits()) {
			strategies.add(
					new MergeStrategy("merge", getString(R.string.mergePullRequestButtonText)));
		}
		if (repository.isAllowRebase()) {
			strategies.add(new MergeStrategy("rebase", getString(R.string.updateStrategyRebase)));
		}
		if (repository.isAllowRebaseExplicit()) {
			strategies.add(
					new MergeStrategy("rebase-merge", getString(R.string.mergeOptionRebase)));
		}
		if (repository.isAllowSquashMerge()) {
			strategies.add(new MergeStrategy("squash", getString(R.string.mergeOptionSquash)));
		}

		if (strategies.isEmpty()) {
			hideMergeSection();
			binding.mergeNotAvailableMessage.setVisibility(View.VISIBLE);
			return;
		}

		ArrayAdapter<MergeStrategy> adapter =
				new ArrayAdapter<>(
						requireContext(),
						R.layout.support_simple_spinner_dropdown_item,
						strategies);
		binding.mergeStrategy.setAdapter(adapter);

		String defaultStyle = repository.getDefaultMergeStyle();
		for (int i = 0; i < strategies.size(); i++) {
			if (strategies.get(i).value().equals(defaultStyle)) {
				binding.mergeStrategy.setText(strategies.get(i).toString(), false);
				selectedDoStrategy = defaultStyle;
				break;
			}
		}
		if (selectedDoStrategy == null && !strategies.isEmpty()) {
			binding.mergeStrategy.setText(strategies.get(0).toString(), false);
			selectedDoStrategy = strategies.get(0).value();
		}

		binding.mergeStrategy.setOnItemClickListener(
				(parent, v, position, id) -> {
					selectedDoStrategy = strategies.get(position).value();
				});

		String defaultTitle = pullRequest.getTitle() + " (#" + prNumber + ")";
		binding.mergeTitle.setText(defaultTitle);

		boolean isFork = repository.isFork() != null && repository.isFork();
		boolean canPushToHead =
				pullRequest.getHead() != null
						&& pullRequest.getHead().getRepo() != null
						&& Boolean.TRUE.equals(
								pullRequest.getHead().getRepo().getPermissions().isPush());

		if (canPushToHead) {
			binding.deleteBranchAfterMerge.setVisibility(View.VISIBLE);
			binding.deleteBranchForkInfo.setVisibility(isFork ? View.VISIBLE : View.GONE);
		} else {
			binding.deleteBranchAfterMerge.setVisibility(View.GONE);
			binding.deleteBranchForkInfo.setVisibility(View.GONE);
		}

		binding.btnMerge.setOnClickListener(
				v -> {
					String title =
							Objects.requireNonNull(binding.mergeTitle.getText()).toString().trim();
					String message =
							Objects.requireNonNull(binding.mergeDescription.getText())
									.toString()
									.trim();
					boolean deleteBranch = binding.deleteBranchAfterMerge.isChecked();
					boolean mergeWhenChecks = binding.mergeWhenChecksSucceed.isChecked();

					if (selectedDoStrategy == null) {
						Toasty.show(requireContext(), getString(R.string.selectMergeStrategy));
						return;
					}

					binding.btnMerge.setText(null);
					binding.btnMerge.setEnabled(false);
					prViewModel.mergePr(
							requireContext(),
							owner,
							repo,
							prNumber,
							selectedDoStrategy,
							title,
							message,
							deleteBranch,
							mergeWhenChecks);
				});
	}

	private void hideMergeSection() {
		binding.mergeStrategyLayout.setVisibility(View.GONE);
		binding.mergeTitleLayout.setVisibility(View.GONE);
		binding.mergeDescriptionLayout.setVisibility(View.GONE);
		binding.deleteBranchAfterMerge.setVisibility(View.GONE);
		binding.deleteBranchForkInfo.setVisibility(View.GONE);
		binding.mergeWhenChecksSucceed.setVisibility(View.GONE);
		binding.mergeDivider.setVisibility(View.GONE);
		binding.mergeHeader.setVisibility(View.GONE);
		binding.btnMerge.setVisibility(View.GONE);
	}

	private void setupUpdateSection() {
		Repository repository =
				pullRequest.getBase() != null ? pullRequest.getBase().getRepo() : null;
		boolean isOpen = "open".equalsIgnoreCase(pullRequest.getState());
		boolean canUpdate =
				repository != null
						&& isOpen
						&& !Boolean.TRUE.equals(pullRequest.isMerged())
						&& repository.isAllowRebaseUpdate();

		binding.btnUpdatePr.setEnabled(canUpdate);
		binding.btnUpdatePr.setAlpha(canUpdate ? 1.0f : 0.4f);

		binding.btnUpdatePr.setOnClickListener(
				v -> {
					if (!canUpdate) return;

					Boolean rebase = repository.isAllowRebaseUpdate() ? true : null;

					binding.btnUpdatePr.setText(null);
					binding.btnUpdatePr.setEnabled(false);
					prViewModel.updatePr(requireContext(), owner, repo, prNumber, rebase);
				});
	}

	private void setupToggleCards() {
		String state = pullRequest.getState();
		boolean isMerged = Boolean.TRUE.equals(pullRequest.isMerged());
		boolean isClosed = "closed".equalsIgnoreCase(state);
		boolean canPushToHead =
				pullRequest.getHead() != null
						&& pullRequest.getHead().getRepo() != null
						&& Boolean.TRUE.equals(
								pullRequest.getHead().getRepo().getPermissions().isPush());

		if (isMerged) {
			disableCard(
					binding.closeReopenCard,
					binding.closeReopenIcon,
					binding.closeReopenText,
					R.drawable.ic_issue_closed,
					R.string.merged);
		} else if (isClosed) {
			binding.closeReopenIcon.setImageResource(R.drawable.ic_refresh);
			binding.closeReopenText.setText(R.string.reopen);
			binding.closeReopen.setEnabled(true);
			binding.closeReopenCard.setAlpha(1.0f);
		} else {
			binding.closeReopenIcon.setImageResource(R.drawable.ic_issue_closed);
			binding.closeReopenText.setText(R.string.close);
			binding.closeReopen.setEnabled(true);
			binding.closeReopenCard.setAlpha(1.0f);
		}

		binding.closeReopen.setOnClickListener(
				v -> {
					if (isMerged) return;
					issueActionsViewModel.toggleState(
							requireContext(), owner, repo, prNumber, state, true);
				});

		if ((isClosed || isMerged) && canPushToHead) {
			String branchName = pullRequest.getHead().getRef();
			prViewModel.checkBranchExists(requireContext(), owner, repo, branchName);

			prViewModel
					.getBranchExists()
					.observe(
							getViewLifecycleOwner(),
							exists -> {
								if (exists != null && exists) {
									binding.deleteBranch.setEnabled(true);
									binding.deleteBranchCard.setAlpha(1.0f);
									binding.deleteBranchIcon.setImageResource(R.drawable.ic_branch);
									binding.deleteBranchText.setText(R.string.deleteBranch);
									binding.deleteBranch.setOnClickListener(
											v -> {
												prViewModel.deleteHeadBranch(
														requireContext(),
														owner,
														repo,
														branchName,
														prNumber);
											});
								} else {
									disableCard(
											binding.deleteBranchCard,
											binding.deleteBranchIcon,
											binding.deleteBranchText,
											R.drawable.ic_branch,
											R.string.branchAlreadyDeleted);
								}
							});
		} else {
			String reason =
					!canPushToHead
							? getString(R.string.noPermission)
							: getString(R.string.prMustBeClosed);
			disableCard(
					binding.deleteBranchCard,
					binding.deleteBranchIcon,
					binding.deleteBranchText,
					R.drawable.ic_branch,
					reason);
		}

		if (isPinned) {
			binding.pinIcon.setImageResource(R.drawable.ic_unpin);
			binding.pinText.setText(R.string.unpin);
		} else {
			binding.pinIcon.setImageResource(R.drawable.ic_pin);
			binding.pinText.setText(R.string.pin);
		}
		binding.pin.setEnabled(true);
		binding.pinCard.setAlpha(1.0f);

		binding.pin.setOnClickListener(
				v -> {
					issueActionsViewModel.togglePin(
							requireContext(), owner, repo, prNumber, isPinned);
				});

		binding.subscribe.setOnClickListener(
				v -> {
					String currentUser =
							((BaseActivity) requireActivity())
									.getAccount()
									.getAccount()
									.getUserName();
					Boolean subscribed = issueActionsViewModel.getIsSubscribed().getValue();
					issueActionsViewModel.toggleSubscribe(
							requireContext(),
							owner,
							repo,
							prNumber,
							currentUser,
							subscribed != null && subscribed);
				});
	}

	private void disableCard(
			MaterialCardView card, ImageView icon, TextView text, int iconRes, int textRes) {
		card.setClickable(false);
		card.setFocusable(false);
		card.setAlpha(0.4f);
		icon.setImageResource(iconRes);
		text.setText(textRes);
	}

	private void disableCard(
			MaterialCardView card, ImageView icon, TextView text, int iconRes, String textStr) {
		card.setClickable(false);
		card.setFocusable(false);
		card.setAlpha(0.4f);
		icon.setImageResource(iconRes);
		text.setText(textStr);
	}

	private void observeViewModels() {
		prViewModel
				.getIsMerging()
				.observe(
						getViewLifecycleOwner(),
						merging -> {
							if (merging) {
								binding.mergeLoader.setVisibility(View.VISIBLE);
								binding.btnMerge.setText(null);
								binding.btnMerge.setEnabled(false);
							}
						});

		prViewModel
				.getIsUpdating()
				.observe(
						getViewLifecycleOwner(),
						updating -> {
							if (updating) {
								binding.updateLoader.setVisibility(View.VISIBLE);
								binding.btnUpdatePr.setText(null);
								binding.btnUpdatePr.setEnabled(false);
							} else {
								binding.updateLoader.setVisibility(View.GONE);
								binding.btnUpdatePr.setText(R.string.update);
								binding.btnUpdatePr.setEnabled(true);
							}
						});

		prViewModel
				.getActionMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								binding.mergeLoader.setVisibility(View.GONE);
								binding.btnMerge.setText(R.string.mergePullRequestButtonText);
								binding.btnMerge.setEnabled(true);

								Toasty.show(requireContext(), msg);
								prViewModel.clearActionMessage();
								triggerParentRefresh();
								dismiss();
							}
						});

		issueActionsViewModel
				.getActionMessage()
				.observe(
						getViewLifecycleOwner(),
						msg -> {
							if (msg != null) {
								Toasty.show(requireContext(), msg);
								issueActionsViewModel.clearActionMessage();
								triggerParentRefresh();
								dismiss();
							}
						});

		prViewModel
				.getActionError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								binding.mergeLoader.setVisibility(View.GONE);
								binding.updateLoader.setVisibility(View.GONE);
								binding.btnMerge.setText(R.string.mergePullRequestButtonText);
								binding.btnMerge.setEnabled(true);
								binding.btnUpdatePr.setText(R.string.update);
								binding.btnUpdatePr.setEnabled(true);

								if ("UNAUTHORIZED".equals(error)) {
									TokenAuthorizationDialog.authorizationTokenRevokedDialog(
											requireContext());
								} else {
									Toasty.show(requireContext(), error);
								}
								prViewModel.clearActionError();
							}
						});

		issueActionsViewModel
				.getActionError()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								if ("UNAUTHORIZED".equals(error)) {
									TokenAuthorizationDialog.authorizationTokenRevokedDialog(
											requireContext());
								} else {
									Toasty.show(requireContext(), error);
								}
								issueActionsViewModel.clearActionError();
							}
						});

		issueActionsViewModel
				.getIsSubscribed()
				.observe(
						getViewLifecycleOwner(),
						subscribed -> {
							if (subscribed != null && subscribed) {
								binding.subscribeIcon.setImageResource(R.drawable.ic_unwatch);
								binding.subscribeText.setText(R.string.singleIssueUnSubscribe);
							} else {
								binding.subscribeIcon.setImageResource(R.drawable.ic_watchers);
								binding.subscribeText.setText(R.string.singleIssueSubscribe);
							}
						});
	}

	private void triggerParentRefresh() {
		AppUIStateManager.refreshData();
		if (getActivity() instanceof BaseActivity) {
			((BaseActivity) getActivity()).triggerGlobalRefresh();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (getDialog() instanceof BottomSheetDialog dialog) {
			AppUtil.applyFullScreenSheetStyle(dialog, false);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private record MergeStrategy(String value, String label) {
		@NonNull @Override
		public String toString() {
			return label;
		}
	}
}
