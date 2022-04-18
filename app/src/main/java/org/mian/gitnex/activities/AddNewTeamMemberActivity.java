package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.v2.models.InlineResponse2001;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.adapters.UserSearchForTeamMemberAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityAddNewTeamMemberBinding;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class AddNewTeamMemberActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private TextView addNewTeamMember;
	private TextView noData;
	private ProgressBar mProgressBar;

	private RecyclerView mRecyclerView;
	private List<User> dataList;
	private UserSearchForTeamMemberAdapter adapter;

	private long teamId;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityAddNewTeamMemberBinding activityAddNewTeamMemberBinding = ActivityAddNewTeamMemberBinding.inflate(getLayoutInflater());
		setContentView(activityAddNewTeamMemberBinding.getRoot());

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		ImageView closeActivity = activityAddNewTeamMemberBinding.close;
		addNewTeamMember = activityAddNewTeamMemberBinding.addNewTeamMember;
		mRecyclerView = activityAddNewTeamMemberBinding.recyclerViewUserSearch;
		mProgressBar = activityAddNewTeamMemberBinding.progressBar;
		noData = activityAddNewTeamMemberBinding.noData;

		addNewTeamMember.requestFocus();
		assert imm != null;
		imm.showSoftInput(addNewTeamMember, InputMethodManager.SHOW_IMPLICIT);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		teamId = getIntent().getLongExtra("teamId", 0);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),	DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		dataList = new ArrayList<>();

		addNewTeamMember.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if(!addNewTeamMember.getText().toString().equals("") && addNewTeamMember.getText().toString().length() > 1) {

					adapter = new UserSearchForTeamMemberAdapter(dataList, ctx, Math.toIntExact(teamId), getIntent().getStringExtra("orgName"));
					loadUserSearchList(addNewTeamMember.getText().toString());
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

		});

	}

	public void loadUserSearchList(String searchKeyword) {

		Call<InlineResponse2001> call = RetrofitClient.getApiInterface(ctx).userSearch(searchKeyword, null, 1, 10);

		mProgressBar.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<>() {

			@Override
			public void onResponse(@NonNull Call<InlineResponse2001> call, @NonNull Response<InlineResponse2001> response) {

				if(response.isSuccessful()) {

					assert response.body() != null;
					if(response.body().getData().size() > 0) {

						dataList.clear();
						dataList.addAll(response.body().getData());
						mRecyclerView.setAdapter(adapter);
						noData.setVisibility(View.GONE);
					}
					else {

						noData.setVisibility(View.VISIBLE);
					}

					mProgressBar.setVisibility(View.GONE);
				}

			}

			@Override
			public void onFailure(@NonNull Call<InlineResponse2001> call, @NonNull Throwable t) {

				Log.e("onFailure", t.toString());
			}

		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
