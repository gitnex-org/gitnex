package org.mian.gitnex.fragments;

import static org.mian.gitnex.helpers.languagestatistics.LanguageColor.languageColor;
import static org.mian.gitnex.helpers.languagestatistics.LanguageStatisticsHelper.calculatePercentage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.amulyakhare.textdrawable.TextDrawable;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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
import org.mian.gitnex.helpers.languagestatistics.SeekbarItem;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @author M M Arif
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
		Locale locale = getResources().getConfiguration().locale;

		pageContent = binding.repoInfoLayout;
		pageContent.setVisibility(View.GONE);

		binding.repoMetaFrame.setVisibility(View.GONE);

		setRepoInfo(locale);

		if (isExpandViewVisible()) {
			toggleExpandView();
		}

		if (!isExpandViewMetaVisible()) {
			toggleExpandViewMeta();
		}

		binding.fileContentsFrameHeader.setOnClickListener(v1 -> toggleExpandView());
		binding.repoMetaFrameHeader.setOnClickListener(v12 -> toggleExpandViewMeta());

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

	private void toggleExpandView() {

		if (binding.repoFileContents.getVisibility() == View.GONE) {
			binding.repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			binding.repoFileContents.setVisibility(View.VISIBLE);
			// Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			// binding.fileContentsFrame.startAnimation(slide_down);
		} else {
			binding.repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			binding.repoFileContents.setVisibility(View.GONE);
			// Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			// binding.fileContentsFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewVisible() {
		return binding.repoFileContents.getVisibility() == View.VISIBLE;
	}

	private void toggleExpandViewMeta() {

		if (binding.repoMetaFrame.getVisibility() == View.GONE) {
			binding.repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			binding.repoMetaFrame.setVisibility(View.VISIBLE);
			// Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			// binding.repoMetaFrame.startAnimation(slide_down);
		} else {
			binding.repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			binding.repoMetaFrame.setVisibility(View.GONE);
			// Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			// binding.repoMetaFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewMetaVisible() {
		return binding.repoMetaFrame.getVisibility() == View.VISIBLE;
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
									if (response.body().size() > 0) {

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

										binding.languagesStatistic.setVisibility(View.VISIBLE);
										binding.languagesStatistic.initData(seekbarItemList);

										binding.languagesStatistic.setOnSeekBarChangeListener(
												new SeekBar.OnSeekBarChangeListener() {

													@Override
													public void onStopTrackingTouch(
															SeekBar seekBar) {}

													@Override
													public void onStartTrackingTouch(
															SeekBar seekBar) {}

													@Override
													public void onProgressChanged(
															SeekBar seekBar,
															int progress,
															boolean fromUser) {

														View view =
																LayoutInflater.from(ctx)
																		.inflate(
																				R.layout
																						.layout_repo_language_statistics,
																				null);

														LinearLayout.LayoutParams params =
																new LinearLayout.LayoutParams(
																		LinearLayout.LayoutParams
																				.WRAP_CONTENT,
																		LinearLayout.LayoutParams
																				.WRAP_CONTENT);
														params.setMargins(0, 32, 32, 0);

														LinearLayout layout =
																view.findViewById(R.id.lang_color);
														layout.removeAllViews();

														for (Map.Entry<String, Long> entry :
																response.body().entrySet()) {

															LinearLayout layoutSub =
																	new LinearLayout(getContext());
															layoutSub.setOrientation(
																	LinearLayout.HORIZONTAL);
															layoutSub.setGravity(
																	Gravity.START | Gravity.TOP);
															layout.addView(layoutSub);

															ImageView colorView =
																	new ImageView(getContext());
															colorView.setLayoutParams(params);

															String hexColor =
																	String.format(
																			"#%06X",
																			(0xFFFFFF
																					& languageColor(
																							entry
																									.getKey())));
															TextDrawable drawable =
																	TextDrawable.builder()
																			.beginConfig()
																			.width(64)
																			.height(64)
																			.endConfig()
																			.buildRoundRect(
																					"",
																					Color
																							.parseColor(
																									hexColor),
																					8);
															drawable.setTint(
																	getResources()
																			.getColor(
																					languageColor(
																							entry
																									.getKey()),
																					null));
															colorView.setImageDrawable(drawable);
															layoutSub.addView(colorView);

															TextView langName =
																	new TextView(getContext());
															langName.setLayoutParams(params);
															langName.setText(entry.getKey());
															layoutSub.addView(langName);

															TextView langPercentage =
																	new TextView(getContext());
															langPercentage.setLayoutParams(params);
															langPercentage.setTextSize(12);
															langPercentage.setText(
																	getString(
																			R.string
																					.lang_percentage,
																			calculatePercentage(
																					entry
																							.getValue(),
																					totalSpan)));
															layoutSub.addView(langPercentage);
														}

														MaterialAlertDialogBuilder
																materialAlertDialogBuilder1 =
																		new MaterialAlertDialogBuilder(
																						ctx)
																				.setTitle(
																						R.string
																								.lang_statistics)
																				.setView(view)
																				.setNeutralButton(
																						getString(
																								R
																										.string
																										.close),
																						null);

														materialAlertDialogBuilder1.create().show();
													}
												});
									}

									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(ctx);
									break;

								case 403:
									Toasty.error(ctx, ctx.getString(R.string.authorizeError));
									binding.languagesStatistic.setVisibility(View.GONE);
									break;

								case 404:
									binding.languagesStatistic.setVisibility(View.GONE);
									break;

								default:
									Toasty.error(getContext(), getString(R.string.genericError));
									binding.languagesStatistic.setVisibility(View.GONE);
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

			binding.repoMetaStars.setText(String.valueOf(repoInfo.getStarsCount()));

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
			binding.websiteFrame.setOnClickListener(
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
																				Toasty.error(
																						ctx,
																						ctx
																								.getString(
																										R
																												.string
																												.genericError));
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

								case 403:
									Toasty.error(ctx, ctx.getString(R.string.authorizeError));
									binding.fileContentsFrameHeader.setVisibility(View.GONE);
									binding.fileContentsFrame.setVisibility(View.GONE);
									break;

								case 404:
									binding.fileContentsFrameHeader.setVisibility(View.GONE);
									binding.fileContentsFrame.setVisibility(View.GONE);
									break;

								default:
									Toasty.error(getContext(), getString(R.string.genericError));
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
