package org.mian.gitnex.fragments.profile;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.Locale;
import org.gitnex.tea4j.v2.models.User;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.PicassoService;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentProfileDetailBinding;
import org.mian.gitnex.helpers.AlertDialogs;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.ClickListener;
import org.mian.gitnex.helpers.RoundedTransformation;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * @author M M Arif
 */
public class DetailFragment extends Fragment {

	private static final String usernameBundle = "";
	Locale locale;
	TinyDB tinyDb;
	private Context context;
	private FragmentProfileDetailBinding binding;
	private String username;

	public DetailFragment() {}

	public static DetailFragment newInstance(String username) {
		DetailFragment fragment = new DetailFragment();
		Bundle args = new Bundle();
		args.putString(usernameBundle, username);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			username = getArguments().getString(usernameBundle);
		}
	}

	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentProfileDetailBinding.inflate(inflater, container, false);
		context = getContext();
		tinyDb = TinyDB.getInstance(context);
		locale = getResources().getConfiguration().locale;

		getProfileDetail(username);

		return binding.getRoot();
	}

	public void getProfileDetail(String username) {

		Call<User> call = RetrofitClient.getApiInterface(context).userGet(username);

		call.enqueue(
				new Callback<>() {

					@Override
					public void onResponse(
							@NonNull Call<User> call, @NonNull retrofit2.Response<User> response) {

						if (response.isSuccessful() && response.body() != null) {

							switch (response.code()) {
								case 200:
									String username =
											!response.body().getFullName().isEmpty()
													? response.body().getFullName()
													: response.body().getLogin();
									String email =
											!response.body().getEmail().isEmpty()
													? response.body().getEmail()
													: "";

									int imgRadius = AppUtil.getPixelsFromDensity(context, 3);

									binding.userFullName.setText(username);
									binding.userLogin.setText(
											getString(
													R.string.usernameWithAt,
													response.body().getLogin()));
									binding.userEmail.setText(email);

									binding.userFollowersCount.setText(
											String.valueOf(response.body().getFollowersCount()));
									binding.userFollowingCount.setText(
											String.valueOf(response.body().getFollowingCount()));
									binding.userStarredReposCount.setText(
											String.valueOf(response.body().getStarredReposCount()));

									String[] userLanguageCodes =
											response.body().getLanguage().split("-");

									if (userLanguageCodes.length >= 2) {
										Locale locale =
												new Locale(
														userLanguageCodes[0], userLanguageCodes[1]);
										binding.userLang.setText(locale.getDisplayLanguage());
									} else {
										binding.userLang.setText(locale.getDisplayLanguage());
									}

									PicassoService.getInstance(context)
											.get()
											.load(response.body().getAvatarUrl())
											.transform(new RoundedTransformation(imgRadius, 0))
											.placeholder(R.drawable.loader_animated)
											.resize(120, 120)
											.centerCrop()
											.into(binding.userAvatar);

									binding.userJoinedOn.setText(
											TimeHelper.formatTime(
													response.body().getCreated(), locale));
									binding.userJoinedOn.setOnClickListener(
											new ClickListener(
													TimeHelper.customDateFormatForToastDateFormat(
															response.body().getCreated()),
													context));
									break;

								case 401:
									AlertDialogs.authorizationTokenRevokedDialog(context);
									break;

								case 403:
									Toasty.error(
											context, context.getString(R.string.authorizeError));
									break;

								default:
									Toasty.error(context, getString(R.string.genericError));
									break;
							}
						}
						binding.progressBar.setVisibility(View.GONE);
					}

					@Override
					public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
						Toasty.error(
								context, context.getResources().getString(R.string.genericError));
					}
				});
	}
}
