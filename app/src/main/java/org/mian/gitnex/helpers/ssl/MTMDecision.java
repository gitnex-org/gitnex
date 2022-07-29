package org.mian.gitnex.helpers.ssl;

/**
 * @author Georg Lukas, modified by opyale
 */

class MTMDecision {

	final static int DECISION_INVALID	= 0;
	final static int DECISION_ABORT		= 1;
	final static int DECISION_ALWAYS 	= 2;

	int state = DECISION_INVALID;

}
