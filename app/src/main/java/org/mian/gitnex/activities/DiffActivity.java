package org.mian.gitnex.activities;

import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivityDiffBinding;
import org.mian.gitnex.fragments.DiffFilesFragment;
import org.mian.gitnex.fragments.DiffFragment;

/**
 * @author opyale
 */

public class DiffActivity extends BaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivityDiffBinding binding = ActivityDiffBinding.inflate(getLayoutInflater());

		setContentView(binding.getRoot());

		DiffFilesFragment fragment = DiffFilesFragment.newInstance();

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
