/* MemorizingTrustManager - a TrustManager which asks the user about invalid
 *  certificates and memorizes their decision.
 *
 * Copyright (c) 2010 Georg Lukas <georg@op-co.de>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.mian.gitnex.ssl;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import org.mian.gitnex.R;

public class MemorizingActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		int decisionId = intent.getIntExtra(MemorizingTrustManager.DECISION_INTENT_ID, MTMDecision.DECISION_INVALID);
		int titleId = intent.getIntExtra(MemorizingTrustManager.DECISION_TITLE_ID, R.string.mtm_accept_cert);
		String cert = intent.getStringExtra(MemorizingTrustManager.DECISION_INTENT_CERT);

		AlertDialog.Builder builder = new AlertDialog.Builder(MemorizingActivity.this);
		builder.setTitle(titleId);
		builder.setMessage(cert);

		builder.setPositiveButton(R.string.mtm_decision_always, (dialog, which) -> onSendResult(decisionId, MTMDecision.DECISION_ALWAYS));
		builder.setNeutralButton(R.string.mtm_decision_once, (dialog, which) -> onSendResult(decisionId, MTMDecision.DECISION_ONCE));
		builder.setNegativeButton(R.string.mtm_decision_abort, (dialog, which) -> onSendResult(decisionId, MTMDecision.DECISION_ABORT));
		builder.setOnCancelListener(dialog -> onSendResult(decisionId, MTMDecision.DECISION_ABORT));

		builder.create().show();
	}

	private void onSendResult(int decisionId, int decision) {
		MemorizingTrustManager.interactResult(decisionId, decision);
		finish();
	}
}