package com.google.code.cxf.protobuf.example.addressbook;

import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.googlecode.cxf.protobuf.ProtobufServerFactoryBean;

/**
 * Simple example that publishes Google's addressbook example as a service in
 * CXF.
 * 
 * @author Gyorgy Orban
 */
public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// create addressbook service
		ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
		serverFactoryBean
				.setAddress("http://localhost:8888/AddressBookService");
		serverFactoryBean.setServiceBean(new AddressBookServiceImpl());
		serverFactoryBean.setMessageClass(AddressBookServiceMessage.class);
		serverFactoryBean.create();

		System.out
				.println("Server is listening at http://localhost:8888/AddressBookService");
		System.out
				.println("Try http://localhost:8888/AddressBookService?proto for the description of the service.");
	}

}
