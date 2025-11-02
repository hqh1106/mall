package com.hqh.leafcore.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class PropertiesFactory {
    private static final Logger logger = LoggerFactory.getLogger(PropertiesFactory.class);

    private static final Properties properties = new Properties();

    static {
        try{
            properties.load(PropertiesFactory.class.getClassLoader().getResourceAsStream("leaf.properties"));
        }catch (IOException e){
            logger.warn("Load Properties Ex: ",e);
        }
    }
    public static Properties getProperties(){
        return properties;
    }
}
