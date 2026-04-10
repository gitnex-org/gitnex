package org.mian.gitnex.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.gitnex.tea4j.v2.auth.ApiKeyAuth;
import org.gitnex.tea4j.v2.models.Release;
import org.gitnex.tea4j.v2.models.Tag;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CreateReleaseActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.adapters.TagsAdapter;
import org.mian.gitnex.databinding.BottomsheetReleaseItemMenuBinding;
import org.mian.gitnex.databinding.FragmentReleasesBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.EndlessRecyclerViewScrollListener;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.UIHelper;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import org.mian.gitnex.models.RepositoryMenuItemModel;
import org.mian.gitnex.notifications.Notifications;
import org.mian.gitnex.structs.FragmentRefreshListener;
import org.mian.gitnex.viewmodels.ReleasesViewModel;

/**
 * @author mmarif
 */
public class ReleasesFragment extends Fragment implements RepoDetailActivity.RepoHubProvider {

	private FragmentReleasesBinding binding;
	private ReleasesViewModel viewModel;
	private ReleasesAdapter releasesAdapter;
	private TagsAdapter tagsAdapter;
	private RepositoryContext repository;
	private int resultLimit;
	private EndlessRecyclerViewScrollListener scrollListener;
	private boolean isInitialLoad = true;
	public static String currentDownloadUrl = null;
	private boolean isFirstLoad = true;

	public interface OnReleaseItemClickListener extends FragmentRefreshListener {
		void onDelete(Object item, int position);

		void onDownload(String url);

		@Override
		default void onRefresh(String url) {
			onDownload(url);
		}
	}

	public static ReleasesFragment newInstance(RepositoryContext repository) {
		ReleasesFragment fragment = new ReleasesFragment();
		fragment.setArguments(repository.getBundle());
		return fragment;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		UIHelper.applyInsets(view, null, binding.recyclerView, binding.pullToRefresh, null);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		repository = RepositoryContext.fromBundle(requireArguments());
	}

	@Nullable @Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		binding = FragmentReleasesBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

		resultLimit = Constants.getCurrentResultLimit(requireContext());

		setupAdapters();
		setupListeners();
		observeViewModel();

