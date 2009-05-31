package com.google.code.cxf.protobuf.example.addressbook;

import org.apache.cxf.endpoint.EndpointException;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookService;
import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.code.cxf.protobuf.client.SimpleRpcChannel;
import com.google.code.cxf.protobuf.client.SimpleRpcController;
import com.google.protobuf.RpcCallback;

/**
 * Simple client for the addressbook service.
 * 
 * @author Gyorgy Orban
 */
public class Client {

	/**
	 * @param args
	 * @throws EndpointException
	 */
	public static void main(String[] args) throws EndpointException {
		System.out.println("Create a person:\n");

		Person.Builder person = Person.newBuilder();

		person.setId(1);
		person.setName("Alice");
		Person alice = person.build();

		System.out.println(alice);

		AddressBookService service = AddressBookService
				.newStub(new SimpleRpcChannel(
						"http://localhost:8888/AddressBookService",
						AddressBookServiceMessage.class));


		SimpleRpcController controller = new SimpleRpcController();

		System.out.println("Adding " + alice.getName() + " to the addressbook.");
		service.addPerson(controller, alice, new RpcCallback<AddressBookSize>() {
			public void run(AddressBookSize size) {
				System.out.println("\nThere are " + size.getSize()
						+ " person(s) in the address book now.");
			}
		});

		controller.reset();

		System.out.println("\nSearching for people with 'A' in their name.");
		service.listPeople(controller, NamePattern.newBuilder().setPattern("A")
				.build(), new RpcCallback<AddressBook>() {
			public void run(AddressBook response) {
				System.out.println("\nList of people found: \n" + response);
			}
		});
	}

}
