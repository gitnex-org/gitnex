package org.mian.gitnex.fragments;

import static org.mian.gitnex.helpers.languagestatistics.LanguageColor.languageColor;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.gitnex.tea4j.v2.models.Organization;
import org.gitnex.tea4j.v2.models.Repository;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.OrganizationDetailActivity;
import org.mian.gitnex.activities.ProfileActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoForksActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentRepoInfoBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.helpers.languagestatistics.LanguageColor;
import org.mian.gitnex.helpers.languagestatistics.LanguageStatisticsHelper;
import org.mian.gitnex.helpers.languagestatistics.SeekbarItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author mmarif
 */
public class RepoInfoFragment extends Fragment {

	private Context ctx;
	private LinearLayout pageContent;

	private FragmentRepoInfoBinding binding;

	private RepositoryContext repository;

	public RepoInfoFragment() {}

	public static RepoInfoFragment newInstance(RepositoryContext repository) {
		RepoInfoFragment fragment = new RepoInfoFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentRepoInfoBinding.inflate(inflater, container, false);
		ctx = getContext();
		Locale locale = getResources().getConfiguration().getLocales().get(0);

		pageContent = binding.repoInfoLayout;
		pageContent.setVisibility(View.GONE);

		setRepoInfo(locale);

		if (repository.isStarred()) {
			binding.repoMetaStars.setIcon(
					ContextCompat.getDrawable(requireContext(), R.drawable.ic_star));
		} else {
			binding.repoMetaStars.setIcon(
					ContextCompat.getDrawable(requireContext(), R.drawable.ic_star_unfilled));
		}

		binding.repoMetaStars.setOnClickListener(
				metaStars ->
						ctx.startActivity(repository.getIntent(ctx, RepoStargazersActivity.class)));

		binding.repoMetaWatchers.setOnClickListener(
				metaWatchers ->
						ctx.startActivity(repository.getIntent(ctx, RepoWatchersActivity.class)));

		binding.repoMetaForks.setOnClickListener(
				metaForks -> ctx.startActivity(repository.getIntent(ctx, RepoForksActivity.class)));

		binding.repoMetaPullRequests.setOnClickListener(
				metaPR -> ((RepoDetailActivity) requireActivity()).viewPager.setCurrentItem(3));

		setLanguageStatistics();

		return binding.getRoot();
	}

	private void setLanguageStatistics() {

		Call<Map<String, Long>> call =
				RetrofitClient.getApiInterface(getContext())
						.repoGetLanguages(repository.getOwner(), repository.getName());

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Map<String, Long>> call,
							@NonNull Response<Map<String, Long>> response) {

						if (isAdded()) {

							switch (response.code()) {
								case 200:
									assert response.body() != null;
									if (!response.body().isEmpty()) {

										ArrayList<SeekbarItem> seekbarItemList = new ArrayList<>();

										float totalSpan =
												(float)
														response.body().values().stream()
																.mapToDouble(a -> a)
																.sum();

										for (Map.Entry<String, Long> entry :
												response.body().entrySet()) {

											SeekbarItem seekbarItem = new SeekbarItem();

											seekbarItem.progressItemPercentage =
													(entry.getValue() / totalSpan) * 100;
											seekbarItem.color = languageColor(entry.getKey());
											seekbarItemList.add(seekbarItem);
										}

										binding.languageStatsCard.setVisibility(View.VISIBLE);
										binding.languagesStatistic.setData(seekbarItemList);

										binding.languageStatsCard.setOnClickListener(
												v -> {
													View view =
															LayoutInflater.from(ctx)
																	.inflate(
																			R.layout
																					.layout_repo_language_statistics,
																			null);
													LinearLayout layout =
															view.findViewById(R.id.lang_color);
													layout.removeAllViews();

													for (Map.Entry<String, Long> entry :
															response.body().entrySet()) {
														Chip chip = new Chip(ctx);
														LinearLayout.LayoutParams params =
																new LinearLayout.LayoutParams(
																		LinearLayout.LayoutParams
																				.MATCH_PARENT,
																		LinearLayout.LayoutParams
																				.WRAP_CONTENT);
														params.setMargins(0, 8, 0, 0);
														chip.setLayoutParams(params);
														chip.setText(
																String.format(
																		"%s - %s%%",
																		entry.getKey(),
																		LanguageStatisticsHelper
																				.calculatePercentage(
																						entry
																								.getValue(),
																						totalSpan)));
														int bgColor =
																ctx.getResources()
																		.getColor(
																				LanguageColor
																						.languageColor(
																								entry
																										.getKey()),
																				null);
														chip.setChipBackgroundColor(
																ColorStateList.valueOf(bgColor));
														chip.setTextColor(
																isLightColor(bgColor)
																		? Color.BLACK
																		: Color.WHITE);
														chip.setShapeAppearanceModel(
																new ShapeAppearanceModel()
																		.toBuilder()
																				.setAllCorners(
																						CornerFamily
																								.ROUNDED,
																						48)
																				.build());
														chip.setEnabled(false);

														layout.addView(chip);
													}

													new MaterialAlertDialogBuilder(ctx)
															.setTitle(R.string.lang_statistics)
															.setView(view)
															.setNeutralButton(
																	getString(R.string.close), null)
															.create()
															.show();
												});
									}

									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(ctx);
									break;

								case 404:
									binding.languageStatsCard.setVisibility(View.GONE);
									break;
							}
						}
					}

