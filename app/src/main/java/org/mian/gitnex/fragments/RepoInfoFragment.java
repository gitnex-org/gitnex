package org.mian.gitnex.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import org.apache.commons.io.FileUtils;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.activities.RepoStargazersActivity;
import org.mian.gitnex.activities.RepoWatchersActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepoInfoFragment extends Fragment {

	private Context ctx;
	private ProgressBar mProgressBar;
	private LinearLayout pageContent;
	private static String repoNameF = "param2";
	private static String repoOwnerF = "param1";

	private String repoName;
	private String repoOwner;
	private TextView repoMetaName;
	private TextView repoMetaDescription;
	private TextView repoMetaStars;
	private TextView repoMetaPullRequests;
	private LinearLayout repoMetaPullRequestsFrame;
	private TextView repoMetaForks;
	private TextView repoMetaSize;
	private TextView repoMetaWatchers;
	private TextView repoMetaCreatedAt;
	private TextView repoMetaWebsite;
	private Button repoAdditionalButton;
	private TextView repoFileContents;
	private LinearLayout repoMetaFrame;
	private ImageView repoMetaDataExpandCollapse;
	private ImageView repoFilenameExpandCollapse;
	private LinearLayout fileContentsFrameHeader;
	private LinearLayout fileContentsFrame;

	private OnFragmentInteractionListener mListener;

	public RepoInfoFragment() {

	}

	public static RepoInfoFragment newInstance(String param1, String param2) {
		RepoInfoFragment fragment = new RepoInfoFragment();
		Bundle args = new Bundle();
		args.putString(repoOwnerF, param1);
		args.putString(repoNameF, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			repoName = getArguments().getString(repoNameF);
			repoOwner = getArguments().getString(repoOwnerF);
		}
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_repo_info, container, false);

		TinyDB tinyDb = TinyDB.getInstance(getContext());

		final String locale = tinyDb.getString("locale");
		final String timeFormat = tinyDb.getString("dateFormat");

		ctx = getActivity();

		pageContent = v.findViewById(R.id.repoInfoLayout);
		pageContent.setVisibility(View.GONE);

		mProgressBar = v.findViewById(R.id.progress_bar);
		repoMetaName = v.findViewById(R.id.repoMetaName);
		repoMetaDescription = v.findViewById(R.id.repoMetaDescription);
		repoMetaStars = v.findViewById(R.id.repoMetaStars);
		repoMetaPullRequests = v.findViewById(R.id.repoMetaPullRequests);
		repoMetaPullRequestsFrame = v.findViewById(R.id.repoMetaPullRequestsFrame);
		repoMetaForks = v.findViewById(R.id.repoMetaForks);
		repoMetaSize = v.findViewById(R.id.repoMetaSize);
		repoMetaWatchers = v.findViewById(R.id.repoMetaWatchers);
		repoMetaCreatedAt = v.findViewById(R.id.repoMetaCreatedAt);
		repoMetaWebsite = v.findViewById(R.id.repoMetaWebsite);
		repoAdditionalButton = v.findViewById(R.id.repoAdditionalButton);
		repoFileContents = v.findViewById(R.id.repoFileContents);
		repoMetaFrame = v.findViewById(R.id.repoMetaFrame);
		LinearLayout repoMetaFrameHeader = v.findViewById(R.id.repoMetaFrameHeader);
		repoMetaDataExpandCollapse = v.findViewById(R.id.repoMetaDataExpandCollapse);
		repoFilenameExpandCollapse = v.findViewById(R.id.repoFilenameExpandCollapse);
		fileContentsFrameHeader = v.findViewById(R.id.fileContentsFrameHeader);
		fileContentsFrame = v.findViewById(R.id.fileContentsFrame);
		LinearLayout repoMetaStarsFrame = v.findViewById(R.id.repoMetaStarsFrame);
		LinearLayout repoMetaForksFrame = v.findViewById(R.id.repoMetaForksFrame);
		LinearLayout repoMetaWatchersFrame = v.findViewById(R.id.repoMetaWatchersFrame);

		repoMetaFrame.setVisibility(View.GONE);

		getRepoInfo(Authorization.get(getContext()), repoOwner, repoName, locale, timeFormat);
		getFileContents(Authorization.get(getContext()), repoOwner, repoName, getResources().getString(R.string.defaultFilename));

		if(isExpandViewVisible()) {
			toggleExpandView();
		}

		if(!isExpandViewMetaVisible()) {
			toggleExpandViewMeta();
		}

		fileContentsFrameHeader.setOnClickListener(v1 -> toggleExpandView());

		repoMetaFrameHeader.setOnClickListener(v12 -> toggleExpandViewMeta());

		repoMetaStarsFrame.setOnClickListener(metaStars -> {

			Intent intent = new Intent(ctx, RepoStargazersActivity.class);
			intent.putExtra("repoFullNameForStars", repoOwner + "/" + repoName);
			ctx.startActivity(intent);
		});

		repoMetaWatchersFrame.setOnClickListener(metaWatchers -> {

			Intent intent = new Intent(ctx, RepoWatchersActivity.class);
			intent.putExtra("repoFullNameForWatchers", repoOwner + "/" + repoName);
			ctx.startActivity(intent);
		});

		repoMetaPullRequestsFrame.setOnClickListener(metaPR -> RepoDetailActivity.mViewPager.setCurrentItem(3));

		return v;
	}

	public void onButtonPressed(Uri uri) {
		if (mListener != null) {
			mListener.onFragmentInteraction(uri);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	public interface OnFragmentInteractionListener {
		void onFragmentInteraction(Uri uri);
	}

	private void toggleExpandView() {

		if (repoFileContents.getVisibility() == View.GONE) {
			repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			repoFileContents.setVisibility(View.VISIBLE);
			//Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			//fileContentsFrame.startAnimation(slide_down);
		}
		else {
			repoFilenameExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			repoFileContents.setVisibility(View.GONE);
			//Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			//fileContentsFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewVisible() {
		return repoFileContents.getVisibility() == View.VISIBLE;
	}

	private void toggleExpandViewMeta() {

		if (repoMetaFrame.getVisibility() == View.GONE) {
			repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_up);
			repoMetaFrame.setVisibility(View.VISIBLE);
			//Animation slide_down = AnimationUtils.loadAnimation(getContext(), R.anim.slide_down);
			//repoMetaFrame.startAnimation(slide_down);
		}
		else {
			repoMetaDataExpandCollapse.setImageResource(R.drawable.ic_chevron_down);
			repoMetaFrame.setVisibility(View.GONE);
			//Animation slide_up = AnimationUtils.loadAnimation(getContext(), R.anim.slide_up);
			//repoMetaFrame.startAnimation(slide_up);
		}
	}

	private boolean isExpandViewMetaVisible() {
		return repoMetaFrame.getVisibility() == View.VISIBLE;
	}

	private void getRepoInfo(String token, final String owner, String repo, final String locale, final String timeFormat) {

		final TinyDB tinyDb = TinyDB.getInstance(getContext());

		Call<UserRepositories> call = RetrofitClient
				.getApiInterface(ctx)
				.getUserRepository(token, owner, repo);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if (isAdded()) {

					if (response.isSuccessful()) {

						if (response.code() == 200) {

							assert repoInfo != null;
							repoMetaName.setText(repoInfo.getName());

							if(!repoInfo.getDescription().isEmpty()) {
								repoMetaDescription.setText(repoInfo.getDescription());
							}
							else {
								repoMetaDescription.setText(getString(R.string.noDataDescription));
							}

							repoMetaStars.setText(repoInfo.getStars_count());

							if(repoInfo.getOpen_pull_count() != null) {
								repoMetaPullRequests.setText(repoInfo.getOpen_pull_count());
							}
							else {
								repoMetaPullRequestsFrame.setVisibility(View.GONE);
							}

							repoMetaForks.setText(repoInfo.getForks_count());
							repoMetaWatchers.setText(repoInfo.getWatchers_count());
							repoMetaSize.setText(FileUtils.byteCountToDisplaySize((int) (repoInfo.getSize() * 1024)));

							repoMetaCreatedAt.setText(TimeHelper.formatTime(repoInfo.getCreated_at(), new Locale(locale), timeFormat, ctx));
							if(timeFormat.equals("pretty")) {
								repoMetaCreatedAt.setOnClickListener(new ClickListener(TimeHelper.customDateFormatForToastDateFormat(repoInfo.getCreated_at()), ctx));
							}

							String repoMetaUpdatedAt = TimeHelper.formatTime(repoInfo.getUpdated_at(), new Locale(locale), timeFormat, ctx);

							String website = (repoInfo.getWebsite().isEmpty()) ? getResources().getString(R.string.noDataWebsite) : repoInfo.getWebsite();
							repoMetaWebsite.setText(website);

							repoAdditionalButton.setOnClickListener(v -> {

								View view = LayoutInflater.from(ctx).inflate(R.layout.layout_repo_more_info, null);

								TextView defaultBranchHeader = view.findViewById(R.id.defaultBranchHeader);
								TextView defaultBranchContent = view.findViewById(R.id.defaultBranchContent);

								TextView lastUpdatedHeader = view.findViewById(R.id.lastUpdatedHeader);
								TextView lastUpdatedContent = view.findViewById(R.id.lastUpdatedContent);

								TextView sshUrlHeader = view.findViewById(R.id.sshUrlHeader);
								TextView sshUrlContent = view.findViewById(R.id.sshUrlContent);

								TextView cloneUrlHeader = view.findViewById(R.id.cloneUrlHeader);
								TextView cloneUrlContent = view.findViewById(R.id.cloneUrlContent);

								TextView repoUrlHeader = view.findViewById(R.id.repoUrlHeader);
								TextView repoUrlContent = view.findViewById(R.id.repoUrlContent);

								defaultBranchHeader.setText(getString(R.string.infoTabRepoDefaultBranch));
								defaultBranchContent.setText(repoInfo.getDefault_branch());

								lastUpdatedHeader.setText(getString(R.string.infoTabRepoUpdatedAt));
								lastUpdatedContent.setText(repoMetaUpdatedAt);

								sshUrlHeader.setText(getString(R.string.infoTabRepoSshUrl));
								sshUrlContent.setText(repoInfo.getSsh_url());

								cloneUrlHeader.setText(getString(R.string.infoTabRepoCloneUrl));
								cloneUrlContent.setText(repoInfo.getClone_url());

								repoUrlHeader.setText(getString(R.string.infoTabRepoRepoUrl));
								repoUrlContent.setText(repoInfo.getHtml_url());

								AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctx);

								alertDialog.setTitle(getResources().getString(R.string.infoMoreInformation));
								alertDialog.setView(view);
								alertDialog.setPositiveButton(getString(R.string.okButton), null);
								alertDialog.create().show();

							});

							if(repoInfo.getHas_issues() != null) {
								tinyDb.putBoolean("hasIssues", repoInfo.getHas_issues());
							}
							else {
								tinyDb.putBoolean("hasIssues", true);
							}

							if(repoInfo.isHas_pull_requests()) {
								tinyDb.putBoolean("hasPullRequests", repoInfo.isHas_pull_requests());
							}
							else {
								tinyDb.putBoolean("hasPullRequests", false);
							}

							tinyDb.putString("repoHtmlUrl", repoInfo.getHtml_url());

							mProgressBar.setVisibility(View.GONE);
							pageContent.setVisibility(View.VISIBLE);

						}

					}
					else {
						Log.e("onFailure", String.valueOf(response.code()));
					}

				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});

	}

	private void getFileContents(String token, final String owner, String repo, final String filename) {

		Call<String> call = RetrofitClient
				.getApiInterface(getContext())
				.getFileContents(token, owner, repo, filename);

		call.enqueue(new Callback<String>() {

			@Override
			public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {

				if (isAdded()) {

					if (response.code() == 200) {

						new Markdown(ctx, response.body(), repoFileContents);

					} else if (response.code() == 401) {

						AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
								getResources().getString(R.string.alertDialogTokenRevokedMessage),
								getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
								getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

					} else if (response.code() == 403) {

						Toasty.error(ctx, ctx.getString(R.string.authorizeError));

					} else if (response.code() == 404) {

						fileContentsFrameHeader.setVisibility(View.GONE);
						fileContentsFrame.setVisibility(View.GONE);

					} else {

						Toasty.error(getContext(), getString(R.string.genericError));

					}
				}

			}

			@Override
			public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}
		});

	}

}
