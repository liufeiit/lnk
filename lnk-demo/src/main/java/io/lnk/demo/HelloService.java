package io.lnk.demo;

import java.io.Serializable;

import io.lnk.api.InvokeType;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.annotation.LnkService;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月24日 下午2:53:30
 */
@LnkService(group = "biz-pay-bgw-payment.srv")
public interface HelloService {

    @LnkMethod(type = InvokeType.SYNC, timeoutMillis = 5000L)
    ComplexResponse welcome(String name, ComplexRequest request);

    @LnkMethod(type = InvokeType.ASYNC, timeoutMillis = 5000L)
    void welcome(String name, WelcomeCallback callback);

    @LnkMethod(type = InvokeType.MULTICAST, timeoutMillis = 5000L)
    void welcomeMulticast(String name, WelcomeCallback callback);

    @LnkMethod(type = InvokeType.ASYNC, timeoutMillis = 5000L)
    void welcome(String name);

    @LnkMethod(type = InvokeType.MULTICAST, timeoutMillis = 5000L)
    void welcomeMulticast(String name);

    public static class ComplexRequest implements Serializable {
        private static final long serialVersionUID = 4528075144492619755L;
        private String name;
        private int age;
        private byte[] ext;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public byte[] getExt() {
            return ext;
        }

        public void setExt(byte[] ext) {
            this.ext = ext;
        }
    }

    public static class ComplexResponse implements Serializable {
        private static final long serialVersionUID = -6187476371003086122L;
        private String name;
        private byte[] ext;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public byte[] getExt() {
            return ext;
        }

        public void setExt(byte[] ext) {
            this.ext = ext;
        }
    }
}
