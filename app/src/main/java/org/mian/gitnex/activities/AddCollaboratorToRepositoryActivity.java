package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.gitnex.tea4j.models.UserInfo;
import org.gitnex.tea4j.models.UserSearch;
import org.mian.gitnex.adapters.UserSearchAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityAddCollaboratorToRepositoryBinding;
import org.mian.gitnex.helpers.Authorization;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author M M Arif
 */

public class AddCollaboratorToRepositoryActivity extends BaseActivity {

    private View.OnClickListener onClickListener;
    private TextView addCollaboratorSearch;
    private TextView noData;
    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityAddCollaboratorToRepositoryBinding activityAddCollaboratorToRepositoryBinding = ActivityAddCollaboratorToRepositoryBinding.inflate(getLayoutInflater());
		setContentView(activityAddCollaboratorToRepositoryBinding.getRoot());

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        ImageView closeActivity = activityAddCollaboratorToRepositoryBinding.close;
        addCollaboratorSearch = activityAddCollaboratorToRepositoryBinding.addCollaboratorSearch;
        mRecyclerView = activityAddCollaboratorToRepositoryBinding.recyclerViewUserSearch;
        mProgressBar = activityAddCollaboratorToRepositoryBinding.progressBar;
        noData = activityAddCollaboratorToRepositoryBinding.noData;

        addCollaboratorSearch.requestFocus();
        assert imm != null;
        imm.showSoftInput(addCollaboratorSearch, InputMethodManager.SHOW_IMPLICIT);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        addCollaboratorSearch.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                if(!addCollaboratorSearch.getText().toString().equals("")) {

	                mProgressBar.setVisibility(View.VISIBLE);
                    loadUserSearchList(addCollaboratorSearch.getText().toString());
                }
            }

            return false;

        });

    }

    public void loadUserSearchList(String searchKeyword) {

        Call<UserSearch> call = RetrofitClient
                .getApiInterface(appCtx)
                .getUserBySearch(Authorization.get(ctx), searchKeyword, 10, 1);

        call.enqueue(new Callback<UserSearch>() {

            @Override
            public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {

	            mProgressBar.setVisibility(View.GONE);

                if (response.code() == 200) {

                    assert response.body() != null;
                    getUsersList(response.body().getData(), ctx);
                }
                else {

                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {
                Log.i("onFailure", t.toString());
            }

        });
    }

    private void getUsersList(List<UserInfo> dataList, Context context) {

        UserSearchAdapter adapter = new UserSearchAdapter(dataList, context);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar.setVisibility(View.VISIBLE);

        if(adapter.getItemCount() > 0) {

            mRecyclerView.setAdapter(adapter);
            noData.setVisibility(View.GONE);
        }
        else {

            noData.setVisibility(View.VISIBLE);
        }

	    mProgressBar.setVisibility(View.GONE);
    }

    private void initCloseListener() {
        onClickListener = view -> finish();
    }

}
