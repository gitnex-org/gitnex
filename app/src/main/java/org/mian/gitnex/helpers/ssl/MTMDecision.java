package org.mian.gitnex.helpers.ssl;

/**
 * @author Georg Lukas, modified by opyale
 */
class MTMDecision {

	static final int DECISION_INVALID = 0;
	static final int DECISION_ABORT = 1;
	static final int DECISION_ALWAYS = 2;

	int state = DECISION_INVALID;
}
