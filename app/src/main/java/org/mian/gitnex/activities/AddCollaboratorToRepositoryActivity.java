package org.mian.gitnex.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserSearchAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserSearch;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import java.util.List;

/**
 * Author M M Arif
 */

public class AddCollaboratorToRepositoryActivity extends AppCompatActivity {

    private View.OnClickListener onClickListener;
    final Context ctx = this;
    private TextView addCollaboratorSearch;
    private TextView noData;
    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_collaborator_to_repository);

        boolean connToInternet = AppUtil.haveNetworkConnection(getApplicationContext());

        TinyDB tinyDb = new TinyDB(getApplicationContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        ImageView closeActivity = findViewById(R.id.close);
        addCollaboratorSearch = findViewById(R.id.addCollaboratorSearch);
        mRecyclerView = findViewById(R.id.recyclerViewUserSearch);
        mProgressBar = findViewById(R.id.progress_bar);
        noData = findViewById(R.id.noData);

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        addCollaboratorSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    if(!addCollaboratorSearch.getText().toString().equals("")) {
                        loadUserSearchList(instanceUrl, instanceToken, addCollaboratorSearch.getText().toString(), getApplicationContext(), loginUid);
                    }
                }
                return false;
            }
        });

    }

    public void loadUserSearchList(String instanceUrl, String token, String searchKeyword, final Context context, String loginUid) {

        Call<UserSearch> call = RetrofitClient
                .getInstance(instanceUrl)
                .getApiInterface()
                .getUserBySearch(Authorization.returnAuthentication(getApplicationContext(), loginUid, token), searchKeyword, 10);

        call.enqueue(new Callback<UserSearch>() {

            @Override
            public void onResponse(@NonNull Call<UserSearch> call, @NonNull Response<UserSearch> response) {

                if (response.isSuccessful()) {
                    assert response.body() != null;
                    getUsersList(response.body().getData(), context);
                } else {
                    Log.i("onResponse", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<UserSearch> call, @NonNull Throwable t) {
                Log.i("onFailure", t.getMessage());
            }

        });
    }

    private void getUsersList(List<UserInfo> dataList, Context context) {

        UserSearchAdapter adapter = new UserSearchAdapter(dataList, context);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar.setVisibility(View.VISIBLE);

        if(adapter.getItemCount() > 0) {
            mRecyclerView.setAdapter(adapter);
            noData.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
        }
        else {
            noData.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
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


}
