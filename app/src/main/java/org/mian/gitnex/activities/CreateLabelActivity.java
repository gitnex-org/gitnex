package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import org.gitnex.tea4j.v2.models.CreateLabelOption;
import org.gitnex.tea4j.v2.models.EditLabelOption;
import org.gitnex.tea4j.v2.models.Label;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityCreateLabelBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.LabelsViewModel;
import org.mian.gitnex.viewmodels.OrganizationLabelsViewModel;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */

public class CreateLabelActivity extends BaseActivity {

	public static boolean refreshLabels = false;

	private OrganizationLabelsViewModel organizationLabelsViewModel;
	private LabelsViewModel labelsViewModel;
    private View.OnClickListener onClickListener;
    private TextView colorPicker;
    private EditText labelName;
    private Button createLabelButton;

    private RepositoryContext repository;
    private String labelColor = "";
    private String labelColorDefault = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityCreateLabelBinding activityCreateLabelBinding = ActivityCreateLabelBinding.inflate(getLayoutInflater());
	    setContentView(activityCreateLabelBinding.getRoot());
	    labelsViewModel = new ViewModelProvider(this).get(LabelsViewModel.class);
	    organizationLabelsViewModel = new ViewModelProvider(this).get(OrganizationLabelsViewModel.class);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        repository = RepositoryContext.fromIntent(getIntent());

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("delete")) {

            deleteLabel(Integer.parseInt(Objects.requireNonNull(getIntent().getStringExtra("labelId"))));
            finish();
            return;
        }

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        ImageView closeActivity = activityCreateLabelBinding.close;
        colorPicker = activityCreateLabelBinding.colorPicker;
        labelName = activityCreateLabelBinding.labelName;
        createLabelButton = activityCreateLabelBinding.createLabelButton;

        labelName.requestFocus();
        assert imm != null;
        imm.showSoftInput(labelName, InputMethodManager.SHOW_IMPLICIT);

        final ColorPicker cp = new ColorPicker(CreateLabelActivity.this, 235, 113, 33);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);
        colorPicker.setOnClickListener(v -> cp.show());

        cp.setCallback(color -> {

            //Log.i("#Hex no alpha", String.format("#%06X", (0xFFFFFF & color)));
            colorPicker.setBackgroundColor(color);
	        labelColor = String.format("#%06X", (0xFFFFFF & color));
            cp.dismiss();
        });

        if(getIntent().getStringExtra("labelAction") != null && Objects.requireNonNull(getIntent().getStringExtra("labelAction")).equals("edit")) {

            labelName.setText(getIntent().getStringExtra("labelTitle"));
            int labelColor_ = Color.parseColor("#" + getIntent().getStringExtra("labelColor"));
            colorPicker.setBackgroundColor(labelColor_);
	        labelColorDefault = "#" + getIntent().getStringExtra("labelColor");

            TextView toolbar_title = activityCreateLabelBinding.toolbarTitle;
            toolbar_title.setText(getResources().getString(R.string.pageTitleLabelUpdate));
            createLabelButton.setText(getResources().getString(R.string.newUpdateButtonCopy));

            createLabelButton.setOnClickListener(updateLabelListener);
            return;
        }

        if(!connToInternet) {

            createLabelButton.setEnabled(false);
        }
        else {

            createLabelButton.setOnClickListener(createLabelListener);
        }

    }

    private final View.OnClickListener createLabelListener = v -> processCreateLabel();

    private final View.OnClickListener updateLabelListener = v -> processUpdateLabel();

    private void processUpdateLabel() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String updateLabelName = labelName.getText().toString();

        String updateLabelColor;
        if(labelColor.isEmpty()) {

            updateLabelColor = labelColorDefault;
        }
        else {

            updateLabelColor = labelColor;
        }

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(updateLabelName.equals("")) {

            Toasty.error(ctx, getString(R.string.labelEmptyError));
            return;
        }

        if(!AppUtil.checkStrings(updateLabelName)) {

            Toasty.error(ctx, getString(R.string.labelNameError));
            return;
        }

        disableProcessButton();
        patchLabel(repository, updateLabelName, updateLabelColor, Integer.parseInt(
	        Objects.requireNonNull(getIntent().getStringExtra("labelId"))));

    }

    private void processCreateLabel() {

        boolean connToInternet = AppUtil.hasNetworkConnection(appCtx);

        String newLabelName = labelName.getText().toString();
        String newLabelColor;

        if(labelColor.isEmpty()) {

            newLabelColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(ctx, R.color.releasePre)));
        }
        else {

            newLabelColor = labelColor;
        }

        if(!connToInternet) {

            Toasty.error(ctx, getResources().getString(R.string.checkNetConnection));
            return;
        }

        if(newLabelName.equals("")) {

            Toasty.error(ctx, getString(R.string.labelEmptyError));
            return;
        }

        if(!AppUtil.checkStrings(newLabelName)) {

            Toasty.error(ctx, getString(R.string.labelNameError));
            return;
        }

        disableProcessButton();
        createNewLabel(newLabelName, newLabelColor);
    }

    private void createNewLabel(String newLabelName, String newLabelColor) {

        CreateLabelOption createLabelFunc = new CreateLabelOption();
		createLabelFunc.setColor(newLabelColor);
		createLabelFunc.setName(newLabelName);

        Call<Label> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

	    	call = RetrofitClient.getApiInterface(ctx).orgCreateLabel(getIntent().getStringExtra("orgName"), createLabelFunc);
	    }
	    else if(repository != null) {

		    call = RetrofitClient.getApiInterface(ctx).issueCreateLabel(repository.getOwner(), repository.getName(), createLabelFunc);
	    } else {
			return;
	    }

        call.enqueue(new Callback<Label>() {

            @Override
            public void onResponse(@NonNull Call<Label> call, @NonNull retrofit2.Response<Label> response) {

                if(response.code() == 201) {

                    Toasty.success(ctx, getString(R.string.labelCreated));
	                refreshLabels = true;
                    finish();
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx);
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Label> call, @NonNull Throwable t) {

	            labelColor = "";
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void patchLabel(RepositoryContext repository, String updateLabelName, String updateLabelColor, int labelId) {

        EditLabelOption createLabelFunc = new EditLabelOption();
		createLabelFunc.setColor(updateLabelColor);
		createLabelFunc.setName(updateLabelName);

        Call<Label> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

		    call = RetrofitClient.getApiInterface(ctx).orgEditLabel(getIntent().getStringExtra("orgName"), (long) labelId, createLabelFunc);
	    }
	    else {

		    call = RetrofitClient.getApiInterface(ctx).issueEditLabel(repository.getOwner(), repository.getName(), (long) labelId, createLabelFunc);
	    }

        call.enqueue(new Callback<Label>() {

            @Override
            public void onResponse(@NonNull Call<Label> call, @NonNull retrofit2.Response<Label> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 200) {

                        Toasty.success(ctx, getString(R.string.labelUpdated));
	                    refreshLabels = true;
                        finish();
                    }
                }
                else if(response.code() == 401) {

                    enableProcessButton();
                    AlertDialogs.authorizationTokenRevokedDialog(ctx);
                }
                else {

                    enableProcessButton();
                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Label> call, @NonNull Throwable t) {

	            labelColor = "";
	            labelColorDefault = "";
                Log.e("onFailure", t.toString());
                enableProcessButton();
            }
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

    private void deleteLabel(int labelId) {

        Call<Void> call;

	    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

		    call = RetrofitClient.getApiInterface(ctx).orgDeleteLabel(getIntent().getStringExtra("orgName"), (long) labelId);
	    }
	    else {

		    call = RetrofitClient.getApiInterface(ctx).issueDeleteLabel(repository.getOwner(), repository.getName(), (long) labelId);
	    }

        call.enqueue(new Callback<Void>() {

            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull retrofit2.Response<Void> response) {

                if(response.isSuccessful()) {

                    if(response.code() == 204) {

                        Toasty.success(ctx, getString(R.string.labelDeleteText));
	                    if(getIntent().getStringExtra("type") != null && Objects.requireNonNull(getIntent().getStringExtra("type")).equals("org")) {

		                    organizationLabelsViewModel.loadOrgLabelsList(getIntent().getStringExtra("orgName"), ctx, null, null);
	                    }
	                    else {

		                    labelsViewModel.loadLabelsList(repository.getOwner(), repository.getName(), ctx);
	                    }
                    }
                }
                else if(response.code() == 401) {

                    AlertDialogs.authorizationTokenRevokedDialog(ctx);
                }
                else {

                    Toasty.error(ctx, getString(R.string.genericError));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    private void disableProcessButton() {

        createLabelButton.setEnabled(false);
    }

    private void enableProcessButton() {

        createLabelButton.setEnabled(true);
    }

	@Override
	public void onResume() {
		super.onResume();
		if(repository == null) return;
		repository.checkAccountSwitch(this);
	}

}
