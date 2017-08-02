package io.lnk.lookup;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月28日 下午10:23:12
 */
public final class Paths {
    public static final String PATH_SEPARATOR = "/";
    public static final String ROOT = "/lnk/";
    public static final class Registry {
        public static final String ROOT_REGISTRY = ROOT + "registry";
        public static final String PROVIDERS = "providers";
        public static String createPath(String serviceId) {
            return new StringBuilder(ROOT_REGISTRY).append(PATH_SEPARATOR).append(serviceId).toString();
        }
        public static String createPath(String serviceId, String version) {
            return new StringBuilder(ROOT_REGISTRY).append(PATH_SEPARATOR).append(serviceId).append(PATH_SEPARATOR).append(version).toString();
        }
        public static String createPath(String serviceId, String version, int protocol) {
            return new StringBuilder(ROOT_REGISTRY).append(PATH_SEPARATOR).append(serviceId).append(PATH_SEPARATOR).append(version).append(PATH_SEPARATOR).append(protocol).append(PATH_SEPARATOR).append(PROVIDERS).toString();
        }
    }
    public static final class Manangement {
        public static final String ROOT_MANANGEMENT = ROOT + "manangement";
        public static String createPath(String userName) {
            return new StringBuilder(ROOT_MANANGEMENT).append(PATH_SEPARATOR).append(userName).toString();
        }
    }
}
