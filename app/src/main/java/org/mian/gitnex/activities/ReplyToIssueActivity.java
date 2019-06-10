package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.actions.IssueActions;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.Issues;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public class ReplyToIssueActivity extends AppCompatActivity {

    public ImageView closeActivity;
    private View.OnClickListener onClickListener;

    private EditText newReplyToIssue;
    final Context ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply_to_issue);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());
        TinyDB tinyDb = new TinyDB(getApplicationContext());

        closeActivity = findViewById(R.id.close);
        newReplyToIssue = findViewById(R.id.newReplyToIssue);
        TextView toolbar_title = findViewById(R.id.toolbar_title);

        if(!tinyDb.getString("issueTitle").isEmpty()) {
            toolbar_title.setText(tinyDb.getString("issueTitle"));
        }

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        Button replyButton = findViewById(R.id.replyButton);

        if(getIntent().getStringExtra("commentAction") != null && getIntent().getStringExtra("commentAction").equals("edit")) {

            newReplyToIssue.setText(getIntent().getStringExtra("commentBody"));
            final String commentId = getIntent().getStringExtra("commentId");

            toolbar_title.setText(getResources().getString(R.string.editCommentTitle));
            replyButton.setText(getResources().getString(R.string.editCommentButtonText));

            replyButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    IssueActions.editIssueComment(ctx, Integer.valueOf(commentId), newReplyToIssue.getText().toString());
                }

            });

            return;

        }

        if(!connToInternet) {

            replyButton.setEnabled(false);
            GradientDrawable shape =  new GradientDrawable();
            shape.setCornerRadius( 8 );
            shape.setColor(getResources().getColor(R.color.hintColor));
            replyButton.setBackground(shape);

        } else {

            replyButton.setOnClickListener(replyToIssue);

        }

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

        String newReplyDT = newReplyToIssue.getText().toString();
        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        if(!connToInternet) {

            Toasty.info(getApplicationContext(), getResources().getString(R.string.checkNetConnection));
            return;

        }

        if(newReplyDT.equals("")) {

            Toasty.info(getApplicationContext(), getString(R.string.commentEmptyError));

        }
        else {

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

                if(response.isSuccessful()) {
                    if(response.code() == 201) {

                        Toasty.info(getApplicationContext(), getString(R.string.commentSuccess));
                        tinyDb.putBoolean("commentPosted", true);
                        tinyDb.putBoolean("resumeIssues", true);
                        finish();

                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyNegativeButton),
                            getResources().getString(R.string.alertDialogTokenRevokedCopyPositiveButton));

                }
                else {

                    Toasty.info(getApplicationContext(), getString(R.string.commentError));

                }

            }

            @Override
            public void onFailure(@NonNull Call<Issues> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

}
