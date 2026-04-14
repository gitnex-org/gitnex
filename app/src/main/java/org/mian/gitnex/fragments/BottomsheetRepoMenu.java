package org.mian.gitnex.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.List;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.BottomsheetRepoMenuBinding;
import org.mian.gitnex.databinding.ItemRepoHubCardBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.models.RepositoryMenuItemModel;

/**
 * @author mmarif
 */
public class BottomsheetRepoMenu extends BottomSheetDialogFragment {

	private BottomsheetRepoMenuBinding binding;
	private List<RepositoryMenuItemModel> contextualItems;
	private RepositoryContext repositoryContext;
	private OnRepoMenuItemListener listener;
	private boolean isStarred, isWatched, isGiteaRepoActionsVisible, adminStatus;

	public interface OnRepoMenuItemListener {
		void onMenuItemSelected(String actionId);
	}

	public static BottomsheetRepoMenu newInstance(
			List<RepositoryMenuItemModel> items,
			RepositoryContext repositoryContext,
			boolean starred,
			boolean watched,
			boolean giteaActions,
			boolean admin) {
		BottomsheetRepoMenu sheet = new BottomsheetRepoMenu();
		Bundle args = new Bundle();
		args.putParcelableArrayList("items", new ArrayList<>(items));
		args.putBundle("repo_bundle", repositoryContext.getBundle());
		args.putBoolean("is_starred", starred);
		args.putBoolean("is_watched", watched);
		args.putBoolean("is_gitea_actions", giteaActions);
		args.putBoolean("admin_status", admin);
		sheet.setArguments(args);
		return sheet;
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BottomsheetRepoMenuBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				contextualItems =
						args.getParcelableArrayList("items", RepositoryMenuItemModel.class);
			} else {
				contextualItems = args.getParcelableArrayList("items");
			}

			isStarred = args.getBoolean("is_starred");
			isWatched = args.getBoolean("is_watched");
			isGiteaRepoActionsVisible = args.getBoolean("is_gitea_actions");
			adminStatus = args.getBoolean("admin_status");
			Bundle repoBundle = args.getBundle("repo_bundle");
			if (repoBundle != null) {
				repositoryContext = RepositoryContext.fromBundle(repoBundle);
			}
		}

		if (repositoryContext != null) {
			binding.repoNameTitle.setText(repositoryContext.getFullName());

			String branch = repositoryContext.getBranchRef();
			if (branch == null || branch.isEmpty()) {
				branch = repositoryContext.getRepository().getDefaultBranch();
			}

			binding.repoCurrentBranch.setText(branch);
			binding.branchFrame.setVisibility(
					branch != null && !branch.isEmpty() ? View.VISIBLE : View.GONE);
			setupCoreActions();
		}

		populateContextualGrid();
	}

	private void setupCoreActions() {
		List<RepositoryMenuItemModel> coreItems = new ArrayList<>();

		if (isGiteaRepoActionsVisible && adminStatus) {
			coreItems.add(
					new RepositoryMenuItemModel(
							"CORE_ACTIONS",
							R.string.actions,
							R.drawable.ic_actions,
							R.attr.colorTertiaryContainer,
							R.attr.colorOnTertiaryContainer));
		}

		coreItems.add(
				new RepositoryMenuItemModel(
						"CORE_STAR",
						isStarred ? R.string.unstar : R.string.starMember,
						isStarred ? R.drawable.ic_star : R.drawable.ic_star_unfilled,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		coreItems.add(
				new RepositoryMenuItemModel(
						"CORE_WATCH",
						isWatched ? R.string.unwatch : R.string.watch,
						isWatched ? R.drawable.ic_unwatch : R.drawable.ic_watchers,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		coreItems.add(
				new RepositoryMenuItemModel(
						"CORE_COPY",
						R.string.genericCopyUrl,
						R.drawable.ic_copy,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		coreItems.add(
				new RepositoryMenuItemModel(
						"CORE_SHARE",
						R.string.share,
						R.drawable.ic_share,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		coreItems.add(
				new RepositoryMenuItemModel(
						"CORE_BROWSER",
						R.string.openInBrowser,
						R.drawable.ic_browser,
						R.attr.colorPrimarySurface,
						R.attr.colorOnPrimarySurface));

		if (adminStatus) {
			coreItems.add(
					new RepositoryMenuItemModel(
							"CORE_SETTINGS",
							R.string.navSettings,
							R.drawable.ic_settings,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		populateGrid(binding.coreHubGrid, coreItems);
	}

	private void populateContextualGrid() {
		if (contextualItems == null || contextualItems.isEmpty()) {
			binding.actionTitle.setVisibility(View.GONE);
			binding.hubDivider.setVisibility(View.GONE);
			binding.repoHubGrid.setVisibility(View.GONE);
			return;
		}
		populateGrid(binding.repoHubGrid, contextualItems);
	}

	private void populateGrid(GridLayout grid, List<RepositoryMenuItemModel> items) {
		grid.removeAllViews();
		for (RepositoryMenuItemModel item : items) {
			ItemRepoHubCardBinding cardBinding =
					ItemRepoHubCardBinding.inflate(getLayoutInflater(), grid, false);

			int bgColor =
					MaterialColors.getColor(requireContext(), item.getBackgroundAttr(), Color.GRAY);
			int contentColor =
					MaterialColors.getColor(
							requireContext(), item.getContentColorAttr(), Color.WHITE);

			cardBinding.hubCard.setCardBackgroundColor(bgColor);
			cardBinding.hubIcon.setImageResource(item.getIconRes());
			cardBinding.hubIcon.setImageTintList(ColorStateList.valueOf(contentColor));
			cardBinding.hubText.setText(item.getLabelRes());
			cardBinding.hubText.setTextColor(contentColor);

			cardBinding.hubCard.setOnClickListener(
					v -> {
						if (listener != null) {
							listener.onMenuItemSelected(item.getId());
						}
						dismiss();
					});

			grid.addView(cardBinding.getRoot());
		}
	}

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		if (context instanceof OnRepoMenuItemListener) {
			listener = (OnRepoMenuItemListener) context;
		} else {
			throw new RuntimeException(context + " must implement OnRepoMenuItemListener");
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Dialog dialog = getDialog();
		if (dialog instanceof BottomSheetDialog) {
			AppUtil.applySheetStyle((BottomSheetDialog) dialog, true);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}
