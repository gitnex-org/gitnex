package org.mian.gitnex.helpers.ssl;

/**
 * Author Georg Lukas, modified by anonTree1417
 */

class MTMDecision {

	final static int DECISION_INVALID	= 0;
	final static int DECISION_ABORT		= 1;
	final static int DECISION_ALWAYS 	= 3;

	int state = DECISION_INVALID;

}
