package org.mian.gitnex.models;

import java.util.Date;

/**
 * Author M M Arif
 */

public class CronTasks {

	private String name;
	private String schedule;
	private Date next;
	private Date prev;
	private int exec_times;

	public String getName() {

		return name;
	}

	public String getSchedule() {

		return schedule;
	}

	public Date getNext() {

		return next;
	}

	public Date getPrev() {

		return prev;
	}

	public int getExec_times() {

		return exec_times;
	}

}
