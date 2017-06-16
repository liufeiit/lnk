package io.lnk.demo.test.thread;

import java.util.HashMap;
import java.util.Map;

public class ThreadMetrics {
	public static Map<String, ThreadMetrics> threadMetrics = new HashMap<String, ThreadMetrics>();
	private long startTime;
	private long endTime;
	private long sucCount[];
	private long failCount[];
	private long lost[];
	private long totalCount[];
	private long fallback[];
	public long[] getFallback() {
		return fallback;
	}

	public void setFallback(long[] fallback) {
		this.fallback = fallback;
	}

	public static Map<String, ThreadMetrics> getThreadMetrics() {
		return threadMetrics;
	}

	public static void setThreadMetrics(Map<String, ThreadMetrics> threadMetrics) {
		ThreadMetrics.threadMetrics = threadMetrics;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long[] getSucCount() {
		return sucCount;
	}

	public void setSucCount(long[] sucCount) {
		this.sucCount = sucCount;
	}

	public long[] getFailCount() {
		return failCount;
	}

	public void setFailCount(long[] failCount) {
		this.failCount = failCount;
	}

	public long[] getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(long[] totalCount) {
		this.totalCount = totalCount;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public ThreadMetrics(int length) {
		this.sucCount = new long[length + 10];
		this.failCount = new long[length + 10];
		this.totalCount = new long[length + 10];
		this.lost = new long[length + 10];
		this.fallback = new long[length + 10];
	}

	public void updateSuccess() {
		Long curIndex = (System.currentTimeMillis() - startTime) / 1000;
		sucCount[curIndex.intValue()]++;
		totalCount[curIndex.intValue()]++;
	}

	public void updateFailed() {
		Long curIndex = (System.currentTimeMillis() - startTime) / 1000;
		failCount[curIndex.intValue()]++;
		totalCount[curIndex.intValue()]++;
	}

	public void updateLost(){
		Long curIndex = (System.currentTimeMillis() - startTime) / 1000;
		lost[curIndex.intValue()]++;
		totalCount[curIndex.intValue()]++;
	}
	
	public void updateFallback() {
		Long curIndex = (System.currentTimeMillis() - startTime) / 1000;
		fallback[curIndex.intValue()]++;
		totalCount[curIndex.intValue()]++;
	}
	
	public void setStartTime() {
		this.startTime = System.currentTimeMillis();
		//this.endTime = this.startTime;
	}

	public void setEndTime() {
		this.endTime = System.currentTimeMillis();
	}

	public long getEndTime() {
		return endTime;
	}

	public long[] getLost() {
		return lost;
	}

	public void setLost(long[] lost) {
		this.lost = lost;
	}
}
