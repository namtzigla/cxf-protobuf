package com.google.code.cxf.protobuf.example.addressbook;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.code.cxf.protobuf.ProtobufMessageHandler;
import com.google.protobuf.Message;

public class AddressBookMessageHandler implements
		ProtobufMessageHandler<AddressBookServiceMessage> {
	Map<Integer, Person> records = new ConcurrentHashMap<Integer, Person>();

	public Message handleMessage(AddressBookServiceMessage message) {

		if (message.hasField(AddressBookServiceMessage.getDescriptor()
				.findFieldByName("listPeople"))) {
			AddressBook.Builder addressbook = AddressBook.newBuilder();
			NamePattern request = message.getListPeople();

			for (Person person : records.values()) {
				if (person.getName().indexOf(request.getPattern()) >= 0) {
					addressbook.addPerson(person);
				}
			}

			return addressbook.build();
		} else if (message.hasField(AddressBookServiceMessage.getDescriptor()
				.findFieldByName("addPerson"))) {

			Person request = message.getAddPerson();
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
