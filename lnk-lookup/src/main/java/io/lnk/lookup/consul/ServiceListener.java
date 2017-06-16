package io.lnk.lookup.consul;

import java.util.List;

public interface ServiceListener {

	void notifyService(URL refUrl, List<URL> urls);

}
