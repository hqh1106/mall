package com.hqh.leafcore.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class IPUtils {
    private static final Logger logger = LoggerFactory.getLogger(IPUtils.class);

    public static String getIp(){
        String ip;
        try{
            List<String> ips = getHostAddress(null);
            ip = (!ips.isEmpty())?ips.get(0):"";
        }catch (Exception e){
            ip = "";
            logger.warn("IpUtils get ip warn: ",e);
        }
        return ip;
    }
    public static String getIp(String interfaceName){
        String ip;
        interfaceName = interfaceName.trim();
        try {
            List<String> ipList = getHostAddress(interfaceName);
            ip = (!ipList.isEmpty()) ? ipList.get(0) : "";
        } catch (Exception ex) {
            ip = "";
            logger.warn("Utils get IP warn", ex);
        }
        return ip;
    }

    private static List<String> getHostAddress(String interfaceName) throws SocketException{
        List<String> ips = new ArrayList<>(5);
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()){
            NetworkInterface ni = interfaces.nextElement();
            Enumeration<InetAddress> addresses = ni.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress inetAddress = addresses.nextElement();
                if (inetAddress.isLoopbackAddress()){
                    continue;
                }
                if (inetAddress instanceof Inet6Address){
                    continue;
                }
                String hostAddress = inetAddress.getHostAddress();
                if (interfaceName==null || interfaceName.equals(ni.getDisplayName())){
                    ips.add(hostAddress);
                }
            }
        }
        return ips;
    }
}
