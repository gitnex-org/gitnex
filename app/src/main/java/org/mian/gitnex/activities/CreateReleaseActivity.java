package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.gitnex.tea4j.models.Branches;
import org.gitnex.tea4j.models.CreateTagOptions;
import org.gitnex.tea4j.models.GitTag;
import org.gitnex.tea4j.models.Releases;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateReleaseBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class CreateReleaseActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    public ImageView closeActivity;
    private EditText releaseTagName;
    private AutoCompleteTextView releaseBranch;
    private EditText releaseTitle;
    private EditText releaseContent;
    private CheckBox releaseType;
    private CheckBox releaseDraft;
    private Button createNewRelease;
    private String selectedBranch;
    private Button createNewTag;

	private String repoOwner;
	private String repoName;

    List<Branches> branchesList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateReleaseBinding activityCreateReleaseBinding = ActivityCreateReleaseBinding.inflate(getLayoutInflater());
	    setContentView(activityCreateReleaseBinding.getRoot());

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        String repoFullName = tinyDB.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        repoOwner = parts[0];
        repoName = parts[1];

        closeActivity = activityCreateReleaseBinding.close;
        releaseTagName = activityCreateReleaseBinding.releaseTagName;
        releaseTitle = activityCreateReleaseBinding.releaseTitle;
        releaseContent = activityCreateReleaseBinding.releaseContent;
        releaseType = activityCreateReleaseBinding.releaseType;
        releaseDraft = activityCreateReleaseBinding.releaseDraft;

	    releaseTitle.requestFocus();
        assert imm != null;
        imm.showSoftInput(releaseTitle, InputMethodManager.SHOW_IMPLICIT);

	    releaseContent.setOnTouchListener((touchView, motionEvent) -> {

		    touchView.getParent().requestDisallowInterceptTouchEvent(true);

		    if ((motionEvent.getAction() & MotionEvent.ACTION_UP) != 0 && (motionEvent.getActionMasked() & MotionEvent.ACTION_UP) != 0) {

			    touchView.getParent().requestDisallowInterceptTouchEvent(false);
		    }
		    return false;
	    });

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        releaseBranch = activityCreateReleaseBinding.releaseBranch;
        getBranches(Authorization.get(ctx), repoOwner, repoName);

        createNewRelease = activityCreateReleaseBinding.createNewRelease;
        createNewTag = activityCreateReleaseBinding.createNewTag;
        disableProcessButton();

        if(!connToInternet) {

            disableProcessButton();
        }
        else {

            createNewRelease.setOnClickListener(createReleaseListener);
        }

        createNewTag.setOnClickListener(v -> createNewTag());

    }

    private void createNewTag() {
    	boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

	    String tagName = releaseTagName.getText().toString();
	    String message = releaseTitle.getText().toString() + "\n\n" + releaseContent.getText().toString();

	    if(!connToInternet) {
		    Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
		    return;
	    }

	    if(tagName.equals("")) {
		    Toasty.error(ctx, getString(R.string.tagNameErrorEmpty));
		    return;
	    }

	    if(selectedBranch == null) {
		    Toasty.error(ctx, getString(R.string.selectBranchError));
		    return;
	    }

	    disableProcessButton();

	    CreateTagOptions createReleaseJson = new CreateTagOptions(message, tagName, selectedBranch);

	    Call<GitTag> call = RetrofitClient
		    .getApiInterface(ctx)
		    .createTag(Authorization.get(ctx), repoOwner, repoName, createReleaseJson);

	    call.enqueue(new Callback<GitTag>() {

		    @Override
		    public void onResponse(@NonNull Call<GitTag> call, @NonNull retrofit2.Response<GitTag> response) {

			    if (response.code() == 201) {
				    tinyDB.putBoolean("updateReleases", true);
				    Toasty.success(ctx, getString(R.string.tagCreated));
				    finish();
			    }
			    else if(response.code() == 401) {
				    enableProcessButton();
				    AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
					    ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
					    ctx.getResources().getString(R.string.cancelButton),
					    ctx.getResources().getString(R.string.navLogout));
			    }
			    else if(response.code() == 403) {
				    enableProcessButton();
				    Toasty.error(ctx, ctx.getString(R.string.authorizeError));
			    }
			    else if(response.code() == 404) {
				    enableProcessButton();
				    Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
			    }
			    else {
				    enableProcessButton();
				    Toasty.error(ctx, ctx.getString(R.string.genericError));
			    }
		    }

		    @Override
		    public void onFailure(@NonNull Call<GitTag> call, @NonNull Throwable t) {
			    Log.e("onFailure", t.toString());
			    enableProcessButton();
		    }
	    });



    }

    private final View.OnClickListener createReleaseListener = v -> processNewRelease();

    private void processNewRelease() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newReleaseTagName = releaseTagName.getText().toString();
        String newReleaseTitle = releaseTitle.getText().toString();
        String newReleaseContent = releaseContent.getText().toString();
	    String checkBranch = selectedBranch;
        boolean newReleaseType = releaseType.isChecked();
        boolean newReleaseDraft = releaseDraft.isChecked();

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

	    if(newReleaseTitle.equals("")) {

		    Toasty.error(ctx, getString(R.string.titleErrorEmpty));
		    return;
	    }

        if(newReleaseTagName.equals("")) {

            Toasty.error(ctx, getString(R.string.tagNameErrorEmpty));
            return;
        }

	    if(checkBranch == null) {

	    	Toasty.error(ctx, getString(R.string.selectBranchError));
		    return;
	    }

        disableProcessButton();
        createNewReleaseFunc(Authorization.get(ctx), repoOwner, repoName, newReleaseTagName, newReleaseTitle, newReleaseContent, selectedBranch, newReleaseType, newReleaseDraft);
    }

    private void createNewReleaseFunc(final String token, String repoOwner, String repoName, String newReleaseTagName, String newReleaseTitle, String newReleaseContent, String selectedBranch, boolean newReleaseType, boolean newReleaseDraft) {

        Releases createReleaseJson = new Releases(newReleaseContent, newReleaseDraft, newReleaseTitle, newReleaseType, newReleaseTagName, selectedBranch);

        Call<Releases> call;

        call = RetrofitClient
                .getApiInterface(ctx)
                .createNewRelease(token, repoOwner, repoName, createReleaseJson);

        call.enqueue(new Callback<Releases>() {

            @Override
            public void onResponse(@NonNull Call<Releases> call, @NonNull retrofit2.Response<Releases> response) {

                if (response.code() == 201) {

                    tinyDB.putBoolean("updateReleases", true);
                    Toasty.success(ctx, getString(R.string.releaseCreatedText));
                    enableProcessButton();
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                     AlertDialogs.authorizationTokenRevokedDialog(ctx, ctx.getResources().getString(R.string.alertDialogTokenRevokedTitle),
                             ctx.getResources().getString(R.string.alertDialogTokenRevokedMessage),
                             ctx.getResources().getString(R.string.cancelButton),
                             ctx.getResources().getString(R.string.navLogout));
                }
                else if(response.code() == 403) {

                    enableProcessButton();
                    Toasty.error(ctx, ctx.getString(R.string.authorizeError));
                }
                else if(response.code() == 404) {

                    enableProcessButton();
                    Toasty.warning(ctx, ctx.getString(R.string.apiNotFound));
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, ctx.getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Releases> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void getBranches(String instanceToken, final String repoOwner, final String repoName) {

        Call<List<Branches>> call = RetrofitClient
                .getApiInterface(ctx)
                .getBranches(instanceToken, repoOwner, repoName);

        call.enqueue(new Callback<List<Branches>>() {

            @Override
            public void onResponse(@NonNull Call<List<Branches>> call, @NonNull retrofit2.Response<List<Branches>> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 200) {

                        List<Branches> branchesList_ = response.body();

                        assert branchesList_ != null;
                        if(branchesList_.size() > 0) {

	                        branchesList.addAll(branchesList_);
                        }

	                    ArrayAdapter<Branches> adapter = new ArrayAdapter<>(CreateReleaseActivity.this,
		                    R.layout.list_spinner_items, branchesList);

                        releaseBranch.setAdapter(adapter);
                        enableProcessButton();

	                    releaseBranch.setOnItemClickListener ((parent, view, position, id) ->

		                    selectedBranch = branchesList.get(position).getName()
	                    );
                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx, getResources().getString(R.string.alertDialogTokenRevokedTitle),
                            getResources().getString(R.string.alertDialogTokenRevokedMessage),
                            getResources().getString(R.string.cancelButton),
                            getResources().getString(R.string.navLogout));
                }

            }

            @Override
            public void onFailure(@NonNull Call<List<Branches>> call, @NonNull Throwable t) {

                Log.e("onFailure", t.toString());
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void disableProcessButton() {
		createNewTag.setEnabled(false);
        createNewRelease.setEnabled(false);
    }

    private void enableProcessButton() {
	    createNewTag.setEnabled(true);
        createNewRelease.setEnabled(true);
    }

}
