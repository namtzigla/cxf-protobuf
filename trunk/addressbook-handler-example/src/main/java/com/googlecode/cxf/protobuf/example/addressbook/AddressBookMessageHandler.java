package com.googlecode.cxf.protobuf.example.addressbook;

import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.Message;
import com.googlecode.cxf.protobuf.ProtobufMessageHandler;
import com.googlecode.cxf.protobuf.addressbook.AddressBookProtos.AddressBook;
import com.googlecode.cxf.protobuf.addressbook.AddressBookProtos.AddressBookServiceMessage;
import com.googlecode.cxf.protobuf.addressbook.AddressBookProtos.AddressBookSize;
import com.googlecode.cxf.protobuf.addressbook.AddressBookProtos.NamePattern;
import com.googlecode.cxf.protobuf.addressbook.AddressBookProtos.Person;

public class AddressBookMessageHandler implements ProtobufMessageHandler {
	List<Person> records = new ArrayList<Person>();

	/**
	 * @see com.googlecode.cxf.protobuf.ProtobufMessageHandler#handleMessage(com.google.protobuf.Message)
	 */
	public Message handleMessage(Message message) {
		AddressBookServiceMessage searchServiceMessage = (AddressBookServiceMessage) message;

		if (searchServiceMessage.hasField(AddressBookServiceMessage
				.getDescriptor().findFieldByName("listPeople"))) {
			AddressBook.Builder addressbook = AddressBook.newBuilder();
			NamePattern request = searchServiceMessage.getListPeople();

			for (Person person : records) {
				if (person.getName().indexOf(request.getPattern()) >= 0) {
					addressbook.addPerson(person);
				}
			}

			return addressbook.build();
		} else if (searchServiceMessage.hasField(AddressBookServiceMessage
				.getDescriptor().findFieldByName("addPerson"))) {
			Person request = searchServiceMessage.getAddPerson();
			records.add(request);
			return AddressBookSize.newBuilder().setSize(records.size()).build();
		} else {
			throw new RuntimeException("Payload not found in message "
					+ message);
		}
	}

}
