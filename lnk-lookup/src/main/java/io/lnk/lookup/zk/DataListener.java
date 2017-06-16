package io.lnk.lookup.zk;

public interface DataListener {
	void dataChanged(String path, String data);
}
