package com.googlecode.cxf.protobuf.example.addressbook;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.binding.BindingFactoryManager;
import org.apache.cxf.transports.http.QueryHandlerRegistry;

import com.example.tutorial.AddressBookProtos;
import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.cxf.protobuf.ProtobufQueryHandler;
import com.googlecode.cxf.protobuf.ProtobufServerFactoryBean;
import com.googlecode.cxf.protobuf.binding.ProtobufBindingFactory;

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
		// register protocol buffer extensions on cxf bus
		Bus bus = BusFactory.getDefaultBus();
		ProtobufBindingFactory protobufBindingFactory = new ProtobufBindingFactory();
		protobufBindingFactory.setBus(bus);

		BindingFactoryManager manager = bus
				.getExtension(BindingFactoryManager.class);
		manager.registerBindingFactory(
				ProtobufBindingFactory.PROTOBUF_BINDING_ID,
				protobufBindingFactory);

		QueryHandlerRegistry queryHandlerRegistry = bus
				.getExtension(QueryHandlerRegistry.class);
		queryHandlerRegistry.registerHandler(new ProtobufQueryHandler());

		// create addressbook service
		ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
		serverFactoryBean
				.setAddress("http://localhost:8888/AddressBookService");
		serverFactoryBean
				.setServiceBean(new AddressBookProtos.AddressBookService() {
					Map<Integer, Person> records = new ConcurrentHashMap<Integer, Person>();

					public void listPeople(RpcController controller,
							NamePattern request, RpcCallback<AddressBook> done) {
						AddressBook.Builder addressbook = AddressBook
								.newBuilder();

						for (Person person : records.values()) {
							if (person.getName().indexOf(request.getPattern()) >= 0) {
								addressbook.addPerson(person);
							}
						}

						done.run(addressbook.build());
					}

					public void addPerson(RpcController controller,
							Person request, RpcCallback<AddressBookSize> done) {
						if (records.containsKey(request.getId())) {
							System.out.println("Warning: will replace existing person: " + records.get(request.getId()));
						}
						records.put(request.getId(), request);
						done.run(AddressBookSize.newBuilder().setSize(
								records.size()).build());
					}
				});
		serverFactoryBean.setMessageClass(AddressBookServiceMessage.class);
		serverFactoryBean.create();
	}

}