		return binding.getRoot();
	}

	@Override
	public List<RepositoryMenuItemModel> getRepoHubItems() {
		List<RepositoryMenuItemModel> items = new ArrayList<>();

		boolean isShowingTags = repository.isReleasesViewTypeIsTag();
		items.add(
				new RepositoryMenuItemModel(
						"RELEASE_VIEW_TOGGLE",
						isShowingTags ? R.string.tabTextReleases : R.string.tags,
						isShowingTags ? R.drawable.ic_release : R.drawable.ic_tag,
						isShowingTags ? R.attr.colorTertiaryContainer : R.attr.colorSurfaceVariant,
						isShowingTags
								? R.attr.colorOnTertiaryContainer
								: R.attr.colorOnSurfaceVariant));

		if (repository.getPermissions().isPush() && !repository.getRepository().isArchived()) {
			items.add(
					new RepositoryMenuItemModel(
							"RELEASE_CREATE_NEW",
							R.string.createRelease,
							R.drawable.ic_add,
							R.attr.colorPrimaryContainer,
							R.attr.colorOnPrimaryContainer));
		}

		return items;
	}

	@Override
	public void onHubActionSelected(String actionId) {
		switch (actionId) {
			case "RELEASE_VIEW_TOGGLE":
				repository.setReleasesViewTypeIsTag(!repository.isReleasesViewTypeIsTag());
				refreshData();
				break;

			case "RELEASE_CREATE_NEW":
				startActivity(repository.getIntent(requireContext(), CreateReleaseActivity.class));
				break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (!isHidden() && isFirstLoad) {
			lazyLoad();
		}
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		if (!hidden && isFirstLoad) {
			lazyLoad();
		}
	}

	private void lazyLoad() {
		isFirstLoad = false;
		initDataFetch();
	}

	private void setupAdapters() {
		LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(layoutManager);

		scrollListener =
				new EndlessRecyclerViewScrollListener(layoutManager) {
					@Override
					public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
						if (repository.isReleasesViewTypeIsTag()) {
							viewModel.fetchTags(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									page,
									resultLimit,
									false);
						} else {
							viewModel.fetchReleases(
									requireContext(),
									repository.getOwner(),
									repository.getName(),
									page,
									resultLimit,
									false);
						}
					}
				};
		binding.recyclerView.addOnScrollListener(scrollListener);
	}

	private void setupListeners() {
		binding.pullToRefresh.setOnRefreshListener(this::refreshData);
	}

	private void initDataFetch() {
		isInitialLoad = true;
		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.resetTagsPagination();

		if (repository.isReleasesViewTypeIsTag()) {
			viewModel.fetchTags(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					1,
					resultLimit,
					true);
		} else {
			viewModel.fetchReleases(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					1,
					resultLimit,
					true);
		}
	}

	private void observeViewModel() {
		viewModel
				.getReleases()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (list == null) return;

							if (!repository.isReleasesViewTypeIsTag()) {
								if (isInitialLoad && list.isEmpty()) {
									repository.setReleasesViewTypeIsTag(true);
									viewModel.fetchTags(
											requireContext(),
											repository.getOwner(),
											repository.getName(),
											1,
											resultLimit,
											true);
									return;
								}
								handleReleasesView(list);
							}
						});

		viewModel
				.getTags()
				.observe(
						getViewLifecycleOwner(),
						list -> {
							if (list == null) return;

							if (repository.isReleasesViewTypeIsTag()) {
								handleTagsView(list);
							}
						});

		viewModel.getIsLoading().observe(getViewLifecycleOwner(), this::handleLoadingState);

		viewModel.getIsTagsLoading().observe(getViewLifecycleOwner(), this::handleLoadingState);

		viewModel
				.getError()
				.observe(
						getViewLifecycleOwner(),
						err -> {
							if (err != null) Toasty.show(requireContext(), err);
						});

		viewModel
				.getActionResult()
				.observe(
						getViewLifecycleOwner(),
						code -> {
							if (code == -1) return;
							if (code == 204) {
								int messageRes =
										repository.isReleasesViewTypeIsTag()
												? R.string.tagDeleted
												: R.string.releaseDeleted;

								Toasty.show(requireContext(), messageRes);
							} else {
								Toasty.show(requireContext(), R.string.genericError);
							}

							viewModel.resetActionResult();
						});
	}

	private void handleLoadingState(boolean loading) {
		if (loading) {
			binding.expressiveLoader.setVisibility(View.VISIBLE);
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
		} else {
			binding.expressiveLoader.setVisibility(View.GONE);
			binding.pullToRefresh.setRefreshing(false);
		}
	}

	private void handleReleasesView(List<Release> list) {
		isInitialLoad = false;
		if (releasesAdapter == null
				|| scrollListener.getCurrentPage() <= 1
				|| binding.recyclerView.getAdapter() != releasesAdapter) {

			boolean canDelete = repository.getPermissions().isPush();

			releasesAdapter =
					new ReleasesAdapter(
							requireContext(),
							list,
							canDelete,
							new OnReleaseItemClickListener() {
								@Override
								public void onDelete(Object item, int position) {
									Release release = (Release) item;
									showReleaseOptionsBottomSheet(release, position);
								}

								@Override
								public void onDownload(String url) {
									requestFileDownload(url);
								}
							});
			binding.recyclerView.setAdapter(releasesAdapter);
		} else {
			releasesAdapter.updateList(list);
		}
		updateEmptyState(list.isEmpty());
	}

	private void showReleaseOptionsBottomSheet(Release release, int position) {
		BottomsheetReleaseItemMenuBinding menuBinding =
				BottomsheetReleaseItemMenuBinding.inflate(getLayoutInflater());
		BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
		dialog.setContentView(menuBinding.getRoot());

		AppUtil.applySheetStyle(dialog, true);

		menuBinding.sheetTitle.setText(release.getName());

		menuBinding.deleteRelease.setOnClickListener(
				v -> {
					dialog.dismiss();
					new MaterialAlertDialogBuilder(requireContext())
							.setTitle(getString(R.string.deleteGenericTitle, release.getName()))
							.setMessage(R.string.deleteReleaseConfirmation)
							.setPositiveButton(
									R.string.menuDeleteText,
									(d, w) ->
											viewModel.deleteRelease(
													requireContext(),
													repository.getOwner(),
													repository.getName(),
													release.getId(),
													position))
							.setNegativeButton(R.string.cancelButton, null)
							.show();
				});

		dialog.show();
	}

	private void handleTagsView(List<Tag> list) {
		isInitialLoad = false;
		if (tagsAdapter == null
				|| scrollListener.getCurrentPage() <= 1
				|| binding.recyclerView.getAdapter() != tagsAdapter) {

			boolean canDelete = repository.getPermissions().isPush();

			tagsAdapter =
					new TagsAdapter(
							requireContext(),
							list,
							canDelete,
							new OnReleaseItemClickListener() {
								@Override
								public void onDelete(Object item, int position) {
									Tag tag = (Tag) item;
									new MaterialAlertDialogBuilder(requireContext())
											.setTitle(
													getString(
															R.string.deleteGenericTitle,
															tag.getName()))
											.setMessage(R.string.deleteTagConfirmation)
											.setPositiveButton(
													R.string.menuDeleteText,
													(d, w) ->
															viewModel.deleteTag(
																	requireContext(),
																	repository.getOwner(),
																	repository.getName(),
																	tag.getName(),
																	position))
											.setNegativeButton(R.string.cancelButton, null)
											.show();
								}

								@Override
								public void onDownload(String url) {
									requestFileDownload(url);
								}
							});
			binding.recyclerView.setAdapter(tagsAdapter);
		} else {
			tagsAdapter.updateList(list);
		}
		updateEmptyState(list.isEmpty());
	}

	private void refreshData() {

		scrollListener.resetState();
		viewModel.resetPagination();
		viewModel.resetTagsPagination();
		binding.expressiveLoader.setVisibility(View.VISIBLE);

		if (repository.isReleasesViewTypeIsTag()) {
			viewModel.fetchTags(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					1,
					resultLimit,
					true);
		} else {
			viewModel.fetchReleases(
					requireContext(),
					repository.getOwner(),
					repository.getName(),
					1,
					resultLimit,
					true);
		}
	}

	private void updateEmptyState(boolean isEmpty) {
		boolean loading = Boolean.TRUE.equals(viewModel.getIsLoading().getValue());

		if (isEmpty && !loading) {
			binding.layoutEmpty.getRoot().setVisibility(View.VISIBLE);
			binding.recyclerView.setVisibility(View.GONE);
		} else if (!isEmpty) {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.VISIBLE);
		} else {
			binding.layoutEmpty.getRoot().setVisibility(View.GONE);
			binding.recyclerView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void requestFileDownload(String url) {
		currentDownloadUrl = url;
		Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_TITLE, Uri.parse(url).getLastPathSegment());
		intent.setType("*/*");
		downloadLauncher.launch(intent);
	}

	private final ActivityResultLauncher<Intent> downloadLauncher =
			registerForActivityResult(
					new ActivityResultContracts.StartActivityForResult(),
					result -> {
						if (result.getResultCode() == Activity.RESULT_OK
								&& result.getData() != null) {
							executeDownload(result.getData().getData());
						}
					});

	private void executeDownload(Uri targetUri) {
		try {
			NotificationManager notificationManager =
					(NotificationManager)
							requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
			int notificationId = Notifications.uniqueNotificationId(requireContext());

			NotificationCompat.Builder builder =
					new NotificationCompat.Builder(
									requireContext(), Constants.downloadNotificationChannelId)
							.setContentTitle(getString(R.string.fileViewerNotificationTitleStarted))
							.setContentText(
									getString(
											R.string.fileViewerNotificationDescriptionStarted,
											Uri.parse(currentDownloadUrl).getLastPathSegment()))
							.setSmallIcon(R.drawable.gitnex_transparent)
							.setPriority(NotificationCompat.PRIORITY_LOW)
							.setOngoing(true);

			notificationManager.notify(notificationId, builder.build());

			SSLContext sslContext = SSLContext.getInstance("TLS");
			MemorizingTrustManager mtm = new MemorizingTrustManager(requireContext());
			sslContext.init(null, new X509TrustManager[] {mtm}, new SecureRandom());

			ApiKeyAuth auth = new ApiKeyAuth("header", "Authorization");
			auth.setApiKey(((BaseActivity) requireActivity()).getAccount().getWebAuthorization());

			OkHttpClient client =
					new OkHttpClient.Builder()
							.addInterceptor(auth)
							.sslSocketFactory(sslContext.getSocketFactory(), mtm)
							.hostnameVerifier(
									mtm.wrapHostnameVerifier(
											HttpsURLConnection.getDefaultHostnameVerifier()))
							.build();

			client.newCall(new Request.Builder().url(currentDownloadUrl).build())
					.enqueue(
							new Callback() {
								@Override
								public void onFailure(@NonNull Call call, @NonNull IOException e) {
									builder.setContentTitle(
													getString(
															R.string
																	.fileViewerNotificationTitleFailed))
											.setContentText(
													getString(
															R.string
																	.fileViewerNotificationDescriptionFailed,
															Uri.parse(currentDownloadUrl)
																	.getLastPathSegment()))
											.setOngoing(false);
									notificationManager.notify(notificationId, builder.build());
								}

								@Override
								public void onResponse(
										@NonNull Call call, @NonNull Response response)
										throws IOException {
									if (!response.isSuccessful()) {
										onFailure(call, new IOException());
										return;
									}
									try (OutputStream os =
											requireContext()
													.getContentResolver()
													.openOutputStream(targetUri)) {
										AppUtil.copyProgress(
												Objects.requireNonNull(response.body())
														.byteStream(),
												os,
												0,
												p -> {});
										builder.setContentTitle(
														getString(
																R.string
																		.fileViewerNotificationTitleFinished))
												.setContentText(
														getString(
																R.string
																		.fileViewerNotificationDescriptionFinished,
																Uri.parse(currentDownloadUrl)
																		.getLastPathSegment()))
												.setOngoing(false);
										notificationManager.notify(notificationId, builder.build());
									}
								}
							});
		} catch (Exception e) {
			Toasty.show(requireContext(), R.string.network_error);
		}
	}
}
