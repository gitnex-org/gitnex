package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.v2.models.Repository;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoForksActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.databinding.BottomsheetRepoAddTopicBinding;
import org.mian.gitnex.databinding.FragmentRepoInfoBinding;
import org.mian.gitnex.databinding.LayoutRepoLanguageStatisticsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.AvatarGenerator;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.helpers.languagestatistics.LanguageColor;
import org.mian.gitnex.helpers.languagestatistics.LanguageStatisticsHelper;
import org.mian.gitnex.viewmodels.CreateIssueViewModel;
import org.mian.gitnex.viewmodels.RepositoryDetailsViewModel;

/**
 * @author mmarif
 */
public class RepoInfoFragment extends Fragment {

	private Context ctx;
	private FragmentRepoInfoBinding binding;
	private RepositoryContext repositoryContext;
	private RepositoryDetailsViewModel viewModel;
	private boolean isAdmin;
	private Locale locale;

	public RepoInfoFragment() {}

	public static RepoInfoFragment newInstance(RepositoryContext repository) {
		RepoInfoFragment fragment = new RepoInfoFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repositoryContext = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentRepoInfoBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		ctx = requireContext();
		locale = getResources().getConfiguration().getLocales().get(0);
		viewModel = new ViewModelProvider(requireActivity()).get(RepositoryDetailsViewModel.class);

		UIHelper.applyInsets(view, null, null, null, binding.repoInfoLayout);

		setupStatHeaders();
		setupClickListeners();
		setupObservers();
		observeCreateIssueViewModel();

		Repository repo = repositoryContext.getRepository();
		setRepoInfo(repo);

		viewModel.loadRepositoryDetails(
				ctx, repositoryContext.getOwner(), repo.getName(), repo.getDefaultBranch());
	}

	private void observeCreateIssueViewModel() {
		CreateIssueViewModel createIssueViewModel =
				new ViewModelProvider(requireActivity()).get(CreateIssueViewModel.class);

		createIssueViewModel
				.getCreatedIssue()
				.observe(
						getViewLifecycleOwner(),
						issue -> {
							if (issue != null) {
								viewModel.fetchRepository(
										ctx,
										repositoryContext.getOwner(),
										repositoryContext.getName());
							}
						});
	}

