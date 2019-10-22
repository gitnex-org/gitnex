package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.hendraanggrian.appcompat.socialview.Mention;
import com.hendraanggrian.appcompat.widget.MentionArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Collaborators;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class ReplyToIssueActivity extends AppCompatActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;

    final Context ctx = this;

    private SocialAutoCompleteTextView addComment;
    private ArrayAdapter<Mention> defaultMentionAdapter;
    private Button replyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_to_issue);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());

        addComment = findViewById(R.id.addComment);
        addComment.setShowSoftInputOnFocus(true);

        defaultMentionAdapter = new MentionArrayAdapter<>(this);
        loadCollaboratorsList();

        addComment.setMentionAdapter(defaultMentionAdapter);

        closeActivity = findViewById(R.id.close);
        TextView toolbar_title = findViewById(R.id.toolbar_title);

        if(!tinyDb.getString("issueTitle").isEmpty()) {
            toolbar_title.setText(tinyDb.getString("issueTitle"));
        }

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        replyButton = findViewById(R.id.replyButton);

        if(getIntent().getStringExtra("commentAction") != null && getIntent().getStringExtra("commentAction").equals("edit")) {

            addComment.setText(getIntent().getStringExtra("commentBody"));
            final String commentId = getIntent().getStringExtra("commentId");

            toolbar_title.setText(getResources().getString(R.string.editCommentTitle));
            replyButton.setText(getResources().getString(R.string.editCommentButtonText));

            replyButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    disableProcessButton();
                    IssueActions.editIssueComment(ctx, Integer.valueOf(commentId), addComment.getText().toString());
                }

            });

            return;

        }

        if(!connToInternet) {

            disableProcessButton();

        } else {

            replyButton.setOnClickListener(replyToIssue);

        }

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

        Call<List<Collaborators>> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getCollaborators(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName);

        call.enqueue(new Callback<List<Collaborators>>() {

            @Override
            public void onResponse(@NonNull Call<List<Collaborators>> call, @NonNull Response<List<Collaborators>> response) {

                if (response.isSuccessful()) {

                    assert response.body() != null;
                    String fullName = "";
                    for (int i = 0; i < response.body().size(); i++) {
                        if(!response.body().get(i).getFull_name().equals("")) {
                            fullName = response.body().get(i).getFull_name();
                        }
                        defaultMentionAdapter.add(
                                new Mention(response.body().get(i).getUsername(), fullName, response.body().get(i).getAvatar_url()));
                    }

                } else {

                    Log.i("onResponse", String.valueOf(response.code()));

                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Collaborators>> call, @NonNull Throwable t) {
                Log.i("onFailure", t.getMessage());
            }

        });
    }

    private void initCloseListener() {
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
    }

    private View.OnClickListener replyToIssue = new View.OnClickListener() {
        public void onClick(View v) {
            processNewCommentReply();
        }
    };

    private void processNewCommentReply() {

        String newReplyDT = addComment.getText().toString();
        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newReplyDT.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.commentEmptyError));

        }
        else {

            disableProcessButton();
            replyComment(newReplyDT);

        }

    }

    private void replyComment(String newReplyDT) {

        final TinyDB tinyDb = new TinyDB(getApplicationContext());

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
                .getInstance(instanceUrl)
                .getApiInterface()
                .replyCommentToIssue(Authorization.returnAuthentication(getApplicationContext(), loginUid, instanceToken), repoOwner, repoName, issueIndex, issueComment);

        call.enqueue(new Callback<Issues>() {

            @Override
            public void onResponse(@NonNull Call<Issues> call, @NonNull retrofit2.Response<Issues> response) {

                if(response.code() == 201) {

                    Toasty.info(getApplicationContext(), getString(R.string.commentSuccess));
                    tinyDb.putBoolean("commentPosted", true);
                    tinyDb.putBoolean("resumeIssues", true);
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
                    Toasty.info(getApplicationContext(), getString(R.string.commentError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void disableProcessButton() {

        replyButton.setEnabled(false);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.hintColor));
        replyButton.setBackground(shape);

    }

    private void enableProcessButton() {

        replyButton.setEnabled(true);
        GradientDrawable shape =  new GradientDrawable();
        shape.setCornerRadius( 8 );
        shape.setColor(getResources().getColor(R.color.btnBackground));
        replyButton.setBackground(shape);

    }

}
