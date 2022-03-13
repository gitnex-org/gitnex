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
import org.gitnex.tea4j.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationInfoBinding;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class OrganizationInfoFragment extends Fragment {

    private Context ctx;
    private ProgressBar mProgressBar;
    private static String orgNameF = "param1";

    private String orgName;
    private ImageView orgAvatar;
    private TextView orgDescInfo;
    private TextView orgWebsiteInfo;
    private TextView orgLocationInfo;
    private LinearLayout orgInfoLayout;

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

        FragmentOrganizationInfoBinding fragmentOrganizationInfoBinding = FragmentOrganizationInfoBinding.inflate(inflater, container, false);

	    ctx = getContext();

        mProgressBar = fragmentOrganizationInfoBinding.progressBar;
        orgAvatar = fragmentOrganizationInfoBinding.orgAvatar;
        TextView orgNameInfo = fragmentOrganizationInfoBinding.orgNameInfo;
        orgDescInfo = fragmentOrganizationInfoBinding.orgDescInfo;
        orgWebsiteInfo = fragmentOrganizationInfoBinding.orgWebsiteInfo;
        orgLocationInfo = fragmentOrganizationInfoBinding.orgLocationInfo;
	    orgInfoLayout = fragmentOrganizationInfoBinding.orgInfoLayout;

        orgNameInfo.setText(orgName);

        getOrgInfo(((BaseActivity) requireActivity()).getAccount().getAuthorization(), orgName);

        return fragmentOrganizationInfoBinding.getRoot();

    }

    private void getOrgInfo(String token, final String owner) {

        Call<Organization> call = RetrofitClient
                .getApiInterface(getContext())
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
		                Markdown.render(ctx, orgInfo.getDescription(), orgDescInfo);
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
}
