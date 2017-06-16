package io.lnk.api;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月23日 下午1:31:20
 */
public interface RemoteObjectFactory {
    
    /**
     * 生成指定stub接口的代理
     */
    <T> T getRemoteStub(Class<T> serviceInterface, String serializeStub);
    
    /**
     * 生成指定接口版本的代理
     */
    <T> T getServiceObject(Class<T> serviceInterface, String version);
    
    /**
     * 生成指定stub的代理
     */
    <T> T getServiceObject(String serializeStub);
}
