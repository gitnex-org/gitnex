package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.MergePullRequest;
import org.mian.gitnex.models.MergePullRequestSpinner;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class MergePullRequestActivity extends BaseActivity {

	public ImageView closeActivity;
	private View.OnClickListener onClickListener;

	final Context ctx = this;

	private SocialAutoCompleteTextView mergeDescription;
	private EditText mergeTitle;
	private Spinner mergeModeSpinner;
	private ArrayAdapter<Mention> defaultMentionAdapter;
	private Button mergeButton;
	private String Do;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_merge_pull_request;
	}

	@SuppressLint("SetTextI18n")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
		TinyDB tinyDb = new TinyDB(getApplicationContext());

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		mergeModeSpinner = findViewById(R.id.mergeSpinner);
		mergeDescription = findViewById(R.id.mergeDescription);
		mergeTitle = findViewById(R.id.mergeTitle);

		mergeTitle.requestFocus();
		assert imm != null;
		imm.showSoftInput(mergeTitle, InputMethodManager.SHOW_IMPLICIT);

		setMergeAdapter();

		mergeModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				MergePullRequestSpinner mergeId = (MergePullRequestSpinner) parent.getSelectedItem();
				Do = mergeId.getId();

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}

		});

		defaultMentionAdapter = new MentionArrayAdapter<>(this);
		loadCollaboratorsList();

		mergeDescription.setMentionAdapter(defaultMentionAdapter);

		closeActivity = findViewById(R.id.close);
		TextView toolbar_title = findViewById(R.id.toolbar_title);

		if(!tinyDb.getString("issueTitle").isEmpty()) {
			toolbar_title.setText(tinyDb.getString("issueTitle"));
			mergeTitle.setText(tinyDb.getString("issueTitle") + " (#" + tinyDb.getString("issueNumber")+ ")");
		}

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		mergeButton = findViewById(R.id.mergeButton);

		if(!connToInternet) {

			disableProcessButton();

		}
		else {

			mergeButton.setOnClickListener(mergePullRequest);

		}

	}

	private void setMergeAdapter() {

		TinyDB tinyDb = new TinyDB(getApplicationContext());

		ArrayList<MergePullRequestSpinner> mergeList = new ArrayList<>();

		mergeList.add(new MergePullRequestSpinner("merge", getResources().getString(R.string.mergeOptionMerge)));
		mergeList.add(new MergePullRequestSpinner("rebase", getResources().getString(R.string.mergeOptionRebase)));
		mergeList.add(new MergePullRequestSpinner("rebase-merge", getResources().getString(R.string.mergeOptionRebaseCommit)));
		//squash merge works only on gitea v1.11.5 and higher due to a bug
		if(VersionCheck.compareVersion("1.11.5", tinyDb.getString("giteaVersion")) < 1) {
			mergeList.add(new MergePullRequestSpinner("squash", getResources().getString(R.string.mergeOptionSquash)));
		}

		ArrayAdapter<MergePullRequestSpinner> adapter = new ArrayAdapter<>(MergePullRequestActivity.this, R.layout.spinner_item, mergeList);
		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
		mergeModeSpinner.setAdapter(adapter);

	}

	public void loadCollaboratorsList() {

		final TinyDB tinyDb = new TinyDB(getApplicationContext());

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		Call<List<Collaborators>> call = RetrofitClient.getInstance(instanceUrl, getApplicationContext()).getApiInterface().getCollaborators(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

		call.enqueue(new Callback<List<Collaborators>>() {

			@Override
			public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					String fullName = "";
					for(int i = 0; i < response.body().size(); i++) {
						if(!response.body().get(i).getFull_name().equals("")) {
							fullName = response.body().get(i).getFull_name();
						}
						defaultMentionAdapter.add(new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
					}

				}
				else {

					Log.i("onResponse", String.valueOf(response.code()));

				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {

				Log.i("onFailure", t.toString());
			}

		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private View.OnClickListener mergePullRequest = v -> processMergePullRequest();

	private void processMergePullRequest() {

		String mergePRDesc = mergeDescription.getText().toString();
		String mergePRTitle = mergeTitle.getText().toString();

		boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

		if(!connToInternet) {

			Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
			return;

		}

		disableProcessButton();
		mergeFunction(Do, mergePRDesc, mergePRTitle);

	}

	private void mergeFunction(String Do, String mergePRDT, String mergeTitle) {

		final TinyDB tinyDb = new TinyDB(getApplicationContext());

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final int prIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

		MergePullRequest mergePR = new MergePullRequest(Do, mergePRDT, mergeTitle);

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, getApplicationContext()).getApiInterface().mergePullRequest(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, prIndex, mergePR);

		call.enqueue(new Callback<ResponseBody>() {

			@Override
			public void onResponse(@NonNull Call<ResponseBody> call, @NonNull retrofit2.Response<ResponseBody> response) {

				if(response.code() == 200) {

					Toasty.info(getApplicationContext(), getString(R.string.mergePRSuccessMsg));
					tinyDb.putBoolean("prMerged", true);
					tinyDb.putBoolean("resumePullRequests", true);
					finish();

				}
				else if(response.code() == 401) {

					enableProcessButton();
					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle), getResources().getString(R.string.alertDialogTokenRevokedMessage), getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton), getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else if(response.code() == 404) {

					enableProcessButton();
					Toasty.info(getApplicationContext(), getString(R.string.mergePR404ErrorMsg));

				}
				else {

					enableProcessButton();
					Toasty.info(getApplicationContext(), getString(R.string.genericError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
				enableProcessButton();
			}

		});

	}

	private void disableProcessButton() {

		mergeButton.setEnabled(false);
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.hintColor));
		mergeButton.setBackground(shape);

	}

	private void enableProcessButton() {

		mergeButton.setEnabled(true);
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.btnBackground));
		mergeButton.setBackground(shape);

	}

}
