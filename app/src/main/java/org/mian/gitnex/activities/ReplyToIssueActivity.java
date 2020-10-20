package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.DraftsApi;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Issues;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class ReplyToIssueActivity extends BaseActivity {

	public ImageView closeActivity;
	private View.OnClickListener onClickListener;

	final Context ctx = this;
	private Context appCtx;

	private TextView draftSaved;
	private EditText addComment;
	private Button replyButton;
	private String TAG = StaticGlobalVariables.replyToIssueActivity;
	private long draftIdOnCreate;

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

		if(getIntent().getStringExtra("draftId") != null) {

			draftIdOnCreate = Long.parseLong(Objects.requireNonNull(getIntent().getStringExtra("draftId")));
		}
		else {

			if(getIntent().getStringExtra("commentBody") != null) {
				draftIdOnCreate = returnDraftId(getIntent().getStringExtra("commentBody"));
			}
			else {
				draftIdOnCreate = returnDraftId("");
			}
		}

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

		if(getIntent().getStringExtra("commentAction") != null && Objects.equals(getIntent().getStringExtra("commentAction"), "edit") && !Objects.equals(getIntent().getStringExtra("commentId"), "new")) {

			final String commentId = getIntent().getStringExtra("commentId");

			toolbar_title.setText(getResources().getString(R.string.editCommentTitle));
			replyButton.setText(getResources().getString(R.string.editCommentButtonText));

			addComment.addTextChangedListener(new TextWatcher() {

				public void afterTextChanged(Editable s) {

				}

				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				public void onTextChanged(CharSequence s, int start, int before, int count) {

					saveDraft(addComment.getText().toString(), commentId, draftIdOnCreate);
					draftSaved.setVisibility(View.VISIBLE);
				}

			});

			replyButton.setOnClickListener(v -> {

				disableProcessButton();
				assert commentId != null;
				IssueActions.editIssueComment(ctx, Integer.parseInt(commentId), addComment.getText().toString(), draftIdOnCreate);
			});

			return;

		}

		addComment.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {

				saveDraft(addComment.getText().toString(), "new", draftIdOnCreate);
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

	private void saveDraft(String draftText, String commentId, long draftIdOnCreate) {

		TinyDB tinyDb = new TinyDB(getApplicationContext());

		int repositoryId = (int) tinyDb.getLong("repositoryId", 0);
		int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		int issueNumber = Integer.parseInt(tinyDb.getString("issueNumber"));

		DraftsApi draftsApi = new DraftsApi(appCtx);

		if(draftIdOnCreate == 0) {

			draftsApi.insertDraft(repositoryId, currentActiveAccountId, issueNumber, draftText, StaticGlobalVariables.draftTypeComment, commentId);
		}
		else {

			DraftsApi.updateDraft(draftText, (int) draftIdOnCreate, commentId); //updateDraftByIssueIdAsyncTask(draftText, issueNumber, repositoryId, commentId);
		}
	}

	private long returnDraftId(String draftText) {

		TinyDB tinyDb = new TinyDB(getApplicationContext());

		int repositoryId = (int) tinyDb.getLong("repositoryId", 0);
		int currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		int issueNumber = Integer.parseInt(tinyDb.getString("issueNumber"));

		DraftsApi draftsApi = new DraftsApi(appCtx);

		return draftsApi.insertDraft(repositoryId, currentActiveAccountId, issueNumber, draftText, StaticGlobalVariables.draftTypeComment, "");

	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private View.OnClickListener replyToIssue = v -> processNewCommentReply();

	private void processNewCommentReply() {

		String newReplyDT = addComment.getText().toString();
		boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

		if(!connToInternet) {

			Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
			return;
		}

		if(newReplyDT.equals("")) {

			Toasty.error(ctx, getString(R.string.commentEmptyError));
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

					Toasty.success(ctx, getString(R.string.commentSuccess));
					tinyDb.putBoolean("commentPosted", true);
					tinyDb.putBoolean("resumeIssues", true);
					tinyDb.putBoolean("resumePullRequests", true);

					// delete draft comment
					if(tinyDb.getBoolean("draftsCommentsDeletionEnabled")) {

						DraftsApi draftsApi = new DraftsApi(appCtx);
						draftsApi.deleteSingleDraft((int) draftIdOnCreate);
					}

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
					Toasty.error(ctx, getString(R.string.commentError));

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
	}

	private void enableProcessButton() {

		replyButton.setEnabled(true);
	}

}
