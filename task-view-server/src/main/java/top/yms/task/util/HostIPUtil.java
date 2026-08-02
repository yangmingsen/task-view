package top.yms.task.util;

import java.net.InetAddress;

public class HostIPUtil {
    public static String getLocalIP() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
