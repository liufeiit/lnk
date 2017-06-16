package io.lnk.demo.test.thread;

import java.text.SimpleDateFormat;
import java.util.Map;

public class ConcurrentThreadMetrics {
	public static void start(RunWork work, long ctTime, long maxCount, int maxThreads) {
		RunThread[] runThread = new RunThread[maxThreads];
		for (int i = 0; i < maxThreads; i++) {
			runThread[i] = new RunThread(ctTime, maxCount, work);
		}
		for (int i = 0; i < maxThreads; i++) {
			runThread[i].start();
		}
		startRealThread(ctTime, maxCount, maxThreads);
		for (int i = 0; i < maxThreads; i++) {
			try {
				runThread[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void startRealThread(final long ctTime, final long maxCount, final int maxThreads) {
		Thread thread = new Thread() {
			public void run() {
				final long startTime = System.currentTimeMillis();
				while (true) {
					try {
						Thread.sleep(1000);
						ConcurrentThreadMetrics.realResult(ctTime, maxCount, maxThreads);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					}
					if (System.currentTimeMillis() > startTime + ctTime) {
						break;
					}
				}
			}
		};
		thread.start();
	}

	public static void realResult(long ctTime, long maxCount, int maxThreads) {
		Map<String, ThreadMetrics> metrics = ThreadMetrics.threadMetrics;
		long startTime = getMinStartTime(metrics);
		long endTime = System.currentTimeMillis();
		int lengthCount = (int) ((endTime - startTime) / 1000);
		if (lengthCount <= 0) {
			return;
		}
		long[] sucCount = new long[lengthCount];
		long[] failCount = new long[lengthCount];
		long[] totalCount = new long[lengthCount];
		long[] lostCount = new long[lengthCount];
		long[] fallbackCount = new long[lengthCount];
		System.out.println("startTime:" + new SimpleDateFormat("yyyy-MM-ddHH:mm:ss:SS").format(startTime) + ", curTime:"
				+ new SimpleDateFormat("yyyy-MM-ddHH:mm:ss:SS").format(endTime));
		for (String key : metrics.keySet()) {
			ThreadMetrics metric = metrics.get(key);
			long[] fail = metric.getFailCount();
			addCount(metric, failCount, fail, startTime);
			long[] suc = metric.getSucCount();
			addCount(metric, sucCount, suc, startTime);
			long[] total = metric.getTotalCount();
			addCount(metric, totalCount, total, startTime);
			long[] lost = metric.getLost();
			addCount(metric, lostCount, lost, startTime);
			long[] fallback = metric.getFallback();
			addCount(metric, fallbackCount, fallback, startTime);
		}
		dumpCount(startTime, endTime, lengthCount, sucCount, failCount, totalCount, lostCount, fallbackCount);
		System.out.println("------------------------------------------------------------");
	}

	public static void dumpResult(long ctTime, long maxCount, int maxThreads) {
		Map<String, ThreadMetrics> metrics = ThreadMetrics.threadMetrics;
		long startTime = getMinStartTime(metrics);
		long endTime = getMaxEndTime(metrics);
		int lengthCount = (int) ((endTime - startTime) / 1000);
		long[] sucCount = new long[lengthCount];
		long[] failCount = new long[lengthCount];
		long[] lostCount = new long[lengthCount];
		long[] fallbackCount = new long[lengthCount];
		long[] totalCount = new long[lengthCount];
		System.out.println("startTime:" + new SimpleDateFormat("yyyy-MM-ddHH:mm:ss:SS").format(startTime) + ", endTime:"
				+ new SimpleDateFormat("yyyy-MM-ddHH:mm:ss:SS").format(endTime));
		for (String key : metrics.keySet()) {
			ThreadMetrics metric = metrics.get(key);
			long[] fail = metric.getFailCount();
			addCount(metric, failCount, fail, startTime);
			long[] suc = metric.getSucCount();
			addCount(metric, sucCount, suc, startTime);
			long[] total = metric.getTotalCount();
			addCount(metric, totalCount, total, startTime);
			long[] lost = metric.getLost();
			addCount(metric, lostCount, lost, startTime);
			long[] fallback = metric.getFallback();
			addCount(metric, fallbackCount, fallback, startTime);
		}
		dumpCount(startTime, endTime, lengthCount, sucCount, failCount, totalCount, lostCount, fallbackCount);
	}

	public static void dumpCount(long startTime, long endTime, long lengthCount, long[] sucCount, long[] failCount,
			long[] totalCount, long[] lostCount, long[] fallbackCount) {
		long maxSuc = maxCount(sucCount);
		long minSuc = minCount(sucCount);
		long averageSuc = sumCount(sucCount);
		long fiveSuc = curFiveMaxCount(sucCount);
		long maxFail = maxCount(failCount);
		long minFail = minCount(failCount);
		long averageFail = sumCount(failCount);
		long fiveFail = curFiveMaxCount(failCount);
		long maxTotal = maxCount(totalCount);
		long minTotal = minCount(totalCount);
		long averageTotal = sumCount(totalCount);
		long fiveTotal = curFiveMaxCount(totalCount);
		long maxLost = maxCount(lostCount);
		long minLost = minCount(lostCount);
		long averageLost = sumCount(lostCount);
		long fiveLost = curFiveMaxCount(lostCount);
		long maxFallback = maxCount(fallbackCount);
		long minFallback = minCount(fallbackCount);
		long averageFallback = sumCount(fallbackCount);
		long fiveFallback = curFiveMaxCount(fallbackCount);
		System.out.println("allSuc:" + averageSuc + ",MaxSuc:" + maxSuc + ",minSuc:" + minSuc + ",averageSuc:"
				+ averageSuc / lengthCount + ",curSuc:" + fiveSuc);
		System.out.println("allFail:" + averageFail + ",MaxFail:" + maxFail + ",minFail:" + minFail + ",averageFail:"
				+ averageFail / lengthCount + ",curFail:" + fiveFail);
		System.out.println("allTotal:" + averageTotal + ",MaxTotal:" + maxTotal + ",minTotal:" + minTotal
				+ ",averageTotal:" + averageTotal / lengthCount + ",curTotal:" + fiveTotal);
		System.out.println("allLost:" + averageLost + ",MaxLost:" + maxLost + ",minLost:" + minLost + ",averageLost:"
				+ averageLost / lengthCount + ",curLost:" + fiveLost);
		System.out.println("allFallback:" + averageFallback + ",MaxFallback:" + maxFallback + ",minFallback:"
				+ minFallback + ",averageFallback:" + averageFallback / lengthCount + ",curFallback:" + fiveFallback);
		System.out.flush();
	}

	public static long sumCount(long[] count) {
		long sum = 0;
		for (int i = 0; i < count.length; i++) {
			sum += count[i];
		}
		return sum;
	}

	public static long curFiveMaxCount(long[] count) {
		long max = 0;
		int checkLen = count.length > 5 ? 5 : count.length;
		for (int i = count.length - checkLen; i < count.length; i++) {
			if (count[i] > max) {
				max = count[i];
			}
		}
		return max;
	}

	public static long minCount(long[] count) {
		long countMin = count[0];
		for (int i = 0; i < count.length; i++) {
			if (countMin == 0) {
				countMin = count[i];
			} else {
				if (count[i] != 0) {
					countMin = countMin < count[i] ? countMin : count[i];
				}
			}
		}
		return countMin;
	}

	public static long maxCount(long[] count) {
		long countMax = count[0];
		for (int i = 0; i < count.length; i++) {
			countMax = countMax < count[i] ? count[i] : countMax;
		}
		return countMax;
	}

	public static long getMaxEndTime(Map<String, ThreadMetrics> metrics) {
		long endTime = System.currentTimeMillis();
		for (String key : metrics.keySet()) {
			ThreadMetrics metric = metrics.get(key);
			long end = metric.getEndTime();
			endTime = endTime > end ? endTime : end;
		}
		return endTime;
	}

	public static long getMinStartTime(Map<String, ThreadMetrics> metrics) {
		long startTime = System.currentTimeMillis();
		for (String key : metrics.keySet()) {
			ThreadMetrics metric = metrics.get(key);
			long start = metric.getStartTime();
			startTime = startTime < start ? startTime : start;
		}
		return startTime;
	}

	public static void addCount(ThreadMetrics metric, long[] src, long[] dst, long startTime) {
		long mStartTime = metric.getStartTime();
		int diff = (int) ((mStartTime - startTime) / 1000);
		int length = src.length < dst.length ? src.length - diff
				: src.length > dst.length + diff ? dst.length : (src.length - diff);
		for (int i = 0; i < length; i++) {
			src[i + diff] += dst[i];
		}
	}
}
