package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityCommitDetailsBinding;
import org.mian.gitnex.fragments.CommitDetailFragment;
import org.mian.gitnex.fragments.DiffFragment;

/**
 * @author qwerty287
 */

public class CommitDetailActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityCommitDetailsBinding binding = ActivityCommitDetailsBinding.inflate(getLayoutInflater());

		setContentView(binding.getRoot());

		CommitDetailFragment fragment = CommitDetailFragment.newInstance();

		getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {

			@Override
			public void handleOnBackPressed() {
				if(getSupportFragmentManager().findFragmentById(R.id.fragment_container) instanceof DiffFragment) {
					getSupportFragmentManager()
						.beginTransaction()
						.replace(R.id.fragment_container, fragment)
						.commit();
				} else {
					finish();
				}
			}
		});

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.fragment_container, fragment)
			.commit();

	}

}
