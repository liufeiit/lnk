package io.lnk.demo.test.thread;

public class RunThread extends Thread {
	// 持续时间
	private long ctTime;
	// 最大次数
	private long maxCount;
	// 当前metrics
	private ThreadMetrics metrics;
	// work
	private RunWork work;

	public RunThread(long ctTime, long maxCount, RunWork work) {
		this.ctTime = ctTime;
		this.maxCount = maxCount;
		this.work = work;
	}

	public void run() {
		metrics = new ThreadMetrics((int) (ctTime / 1000 + 1));
		String threadId = Integer.valueOf((int) Thread.currentThread().getId()).toString();
		ThreadMetrics.threadMetrics.put(threadId, metrics);
		long count = 0;
		long endTime = System.currentTimeMillis() + ctTime;
		metrics.setStartTime();
		while (true) {
			try {
				if (maxCount != 0 && count++ > maxCount) {
					break;
				}
				if (System.currentTimeMillis() >= endTime) {
					break;
				}
				
				switch (work.run()){
				case Success:
					metrics.updateSuccess();
					break;
				case Fail:
					metrics.updateFailed();
					break;
				case Lost:
					metrics.updateLost();
					break;
				case Fallback:
					metrics.updateFallback();
					break;
				}
			} catch (Exception e) {
				try {
					metrics.updateFailed();
				} catch (Exception e1) {

				}
			}
		}
		metrics.setEndTime();
	}
}
