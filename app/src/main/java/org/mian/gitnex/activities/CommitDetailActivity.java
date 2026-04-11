package org.mian.gitnex.activities;

import android.os.Bundle;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityCommitDetailsBinding;
import org.mian.gitnex.fragments.CommitDetailFragment;

/**
 * @author qwerty287
 * @author mmarif
 */
public class CommitDetailActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActivityCommitDetailsBinding binding =
				ActivityCommitDetailsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		if (savedInstanceState == null) {
			getSupportFragmentManager()
					.beginTransaction()
					.replace(R.id.fragment_container, CommitDetailFragment.newInstance())
					.commit();
		}
	}
}
