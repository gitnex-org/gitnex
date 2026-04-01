package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.button.MaterialButton;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityAccountSettingsBinding;
import org.mian.gitnex.fragments.AccountSettingsEmailsFragment;
import org.mian.gitnex.fragments.AccountSettingsSSHKeysFragment;
import org.mian.gitnex.helpers.UIHelper;

/**
 * @author mmarif
 */
public class AccountSettingsActivity extends BaseActivity {

	private ActivityAccountSettingsBinding binding;
	private final Fragment emailFrag = new AccountSettingsEmailsFragment();
	private final Fragment sshFrag = new AccountSettingsSSHKeysFragment();
	private Fragment activeFragment = emailFrag;
	private final FragmentManager fm = getSupportFragmentManager();
	private View detachedDivider;
	private View detachedAddBtn;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		UIHelper.applyEdgeToEdge(this, binding.dockedToolbar, null, null, null);

		detachedDivider = binding.dockDivider;
		detachedAddBtn = binding.btnDockAdd;

		setupFragments();
		setupDockListeners();

		updateDockUI(R.id.btn_nav_emails);
	}

	private void setupFragments() {
		fm.beginTransaction()
				.add(R.id.account_settings_content, sshFrag, "ssh")
				.hide(sshFrag)
				.add(R.id.account_settings_content, emailFrag, "email")
				.show(emailFrag)
				.commitNow();
	}

	private void setupDockListeners() {
		prepareNavButton(binding.btnNavEmails);
		prepareNavButton(binding.btnNavSshKeys);
		prepareNavButton(binding.btnBack);

		binding.btnBack.setOnClickListener(v -> finish());

		binding.btnNavEmails.setOnClickListener(v -> switchTab(emailFrag, R.id.btn_nav_emails));
		binding.btnNavSshKeys.setOnClickListener(v -> switchTab(sshFrag, R.id.btn_nav_ssh_keys));

		binding.btnDockAdd.setOnClickListener(
				v -> {
					if (activeFragment instanceof AccountSettingsEmailsFragment ef) {
						ef.showAddEmailDialog();
					} else if (activeFragment instanceof AccountSettingsSSHKeysFragment sf) {
						sf.showNewSSHKeyDialog();
					}
				});
	}

	private void prepareNavButton(MaterialButton btn) {
		btn.setBackgroundResource(R.drawable.nav_pill_background);
		btn.setBackgroundTintList(null);
		btn.getBackground().setAlpha(0);
	}

	private void activatePill(MaterialButton btn) {
		btn.setSelected(true);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(255);
	}

	private void resetPill(MaterialButton btn) {
		btn.setSelected(false);
		if (btn.getBackground() != null) btn.getBackground().setAlpha(0);
	}

	private void switchTab(Fragment target, int btnId) {
		if (activeFragment == target) return;

		fm.beginTransaction()
				.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
				.hide(activeFragment)
				.show(target)
				.commit();

		activeFragment = target;
		updateDockUI(btnId);
	}

	private void updateDockUI(int activeBtnId) {
		resetPill(binding.btnNavEmails);
		resetPill(binding.btnNavSshKeys);

		if (activeBtnId == R.id.btn_nav_emails) {
			activatePill(binding.btnNavEmails);
		} else {
			activatePill(binding.btnNavSshKeys);
		}

		updateContextualDockActions();
	}

	private void updateContextualDockActions() {
		ViewGroup parent = binding.dockedToolbarChild;

		parent.removeView(detachedDivider);
		parent.removeView(detachedAddBtn);

		LinearLayout.LayoutParams dividerParams =
				(LinearLayout.LayoutParams) detachedDivider.getLayoutParams();

		if (activeFragment instanceof AccountSettingsSSHKeysFragment) {
			dividerParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dimen16dp));
		} else {
			dividerParams.setMarginStart(getResources().getDimensionPixelSize(R.dimen.dimen4dp));
		}
		detachedDivider.setLayoutParams(dividerParams);

		parent.addView(detachedDivider);
		parent.addView(detachedAddBtn);

		detachedDivider.setVisibility(View.VISIBLE);
		detachedAddBtn.setVisibility(View.VISIBLE);

		binding.dockedToolbar.requestLayout();
	}
}
