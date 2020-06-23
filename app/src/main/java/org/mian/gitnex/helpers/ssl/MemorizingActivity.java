package org.mian.gitnex.helpers.ssl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import org.mian.gitnex.R;

/**
 * Author Georg Lukas, modified by opyale
 */

public class MemorizingActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		int decisionId = intent.getIntExtra("DECISION_INTENT_ID", MTMDecision.DECISION_INVALID);
		int titleId = intent.getIntExtra("DECISION_TITLE_ID", R.string.mtm_accept_cert);
		String cert = intent.getStringExtra("DECISION_INTENT_CERT");

		AlertDialog.Builder builder = new AlertDialog.Builder(MemorizingActivity.this);
		builder.setTitle(titleId);
		builder.setMessage(cert);

		builder.setPositiveButton(R.string.mtm_decision_always, (dialog, which) -> onSendResult(decisionId, MTMDecision.DECISION_ALWAYS));
		builder.setNeutralButton(R.string.mtm_decision_abort, (dialog, which) -> onSendResult(decisionId, MTMDecision.DECISION_ABORT));
		builder.setOnCancelListener(dialog -> onSendResult(decisionId, MTMDecision.DECISION_ABORT));

		builder.create().show();

	}

	private void onSendResult(int decisionId, int decision) {

		MemorizingTrustManager.interactResult(decisionId, decision);
		finish();

	}

}
