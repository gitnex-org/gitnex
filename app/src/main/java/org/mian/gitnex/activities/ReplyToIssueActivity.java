package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class ReplyToIssueActivity extends BaseActivity {

	public ImageView closeActivity;
	private View.OnClickListener onClickListener;

	final Context ctx = this;
	private Context appCtx;

	private TextView draftSaved;
	private SocialAutoCompleteTextView addComment;
	private ArrayAdapter<Mention> defaultMentionAdapter;
	private Button replyButton;
	private String TAG = StaticGlobalVariables.replyToIssueActivity;

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_reply_to_issue;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);
        TinyDB tinyDb = new TinyDB(appCtx);

		draftSaved = findViewById(R.id.draftSaved);
		addComment = findViewById(R.id.addComment);
		addComment.setShowSoftInputOnFocus(true);

        defaultMentionAdapter = new MentionArrayAdapter<>(ctx);
		loadCollaboratorsList();

		addComment.setMentionAdapter(defaultMentionAdapter);

		closeActivity = findViewById(R.id.close);
		TextView toolbar_title = findViewById(R.id.toolbar_title);

		addComment.requestFocus();
		assert imm != null;
		imm.showSoftInput(addComment, InputMethodManager.SHOW_IMPLICIT);

		if(!tinyDb.getString("issueTitle").isEmpty()) {
			toolbar_title.setText(tinyDb.getString("issueTitle"));
		}

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		replyButton = findViewById(R.id.replyButton);

		if(getIntent().getStringExtra("commentBody") != null) {

			addComment.setText(getIntent().getStringExtra("commentBody"));

			if(getIntent().getBooleanExtra("cursorToEnd", false)) {
				addComment.setSelection(addComment.length());
			}

		}

		if(getIntent().getStringExtra("draftTitle") != null) {

			toolbar_title.setText(getIntent().getStringExtra("draftTitle"));

		}

		if(getIntent().getStringExtra("commentAction") != null && getIntent().getStringExtra("commentAction").equals("edit")) {

			final String commentId = getIntent().getStringExtra("commentId");

			toolbar_title.setText(getResources().getString(R.string.editCommentTitle));
			replyButton.setText(getResources().getString(R.string.editCommentButtonText));

			addComment.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {

				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {

				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {

					saveDraft(addComment.getText().toString());
					draftSaved.setVisibility(View.VISIBLE);

				}

			});

			replyButton.setOnClickListener(v -> {

				disableProcessButton();
				assert commentId != null;
				IssueActions.editIssueComment(ctx, Integer.parseInt(commentId), addComment.getText().toString());

			});

			return;

		}

		addComment.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {

				saveDraft(addComment.getText().toString());
				draftSaved.setVisibility(View.VISIBLE);

			}

		});

		if(!connToInternet) {

			disableProcessButton();

		}
		else {

			replyButton.setOnClickListener(replyToIssue);

		}

	}

	private void saveDraft(String draftText) {

		TinyDB tinyDb = new TinyDB(getApplicationContext());

		int repositoryId = (int) tinyDb.getLong("repositoryId", 0);
		int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		int issueNumber = Integer.parseInt(tinyDb.getString("issueNumber"));

		DraftsApi draftsApi = new DraftsApi(getApplicationContext());

		int countDraft = draftsApi.checkDraft(issueNumber, repositoryId);

		if(countDraft == 0) {
			long draftId = draftsApi.insertDraft(repositoryId, currentActiveAccountId, issueNumber, draftText, StaticGlobalVariables.draftTypeComment);
		}
		else {
			DraftsApi.updateDraftByIssueIdAsyncTask(draftText, issueNumber, repositoryId);
		}

	}

	public void loadCollaboratorsList() {

		final TinyDB tinyDb = new TinyDB(appCtx);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];

		Call<List<Collaborators>> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getCollaborators(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName);

		call.enqueue(new Callback<List<Collaborators>>() {

			@Override
			public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

				if (response.isSuccessful()) {

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

					Log.i(TAG, String.valueOf(response.code()));

				}

			}

			@Override
			public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
			}

		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private View.OnClickListener replyToIssue = v -> processNewCommentReply();

	private void processNewCommentReply() {

		String newReplyDT = addComment.getText().toString();
		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		if(!connToInternet) {

			Toasty.info(ctx, getResources().getString(R.string.checkNetConnection));
			return;

		}

		if(newReplyDT.equals("")) {

			Toasty.info(ctx, getString(R.string.commentEmptyError));

		}
		else {

			disableProcessButton();
			replyComment(newReplyDT);

		}

	}

	private void replyComment(String newReplyDT) {

		final TinyDB tinyDb = new TinyDB(appCtx);

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		final String repoOwner = parts[0];
		final String repoName = parts[1];
		final int issueIndex = Integer.parseInt(tinyDb.getString("issueNumber"));

		Issues issueComment = new Issues(newReplyDT);

		Call<Issues> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.replyCommentToIssue(Authorization.returnAuthentication(ctx, loginUid, instanceToken), repoOwner, repoName, issueIndex, issueComment);

		call.enqueue(new Callback<Issues>() {

			@Override
			public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

				if(response.code() == 201) {

					Toasty.info(ctx, getString(R.string.commentSuccess));
					tinyDb.putBoolean("commentPosted", true);
					tinyDb.putBoolean("resumeIssues", true);
					tinyDb.putBoolean("resumePullRequests", true);
					finish();

				}
				else if(response.code() == 401) {

					enableProcessButton();
					AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
						getResources().getString(R.string.alertDialogTokenRevokedMessage),
						getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
						getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

				}
				else {

					enableProcessButton();
					Toasty.info(ctx, getString(R.string.commentError));

				}

			}

			@Override
			public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {

				Log.e(TAG, t.toString());
				enableProcessButton();
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reply_to_issue, menu);

		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()) {

			case R.id.replyToIssueMenu:
				Intent fragmentIntent = new Intent(ReplyToIssueActivity.this, MainActivity.class);
				fragmentIntent.putExtra("launchFragment", "drafts");
				ReplyToIssueActivity.this.startActivity(fragmentIntent);
				break;

			default:
				return super.onOptionsItemSelected(item);

		}

		return true;
	}

	private void disableProcessButton() {

		replyButton.setEnabled(false);
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.hintColor));
		replyButton.setBackground(shape);

	}

	private void enableProcessButton() {

		replyButton.setEnabled(true);
		GradientDrawable shape = new GradientDrawable();
		shape.setCornerRadius(8);
		shape.setColor(getResources().getColor(R.color.btnBackground));
		replyButton.setBackground(shape);

	}

}
