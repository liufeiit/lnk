package io.lnk.lookup.consul;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsulUtils {

	/**
	 * 判断两个list中的url是否一致。 如果任意一个list为空，则返回false； 此方法并未做严格互相判等
	 *
	 * @param urls1
	 * @param urls2
	 * @return
	 */
	public static boolean isSame(List<URL> urls1, List<URL> urls2) {
		if (urls1 == null || urls2 == null) {
			return false;
		}
		if (urls1.size() != urls2.size()) {
			return false;
		}
		return urls1.containsAll(urls2);
	}

	/**
	 * 根据服务的url生成consul对应的service
	 *
	 * @param url
	 * @return
	 */
	public static ConsulService buildService(URL url) {
		ConsulService service = new ConsulService();
		service.setAddress(url.getHost());
		service.setId(ConsulUtils.convertConsulSerivceId(url));
		service.setName(ConsulUtils.convertGroupToServiceName(url.getServiceGroup()));
		service.setPort(url.getPort());
		service.setTtl(ConsulConstants.TTL);

		List<String> tags = new ArrayList<String>();
		tags.add(ConsulConstants.CONSUL_TAG_LNK_PROTOCOL + url.getProtocol());
		tags.add(ConsulConstants.CONSUL_TAG_LNK_VERSION + url.getVersion());
		tags.add(ConsulConstants.CONSUL_TAG_LNK_URL + StringTools.urlEncode(url.toFullStr()));
		service.setTags(tags);

		return service;
	}

	/**
	 * 根据service生成lnk使用的
	 *
	 * @param service
	 * @return
	 */
	public static URL buildUrl(ConsulService service) {
		URL url = null;
		for (String tag : service.getTags()) {
			if (tag.startsWith(ConsulConstants.CONSUL_TAG_LNK_URL)) {
				String encodeUrl = tag.substring(tag.indexOf("_") + 1);
				url = URL.valueOf(StringTools.urlDecode(encodeUrl));
			}
		}
		if (url == null) {
			Map<String, String> params = new HashMap<String, String>();
			String group = service.getName().substring(ConsulConstants.CONSUL_SERVICE_LNK_PRE.length());
			params.put("group", group);
			params.put("nodetype", "service");
			int protocol = ConsulUtils.getProtocolFromTag(service.getTags().get(0));
			String version = ConsulUtils.getVersionFromTag(service.getTags().get(1));
			url = new URL(group, ConsulUtils.getPathFromServiceId(service.getId()), version, protocol,
					service.getAddress(), service.getPort());
		}
		return url;
	}

	/**
	 * 根据url获取cluster信息，cluster 信息包括协议,version, serviceGroup,
	 * serviceId（rpc服务中的接口类）。
	 *
	 * @param url
	 * @return
	 */
	public static String getUrlClusterInfo(URL url) {
		return url.getServiceGroup() + "-" + url.getServiceId() + "-" + url.getVersion() + "-" + url.getProtocol();
	}

	/**
	 * 有lnk的group生成consul的serivce name
	 *
	 * @param group
	 * @return
	 */
	public static String convertGroupToServiceName(String group) {
		return ConsulConstants.CONSUL_SERVICE_LNK_PRE + group;
	}

	/**
	 * 从consul的service name中获取lnk的group
	 *
	 * @param group
	 * @return
	 */
	public static String getGroupFromServiceName(String group) {
		return group.substring(ConsulConstants.CONSUL_SERVICE_LNK_PRE.length());
	}

	/**
	 * 根据lnk的url生成consul的serivce id。 serviceid 包括ip＋port＋rpc服务的接口类名
	 *
	 * @param url
	 * @return
	 */
	public static String convertConsulSerivceId(URL url) {
		if (url == null) {
			return null;
		}
		return convertServiceId(url.getHost(), url.getPort(), url.getServiceId());
	}

	/**
	 * 从consul 的serviceid中获取rpc服务的接口类名（url的path）
	 *
	 * @param serviceId
	 * @return
	 */
	public static String getPathFromServiceId(String serviceId) {
		return serviceId.substring(serviceId.indexOf("-") + 1);
	}

	/**
	 * 从consul的tag获取lnk的protocol
	 *
	 * @param tag
	 * @return
	 */
	public static int getProtocolFromTag(String tag) {
		return Integer.parseInt(tag.substring(ConsulConstants.CONSUL_TAG_LNK_PROTOCOL.length()));
	}

	public static String getVersionFromTag(String tag) {
		return tag.substring(ConsulConstants.CONSUL_TAG_LNK_VERSION.length());
	}

	public static String convertServiceId(String host, int port, String serviceId) {
		return host + ":" + port + "-" + serviceId;
	}
}