	private void setupObservers() {
		viewModel
				.getIsStarred()
				.observe(
						getViewLifecycleOwner(),
						starred -> {
							binding.statStars.statIcon.setImageResource(
									starred ? R.drawable.ic_star : R.drawable.ic_star_unfilled);
						});
		viewModel
				.getIsWatched()
				.observe(
						getViewLifecycleOwner(),
						watched -> {
							binding.statWatchers.statIcon.setImageResource(
									watched ? R.drawable.ic_unwatch : R.drawable.ic_watchers);
						});

		viewModel
				.getLocalStarCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							binding.statStars.statCount.setText(AppUtil.numberFormatter(count));
						});
		viewModel
				.getLocalWatchCount()
				.observe(
						getViewLifecycleOwner(),
						count -> {
							binding.statWatchers.statCount.setText(AppUtil.numberFormatter(count));
						});

		viewModel
				.getRepoData()
				.observe(
						getViewLifecycleOwner(),
						repo -> {
							if (repo != null) setRepoInfo(repo);
						});

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							binding.expressiveLoader.setVisibility(
									loading ? View.VISIBLE : View.GONE);
							binding.repoInfoLayout.setVisibility(
									loading ? View.GONE : View.VISIBLE);
						});

		viewModel
				.getReadmeData()
				.observe(
						getViewLifecycleOwner(),
						content -> {
							if (content != null) {
								binding.layoutReadme.getRoot().setVisibility(View.VISIBLE);
								binding.moreInfoFrame.setVisibility(View.GONE);
								binding.repoAdditionalButton.setIconResource(
										R.drawable.ic_arrow_down);
								Markdown.render(
										ctx,
										content,
										binding.layoutReadme.repoFileContents,
										repositoryContext);
							} else {
								binding.layoutReadme.getRoot().setVisibility(View.GONE);
								binding.moreInfoFrame.setVisibility(View.VISIBLE);
								binding.repoAdditionalButton.setVisibility(View.GONE);
							}
						});

		viewModel
				.getProcessedLanguages()
				.observe(
						getViewLifecycleOwner(),
						items -> {
							Map<String, Long> langMap = viewModel.getLanguagesData().getValue();

							if (items != null && !items.isEmpty() && langMap != null) {
								binding.languageStatsContainer.setVisibility(View.VISIBLE);
								binding.languagesStatistic.setData(items);

								Map.Entry<String, Long> topLang = null;
								long totalBytes = 0;
								for (Map.Entry<String, Long> entry : langMap.entrySet()) {
									totalBytes += entry.getValue();
									if (topLang == null || entry.getValue() > topLang.getValue()) {
										topLang = entry;
									}
								}

								if (topLang != null) {
									String langName = topLang.getKey();
									float totalSpan = (float) totalBytes;
									String percentage =
											LanguageStatisticsHelper.calculatePercentage(
													topLang.getValue(), totalSpan);
									int color =
											ContextCompat.getColor(
													ctx, LanguageColor.languageColor(langName));

									binding.primaryLanguageText.setText(
											String.format("%s %s%%", langName, percentage));
									Drawable dot =
											AvatarGenerator.getCircleColorDrawable(ctx, color, 12);
									binding.primaryLanguageText
											.setCompoundDrawablesWithIntrinsicBounds(
													dot, null, null, null);
								}

								binding.languageStatsContainer.setOnClickListener(
										v -> showLanguageDetailDialog());
							} else {
								binding.languageStatsContainer.setVisibility(View.GONE);
							}
						});

		viewModel.getTopicsData().observe(getViewLifecycleOwner(), this::displayTopics);

		viewModel
				.getActionSuccessEvent()
				.observe(
						getViewLifecycleOwner(),
						resId -> {
							if (resId != null) {
								Toasty.show(ctx, getString(resId));
								viewModel.consumeActionEvents();
							}
						});

		viewModel
				.getErrorMessage()
				.observe(
						getViewLifecycleOwner(),
						error -> {
							if (error != null) {
								Toasty.show(ctx, error);
								viewModel.consumeActionEvents();
							}
						});
	}

	private void setupClickListeners() {
		binding.statStars
				.getRoot()
				.setOnClickListener(
						v ->
								ctx.startActivity(
										repositoryContext.getIntent(
												ctx, RepoStargazersActivity.class)));
		binding.statWatchers
				.getRoot()
				.setOnClickListener(
						v ->
								ctx.startActivity(
										repositoryContext.getIntent(
												ctx, RepoWatchersActivity.class)));
		binding.statForks
				.getRoot()
				.setOnClickListener(
						v ->
								ctx.startActivity(
										repositoryContext.getIntent(ctx, RepoForksActivity.class)));
		binding.statIssues
				.getRoot()
				.setOnClickListener(
						v -> {
							if (getActivity() instanceof RepoDetailActivity activity) {
								activity.switchTab("issues", R.id.btn_nav_issues);
							}
						});

		binding.statPRs
				.getRoot()
				.setOnClickListener(
						v -> {
							if (getActivity() instanceof RepoDetailActivity activity) {
								activity.switchTab("prs", R.id.btn_nav_prs);
							}
						});

		binding.statBranch
				.getRoot()
				.setOnClickListener(
						v -> {
							if (getActivity() instanceof RepoDetailActivity activity) {
								activity.switchTab("files", R.id.btn_nav_files);
							}
						});
		binding.statUpdatedAt
				.getRoot()
				.setOnClickListener(
						v ->
								Toasty.show(
										ctx,
										TimeHelper.getFullDateTime(
												repositoryContext.getRepository().getUpdatedAt(),
												locale)));
		binding.layoutCreatedAt
				.getRoot()
				.setOnClickListener(
						v ->
								Toasty.show(
										ctx,
										TimeHelper.getFullDateTime(
												repositoryContext.getRepository().getCreatedAt(),
												locale)));

		binding.layoutWebsite
				.getRoot()
				.setOnClickListener(
						v ->
								AppUtil.openUrlInBrowser(
										ctx, repositoryContext.getRepository().getWebsite()));
		binding.layoutHtmlUrl
				.getRoot()
				.setOnClickListener(
						v ->
								AppUtil.copyToClipboard(
										ctx,
										repositoryContext.getRepository().getWebsite(),
										getString(R.string.copied)));

		binding.addTopicChip.setOnClickListener(v -> showAddTopicDialog());
		binding.repoAdditionalButton.setOnClickListener(
				v -> {
					boolean isVisible = binding.moreInfoFrame.getVisibility() == View.VISIBLE;
					binding.moreInfoFrame.setVisibility(isVisible ? View.GONE : View.VISIBLE);
					binding.repoAdditionalButton.setIconResource(
							isVisible ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up);
				});
	}

	private void setRepoInfo(Repository repo) {
		isAdmin = repo.getPermissions() != null && repo.getPermissions().isAdmin();
		loadAvatar(repo);

		binding.repoMetaName.setText(repo.getName());
		binding.repoMetaOwner.setText(repo.getOwner().getLogin());

		viewModel.setInitialCounts(repo.getStarsCount(), repo.getWatchersCount());

		int lockIcon = repo.isPrivate() ? R.drawable.ic_lock : 0;
		binding.repoMetaName.setCompoundDrawablesWithIntrinsicBounds(lockIcon, 0, 0, 0);
		TextViewCompat.setCompoundDrawableTintList(
				binding.repoMetaName,
				ColorStateList.valueOf(
						AppUtil.getColorFromAttribute(ctx, android.R.attr.textColorPrimary)));

		if (!repo.getDescription().isEmpty()) {
			Markdown.render(ctx, repo.getDescription(), binding.repoMetaDescription);
		} else {
			binding.repoMetaDescription.setText(getString(R.string.noDataDescription));
		}

		setPluralStats(repo);

		binding.statSize.statCount.setText(FileUtils.byteCountToDisplaySize(repo.getSize() * 1024));
		binding.statBranch.statCount.setText(repo.getDefaultBranch());
		binding.statUpdatedAt.statCount.setText(TimeHelper.formatTime(repo.getUpdatedAt(), locale));

		binding.layoutCreatedAt.infoText.setText(
				TimeHelper.formatTime(repo.getCreatedAt(), locale));
		binding.layoutCreatedAt.infoIcon.setImageResource(R.drawable.ic_calendar);

		binding.layoutWebsite.infoText.setText(
				repo.getWebsite().isEmpty()
						? getString(R.string.noDataWebsite)
						: repo.getWebsite());
		binding.layoutWebsite.infoIcon.setImageResource(R.drawable.ic_browser);

		binding.layoutHtmlUrl.infoText.setText(repo.getHtmlUrl());
		binding.layoutHtmlUrl.infoIcon.setImageResource(R.drawable.ic_link);

		binding.layoutSshUrl.infoText.setText(repo.getSshUrl());
		binding.layoutSshUrl.infoIcon.setImageResource(R.drawable.ic_code);

		binding.repoIsArchived.setVisibility(repo.isArchived() ? View.VISIBLE : View.GONE);

		if (repo.isFork()) {
			binding.repoIsFork.setVisibility(View.VISIBLE);
			binding.repoIsFork.setText(
					getString(R.string.repoForkOf, repo.getParent().getFullName()));
		} else {
			binding.repoIsFork.setVisibility(View.GONE);
		}
	}

	private void setupStatHeaders() {
		binding.statStars.statIcon.setImageResource(R.drawable.ic_star);
		binding.statForks.statIcon.setImageResource(R.drawable.ic_fork);
		binding.statWatchers.statIcon.setImageResource(R.drawable.ic_watchers);
		binding.statPRs.statIcon.setImageResource(R.drawable.ic_pull_request);
		binding.statIssues.statIcon.setImageResource(R.drawable.ic_issue);
		binding.statSize.statIcon.setImageResource(R.drawable.ic_download);
		binding.statUpdatedAt.statIcon.setImageResource(R.drawable.ic_clock);
		binding.statBranch.statIcon.setImageResource(R.drawable.ic_branch);

		binding.statSize.statLabel.setText(getString(R.string.size));
		binding.statBranch.statLabel.setText(getString(R.string.default_branch));
		binding.statUpdatedAt.statLabel.setText(getString(R.string.updated));
	}

	private void setPluralStats(Repository repo) {
		int stars = Math.toIntExact(repo.getStarsCount());
		int forks = Math.toIntExact(repo.getForksCount());
		int watchers = Math.toIntExact(repo.getWatchersCount());
		int prs = Math.toIntExact(repo.getOpenPrCounter());
		int issues = Math.toIntExact(repo.getOpenIssuesCount());

		binding.statStars.statLabel.setText(
				getResources().getQuantityString(R.plurals.repoStars, stars));
		binding.statStars.statCount.setText(AppUtil.numberFormatter(stars));

		binding.statForks.statLabel.setText(
				getResources().getQuantityString(R.plurals.repoForks, forks));
		binding.statForks.statCount.setText(AppUtil.numberFormatter(forks));

		binding.statWatchers.statLabel.setText(
				getResources().getQuantityString(R.plurals.repoWatchers, watchers));
		binding.statWatchers.statCount.setText(AppUtil.numberFormatter(watchers));

		binding.statPRs.statLabel.setText(
				getResources().getQuantityString(R.plurals.repoPullRequests, prs));
		binding.statPRs.statCount.setText(AppUtil.numberFormatter(prs));

		binding.statIssues.statLabel.setText(
				getResources().getQuantityString(R.plurals.repoOpenIssues, issues));
		binding.statIssues.statCount.setText(AppUtil.numberFormatter(issues));
	}

	private void displayTopics(List<String> topics) {
		binding.repoTopicsChipGroup.removeAllViews();

		if ((topics == null || topics.isEmpty()) && !isAdmin) {
			binding.repoTopicsContainer.setVisibility(View.GONE);
			return;
		}

		binding.repoTopicsContainer.setVisibility(View.VISIBLE);

		int[] chipColors = {
			ContextCompat.getColor(ctx, R.color.chipColor1),
			ContextCompat.getColor(ctx, R.color.chipColor2),
			ContextCompat.getColor(ctx, R.color.chipColor3),
			ContextCompat.getColor(ctx, R.color.chipColor4),
			ContextCompat.getColor(ctx, R.color.chipColor5)
		};

		for (int i = 0; i < Objects.requireNonNull(topics).size(); i++) {
			Chip chip = createTopicChip(topics.get(i), chipColors[i % chipColors.length]);
			binding.repoTopicsChipGroup.addView(chip);
		}

		if (isAdmin) {
			binding.repoTopicsChipGroup.addView(binding.addTopicChip);
		}
	}

	private Chip createTopicChip(String topic, int color) {
		Chip chip = new Chip(ctx);
		chip.setText(topic);
		chip.setCloseIconVisible(isAdmin);
		if (isAdmin) {
			chip.setCloseIconTint(
					ColorStateList.valueOf(ContextCompat.getColor(ctx, R.color.colorRed)));
			chip.setOnCloseIconClickListener(
					v ->
							viewModel.deleteTopic(
									ctx,
									repositoryContext.getOwner(),
									repositoryContext.getName(),
									topic));
		}
		chip.setChipBackgroundColor(ColorStateList.valueOf(color));
		chip.setTextColor(AppUtil.isLightColor(color) ? Color.BLACK : Color.WHITE);
		chip.setShapeAppearanceModel(
				new ShapeAppearanceModel()
						.toBuilder()
								.setAllCorners(
										CornerFamily.ROUNDED,
										getResources().getDimension(R.dimen.dimen8dp))
								.build());
		return chip;
	}

	private void showAddTopicDialog() {
		BottomsheetRepoAddTopicBinding sheetBinding =
				BottomsheetRepoAddTopicBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(ctx);
		dialog.setContentView(sheetBinding.getRoot());
		AppUtil.applySheetStyle(dialog, false);
		sheetBinding.btnClose.setOnClickListener(v -> dialog.dismiss());
		sheetBinding.btnAddTopic.setOnClickListener(
				v -> {
					String name =
							Objects.requireNonNull(sheetBinding.topicInput.getText())
									.toString()
									.trim();
					if (!name.isEmpty()) {
						viewModel.addNewTopic(
								ctx,
								repositoryContext.getOwner(),
								repositoryContext.getName(),
								name);
						dialog.dismiss();
					} else {
						Toasty.show(ctx, getString(R.string.emptyFields));
					}
				});
		dialog.show();
	}

	private void loadAvatar(Repository repo) {
		Drawable placeholder = AvatarGenerator.getLetterAvatar(ctx, repo.getName(), 44);
		Glide.with(this)
				.load(repo.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.error(placeholder)
				.centerCrop()
				.into(binding.repoAvatar);
	}

	private void showLanguageDetailDialog() {
		Map<String, Long> langMap = viewModel.getLanguagesData().getValue();
		if (langMap == null || langMap.isEmpty()) return;

		LayoutRepoLanguageStatisticsBinding binding =
				LayoutRepoLanguageStatisticsBinding.inflate(LayoutInflater.from(ctx));

		BottomSheetDialog dialog = new BottomSheetDialog(ctx);
		dialog.setContentView(binding.getRoot());

		AppUtil.applySheetStyle(dialog, true);

		binding.langColor.removeAllViews();
		float totalSpan = (float) langMap.values().stream().mapToDouble(a -> a).sum();

		int margin8 = ctx.getResources().getDimensionPixelSize(R.dimen.dimen2dp);

		for (Map.Entry<String, Long> entry : langMap.entrySet()) {
			Chip chip = new Chip(ctx);
			LinearLayout.LayoutParams params =
					new LinearLayout.LayoutParams(
							ViewGroup.LayoutParams.MATCH_PARENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(0, margin8, 0, 0);
			chip.setLayoutParams(params);

			String percentage =
					LanguageStatisticsHelper.calculatePercentage(entry.getValue(), totalSpan);
			chip.setText(String.format("%s — %s%%", entry.getKey(), percentage));

			int color = ContextCompat.getColor(ctx, LanguageColor.languageColor(entry.getKey()));
			chip.setChipBackgroundColor(ColorStateList.valueOf(color));
			chip.setTextColor(AppUtil.isLightColor(color) ? Color.BLACK : Color.WHITE);

			chip.setShapeAppearanceModel(
					chip.getShapeAppearanceModel().toBuilder()
							.setAllCorners(CornerFamily.ROUNDED, 48f)
							.build());

			chip.setClickable(false);
			chip.setFocusable(false);
			chip.setCheckable(false);

			binding.langColor.addView(chip);
		}

		dialog.show();
	}
}
