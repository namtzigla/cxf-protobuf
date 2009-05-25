package com.googlecode.cxf.protobuf.example.addressbook;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Simple example that publishes Google's addressbook example as a service in
 * CXF. See Spring configuration in serverConfig.xml.
 * 
 * @author Gyorgy Orban
 */
public class SpringServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("classpath:/com/googlecode/cxf/protobuf/example/addressbook/serverConfig.xml");
	}

}
