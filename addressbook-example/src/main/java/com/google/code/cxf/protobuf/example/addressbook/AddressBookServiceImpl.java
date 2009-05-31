package com.google.code.cxf.protobuf.example.addressbook;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookService;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class AddressBookServiceImpl extends AddressBookService {
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

}