					@Override
					public void onFailure(
							@NonNull Call<Map<String, Long>> call, @NonNull Throwable t) {
						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}

	private boolean isLightColor(int color) {
		double luminance =
				(0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color))
						/ 255;
		return luminance > 0.5;
	}

	private void setRepoInfo(Locale locale) {
		Repository repoInfo = repository.getRepository();

		if (isAdded()) {
			assert repoInfo != null;
			binding.repoMetaOwner.setText(repoInfo.getOwner().getLogin());
			binding.repoMetaOwner.setOnClickListener(
					(v) ->
							RetrofitClient.getApiInterface(ctx)
									.orgGet(repository.getOwner())
									.enqueue(
											new Callback<>() {

												@Override
												public void onResponse(
														@NotNull Call<Organization> call,
														@NotNull Response<Organization> response) {
													Intent intent =
															new Intent(
																	ctx,
																	response.isSuccessful()
																			? OrganizationDetailActivity
																					.class
																			: ProfileActivity
																					.class);
													intent.putExtra(
															response.isSuccessful()
																	? "orgName"
																	: "username",
															repository.getOwner());
													intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
													startActivity(intent);
												}

												@Override
												public void onFailure(
														@NotNull Call<Organization> call,
														@NotNull Throwable t) {}
											}));
			binding.repoMetaName.setText(repoInfo.getName());

			if (!repoInfo.getDescription().isEmpty()) {
				Markdown.render(ctx, repoInfo.getDescription(), binding.repoMetaDescription);
			} else {
				binding.repoMetaDescription.setText(getString(R.string.noDataDescription));
			}

			binding.repoMetaStars.setText(AppUtil.numberFormatter(repoInfo.getStarsCount()));

			if (repoInfo.getOpenPrCounter() != null) {
				binding.repoMetaPullRequests.setText(String.valueOf(repoInfo.getOpenPrCounter()));
			} else {
				binding.repoMetaPullRequests.setVisibility(View.GONE);
			}

			binding.repoMetaForks.setText(String.valueOf(repoInfo.getForksCount()));
			binding.repoMetaWatchers.setText(String.valueOf(repoInfo.getWatchersCount()));
			binding.repoMetaSize.setText(
					FileUtils.byteCountToDisplaySize(repoInfo.getSize() * 1024));

			binding.repoMetaCreatedAt.setText(
					TimeHelper.formatTime(repoInfo.getCreatedAt(), locale));
			binding.repoMetaCreatedAt.setOnClickListener(
					new ClickListener(
							TimeHelper.customDateFormatForToastDateFormat(repoInfo.getCreatedAt()),
							ctx));

			String repoMetaUpdatedAt = TimeHelper.formatTime(repoInfo.getUpdatedAt(), locale);

			String website =
					(repoInfo.getWebsite().isEmpty())
							? getResources().getString(R.string.noDataWebsite)
							: repoInfo.getWebsite();
			binding.repoMetaWebsite.setText(website);
			binding.repoMetaWebsite.setLinksClickable(false);
			binding.repoMetaWebsite.setOnClickListener(
					(v) -> {
						if (!repoInfo.getWebsite().isEmpty()) {
							AppUtil.openUrlInBrowser(requireContext(), repoInfo.getWebsite());
						}
					});

			binding.repoAdditionalButton.setOnClickListener(
					v -> {
						View view =
								LayoutInflater.from(ctx)
										.inflate(R.layout.layout_repo_more_info, null);

						TextView defaultBranchHeader = view.findViewById(R.id.defaultBranchHeader);
						TextView defaultBranchContent =
								view.findViewById(R.id.defaultBranchContent);

						TextView lastUpdatedHeader = view.findViewById(R.id.lastUpdatedHeader);
						TextView lastUpdatedContent = view.findViewById(R.id.lastUpdatedContent);

						TextView sshUrlHeader = view.findViewById(R.id.sshUrlHeader);
						TextView sshUrlContent = view.findViewById(R.id.sshUrlContent);

						TextView cloneUrlHeader = view.findViewById(R.id.cloneUrlHeader);
						TextView cloneUrlContent = view.findViewById(R.id.cloneUrlContent);

						TextView repoUrlHeader = view.findViewById(R.id.repoUrlHeader);
						TextView repoUrlContent = view.findViewById(R.id.repoUrlContent);

						defaultBranchHeader.setText(getString(R.string.infoTabRepoDefaultBranch));
						defaultBranchContent.setText(repoInfo.getDefaultBranch());

						lastUpdatedHeader.setText(getString(R.string.infoTabRepoUpdatedAt));
						lastUpdatedContent.setText(repoMetaUpdatedAt);

						sshUrlHeader.setText(getString(R.string.infoTabRepoSshUrl));
						sshUrlContent.setText(repoInfo.getSshUrl());

						cloneUrlHeader.setText(getString(R.string.infoTabRepoCloneUrl));
						cloneUrlContent.setText(repoInfo.getCloneUrl());

						repoUrlHeader.setText(getString(R.string.infoTabRepoRepoUrl));
						repoUrlContent.setText(repoInfo.getHtmlUrl());

						MaterialAlertDialogBuilder materialAlertDialogBuilder =
								new MaterialAlertDialogBuilder(ctx)
										.setTitle(R.string.infoMoreInformation)
										.setView(view)
										.setNeutralButton(getString(R.string.close), null);

						materialAlertDialogBuilder.create().show();
					});

			if (repoInfo.isArchived()) {
				binding.repoIsArchived.setVisibility(View.VISIBLE);
			} else {
				binding.repoIsArchived.setVisibility(View.GONE);
			}

			if (repoInfo.isFork()) {
				binding.repoForkFrame.setVisibility(View.VISIBLE);
				binding.repoForkFrame.setOnClickListener(
						(v) -> {
							Intent parent =
									new RepositoryContext(repoInfo.getParent(), requireContext())
											.getIntent(requireContext(), RepoDetailActivity.class);
							startActivity(parent);
						});
				binding.repoFork.setText(
						getString(R.string.repoForkOf, repoInfo.getParent().getFullName()));
			} else {
				binding.repoForkFrame.setVisibility(View.GONE);
			}

			getFileContents(
					repository.getOwner(),
					repository.getName(),
					getResources().getString(R.string.defaultFilename),
					repoInfo.getDefaultBranch());

			pageContent.setVisibility(View.VISIBLE);
		}
	}

	private void getFileContents(
			final String owner, String repo, final String filename, final String defBranch) {

		Call<ResponseBody> call =
				RetrofitClient.getWebInterface(getContext())
						.getFileContents(owner, repo, defBranch, filename);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<ResponseBody> call,
							@NonNull retrofit2.Response<ResponseBody> response) {

						if (isAdded()) {

							switch (response.code()) {
								case 200:
									assert response.body() != null;
									new Thread(
													() -> {
														try {
															Markdown.render(
																	ctx,
																	response.body().string(),
																	binding.repoFileContents,
																	repository);
														} catch (IOException e) {
															requireActivity()
																	.runOnUiThread(
																			() -> {
																				binding
																						.fileContentsFrameHeader
																						.setVisibility(
																								View
																										.GONE);
																				binding
																						.fileContentsFrame
																						.setVisibility(
																								View
																										.GONE);
																			});
														}
													})
											.start();
									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(ctx);
									break;

								case 404:
									binding.fileContentsFrameHeader.setVisibility(View.GONE);
									binding.fileContentsFrame.setVisibility(View.GONE);
									break;
							}
						}
					}

					@Override
					public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
						Toasty.error(ctx, ctx.getString(R.string.genericServerResponseError));
					}
				});
	}
}
