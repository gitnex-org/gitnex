package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.android.material.tabs.TabLayoutMediator;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityAccountSettingsBinding;
import org.mian.gitnex.fragments.AccountSettingsEmailsFragment;
import org.mian.gitnex.fragments.SSHKeysFragment;
import org.mian.gitnex.helpers.AppDatabaseSettings;
import org.mian.gitnex.helpers.ViewPager2Transformers;

/**
 * @author mmarif
 */
public class AccountSettingsActivity extends BaseActivity {

	private ActivityAccountSettingsBinding binding;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		setupViewPager();
	}

	private void setupViewPager() {
		ViewPagerAdapter adapter = new ViewPagerAdapter(this);
		binding.accountSettingsContainer.setAdapter(adapter);
		binding.accountSettingsContainer.setOffscreenPageLimit(1);

		ViewPager2Transformers.returnSelectedTransformer(
				binding.accountSettingsContainer,
				Integer.parseInt(
						AppDatabaseSettings.getSettingsValue(
								this, AppDatabaseSettings.APP_TABS_ANIMATION_KEY)));

		String[] tabTitles = {getString(R.string.accountEmails), getString(R.string.sshKeys)};

		new TabLayoutMediator(
						binding.tabs,
						binding.accountSettingsContainer,
						(tab, position) -> tab.setText(tabTitles[position]))
				.attach();
	}

	private static class ViewPagerAdapter extends FragmentStateAdapter {

		public ViewPagerAdapter(@NonNull BaseActivity activity) {
			super(activity);
		}

		@NonNull @Override
		public Fragment createFragment(int position) {
			return switch (position) {
				case 0 -> new AccountSettingsEmailsFragment();
				case 1 -> new SSHKeysFragment();
				default -> throw new IllegalStateException("Unexpected position " + position);
			};
		}

		@Override
		public int getItemCount() {
			return 2;
		}
	}
}
