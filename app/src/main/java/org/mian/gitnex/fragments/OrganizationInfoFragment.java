package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentOrganizationInfoBinding;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.helpers.RoundedTransformation;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class OrganizationInfoFragment extends Fragment {

	FragmentOrganizationInfoBinding fragmentOrganizationInfoBinding;
	private static final String orgNameF = "param1";
	private Context ctx;
	private String orgName;

	public OrganizationInfoFragment() {}

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
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentOrganizationInfoBinding =
				FragmentOrganizationInfoBinding.inflate(inflater, container, false);

		ctx = getContext();

		getOrgInfo(orgName);

		return fragmentOrganizationInfoBinding.getRoot();
	}

	private void getOrgInfo(final String owner) {

		Call<Organization> call = RetrofitClient.getApiInterface(getContext()).orgGet(owner);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<Organization> call,
							@NonNull retrofit2.Response<Organization> response) {

						Organization orgInfo = response.body();

						if (response.code() == 200) {

							fragmentOrganizationInfoBinding.orgInfoLayout.setVisibility(
									View.VISIBLE);

							assert orgInfo != null;

							PicassoService.getInstance(ctx)
									.get()
									.load(orgInfo.getAvatarUrl())
									.placeholder(R.drawable.loader_animated)
									.transform(new RoundedTransformation(8, 0))
									.resize(230, 230)
									.centerCrop()
									.into(fragmentOrganizationInfoBinding.orgAvatar);

							if (orgInfo.getFullName() != null && !orgInfo.getFullName().isEmpty()) {
								fragmentOrganizationInfoBinding.orgNameInfo.setText(
										getString(
												R.string.organizationFullname,
												orgInfo.getFullName(),
												orgName));
							} else {
								fragmentOrganizationInfoBinding.orgNameInfo.setText(orgName);
							}

							if (!orgInfo.getDescription().isEmpty()) {
								Markdown.render(
										ctx,
										orgInfo.getDescription(),
										fragmentOrganizationInfoBinding.orgDescInfo);
							} else {
								fragmentOrganizationInfoBinding.orgDescInfo.setText(
										getString(R.string.noDataDescription));
							}

							if (!orgInfo.getWebsite().isEmpty()) {
								fragmentOrganizationInfoBinding.orgWebsiteInfo.setText(
										orgInfo.getWebsite());
							} else {
								fragmentOrganizationInfoBinding.orgWebsiteInfo.setText(
										getString(R.string.noDataWebsite));
							}

							if (!orgInfo.getLocation().isEmpty()) {
								fragmentOrganizationInfoBinding.orgLocationInfo.setText(
										orgInfo.getLocation());
							} else {
								fragmentOrganizationInfoBinding.orgLocationInfo.setText(
										getString(R.string.noDataLocation));
							}

							fragmentOrganizationInfoBinding.progressBar.setVisibility(View.GONE);

						} else if (response.code() == 404) {

							fragmentOrganizationInfoBinding.progressBar.setVisibility(View.GONE);

						} else {
							Log.e("onFailure", String.valueOf(response.code()));
						}
					}

					@Override
					public void onFailure(@NonNull Call<Organization> call, @NonNull Throwable t) {}
				});
	}
}
