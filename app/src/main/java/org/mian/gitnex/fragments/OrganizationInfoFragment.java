package org.mian.gitnex.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.Organization;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class OrganizationInfoFragment extends Fragment {

    private Context ctx = getContext();
    private ProgressBar mProgressBar;
    private static String orgNameF = "param1";

    private String orgName;
    private ImageView orgAvatar;
    private TextView orgDescInfo;
    private TextView orgWebsiteInfo;
    private TextView orgLocationInfo;
    private LinearLayout orgInfoLayout;

    private RepoInfoFragment.OnFragmentInteractionListener mListener;

    public OrganizationInfoFragment() {
    }

    public static OrganizationInfoFragment newInstance(String param1) {
        OrganizationInfoFragment fragment = new OrganizationInfoFragment();
        Bundle args = new Bundle();
        args.putString(orgNameF, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orgName = getArguments().getString(orgNameF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_organization_info, container, false);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        mProgressBar = v.findViewById(R.id.progress_bar);
        orgAvatar = v.findViewById(R.id.orgAvatar);
        TextView orgNameInfo = v.findViewById(R.id.orgNameInfo);
        orgDescInfo = v.findViewById(R.id.orgDescInfo);
        orgWebsiteInfo = v.findViewById(R.id.orgWebsiteInfo);
        orgLocationInfo = v.findViewById(R.id.orgLocationInfo);
	    orgInfoLayout = v.findViewById(R.id.orgInfoLayout);

        orgNameInfo.setText(orgName);

        getOrgInfo(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), orgName);

        return v;

    }

    private void getOrgInfo(String instanceUrl, String token, final String owner) {

        Call<Organization> call = RetrofitClient
                .getInstance(instanceUrl, getContext())
                .getApiInterface()
                .getOrganization(token, owner);

        call.enqueue(new Callback<Organization>() {

            @Override
            public void onResponse(@NonNull Call<Organization> call, @NonNull retrofit2.Response<Organization> response) {

                Organization orgInfo = response.body();

                if (response.code() == 200) {

                    orgInfoLayout.setVisibility(View.VISIBLE);

                    assert orgInfo != null;

                    PicassoService.getInstance(ctx).get()
	                    .load(orgInfo.getAvatar_url())
	                    .placeholder(R.drawable.loader_animated)
	                    .transform(new RoundedTransformation(8, 0))
	                    .resize(230, 230)
	                    .centerCrop().into(orgAvatar);

	                if(!orgInfo.getDescription().isEmpty()) {
		                orgDescInfo.setText(orgInfo.getDescription());
	                }
	                else {
		                orgDescInfo.setText(getString(R.string.noDataDescription));
	                }

                    if(!orgInfo.getWebsite().isEmpty()) {
	                    orgWebsiteInfo.setText(orgInfo.getWebsite());
                    }
                    else {
	                    orgWebsiteInfo.setText(getString(R.string.noDataWebsite));
                    }

	                if(!orgInfo.getLocation().isEmpty()) {
		                orgLocationInfo.setText(orgInfo.getLocation());
	                }
	                else {
		                orgLocationInfo.setText(getString(R.string.noDataLocation));
	                }

                    mProgressBar.setVisibility(View.GONE);

                }
                else if(response.code() == 404) {

	                mProgressBar.setVisibility(View.GONE);

                }
                else {
                    Log.e("onFailure", String.valueOf(response.code()));
                }

            }

            @Override
            public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {
                Log.e("onFailure", t.toString());
            }
        });

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
