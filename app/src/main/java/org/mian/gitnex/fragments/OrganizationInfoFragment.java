package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import org.gitnex.tea4j.v2.models.Organization;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.FragmentOrganizationInfoBinding;
import org.mian.gitnex.databinding.ItemProfileInfoBinding;
import org.mian.gitnex.helpers.Markdown;
import org.mian.gitnex.viewmodels.OrganizationsViewModel;

/**
 * @author mmarif
 */
public class OrganizationInfoFragment extends Fragment {

	private FragmentOrganizationInfoBinding binding;
	private OrganizationsViewModel viewModel;
	private String orgName;

	public static OrganizationInfoFragment newInstance(String name) {
		OrganizationInfoFragment fragment = new OrganizationInfoFragment();
		Bundle args = new Bundle();
		args.putString("org_name", name);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) orgName = getArguments().getString("org_name");
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentOrganizationInfoBinding.inflate(inflater, container, false);
		viewModel = new ViewModelProvider(this).get(OrganizationsViewModel.class);

		observeViewModel();
		viewModel.fetchOrgDetails(requireContext(), orgName);

		return binding.getRoot();
	}

	private void observeViewModel() {
		viewModel.getSingleOrg().observe(getViewLifecycleOwner(), this::populateUi);

		viewModel
				.getIsLoading()
				.observe(
						getViewLifecycleOwner(),
						loading -> {
							if (loading) {
								binding.expressiveLoader.setVisibility(View.VISIBLE);
								binding.orgInfoLayout.setVisibility(View.GONE);
							}
						});
	}

	private void populateUi(Organization org) {
		binding.expressiveLoader.setVisibility(View.GONE);
		binding.orgInfoLayout.setVisibility(View.VISIBLE);

		binding.orgNameInfo.setText(
				org.getFullName().isEmpty() ? org.getUsername() : org.getFullName());
		binding.orgUsername.setText(getString(R.string.usernameWithAt, org.getUsername()));

		Glide.with(this)
				.load(org.getAvatarUrl())
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.placeholder(R.drawable.loader_animated)
				.into(binding.orgAvatar);

		if (org.getDescription() != null && !org.getDescription().trim().isEmpty()) {
			Markdown.render(requireContext(), org.getDescription(), binding.orgDescInfo);
		}

		setupInfoItem(binding.layoutEmail, R.drawable.ic_email, org.getEmail(), false);
		setupInfoItem(binding.layoutWebsite, R.drawable.ic_link, org.getWebsite(), false);
		setupInfoItem(binding.layoutLocation, R.drawable.ic_location, org.getLocation(), false);
	}

	private void setupInfoItem(
			ItemProfileInfoBinding item, int iconRes, String value, boolean isPrivate) {
		item.infoIcon.setImageResource(iconRes);
		boolean isEmpty = (value == null || value.trim().isEmpty());
		item.infoText.setText(isEmpty ? "—" : value);
		item.getRoot().setAlpha(isEmpty ? 0.6f : 1.0f);
	}
}
