package com.google.code.cxf.protobuf.example.addressbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.code.cxf.protobuf.ProtobufMessageHandler;
import com.google.protobuf.Message;

public class AddressBookMessageHandler implements ProtobufMessageHandler {
	Map<Integer, Person> records = new ConcurrentHashMap<Integer, Person>();

	/**
	 * @see com.googlecode.cxf.protobuf.ProtobufMessageHandler#handleMessage(com.google.protobuf.Message)
	 */
	public Message handleMessage(Message message) {
		AddressBookServiceMessage searchServiceMessage = (AddressBookServiceMessage) message;

		if (searchServiceMessage.hasField(AddressBookServiceMessage
				.getDescriptor().findFieldByName("listPeople"))) {
			AddressBook.Builder addressbook = AddressBook.newBuilder();
			NamePattern request = searchServiceMessage.getListPeople();

			for (Person person : records.values()) {
				if (person.getName().indexOf(request.getPattern()) >= 0) {
					addressbook.addPerson(person);
				}
			}

			return addressbook.build();
		} else if (searchServiceMessage.hasField(AddressBookServiceMessage
				.getDescriptor().findFieldByName("addPerson"))) {

			Person request = searchServiceMessage.getAddPerson();
			if (records.containsKey(request.getId())) {
				System.out.println("Warning: will replace existing person: "
						+ records.get(request.getId()));
			}
			records.put(request.getId(), request);
			return AddressBookSize.newBuilder().setSize(records.size()).build();
		} else {
			throw new RuntimeException("Payload not found in message "
					+ message);
		}
	}

}
