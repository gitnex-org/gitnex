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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserSearchForTeamMemberAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityAddNewTeamMemberBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.models.UserSearch;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
	private List<UserInfo> dataList;
	private UserSearchForTeamMemberAdapter adapter;

	private String teamId;

	@Override
	protected int getLayoutResourceId() {
		return R.layout.activity_add_new_team_member;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityAddNewTeamMemberBinding activityAddNewTeamMemberBinding = ActivityAddNewTeamMemberBinding.inflate(getLayoutInflater());

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

		if(getIntent().getStringExtra("teamId") != null && !Objects.requireNonNull(getIntent().getStringExtra("teamId")).equals("")) {

			teamId = getIntent().getStringExtra("teamId");
		}
		else {

			teamId = "0";
		}

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),	DividerItemDecoration.VERTICAL);
		mRecyclerView.addItemDecoration(dividerItemDecoration);

		dataList = new ArrayList<>();

		addNewTeamMember.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

				if(!addNewTeamMember.getText().toString().equals("") && addNewTeamMember.getText().toString().length() > 1) {

					adapter = new UserSearchForTeamMemberAdapter(dataList, ctx, Integer.parseInt(teamId));
					loadUserSearchList(addNewTeamMember.getText().toString(), teamId);
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

	public void loadUserSearchList(String searchKeyword, String teamId) {

		Call<UserSearch> call = RetrofitClient.getApiInterface(ctx).getUserBySearch(Authorization.get(ctx), searchKeyword, 10);

		mProgressBar.setVisibility(View.VISIBLE);

		call.enqueue(new Callback<UserSearch>() {

			@Override
			public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {

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
			public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {
				Log.e("onFailure", t.toString());
			}

		});
	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
