package com.google.code.cxf.protobuf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.junit.Test;

import com.google.code.cxf.protobuf.addressbook.AddressBookProtos;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.AddressBook;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.AddressBookService;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.AddressBookServiceMessage;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.AddressBookSize;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.NamePattern;
import com.google.code.cxf.protobuf.addressbook.AddressBookProtos.Person;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class ProtobufQueryHandlerTest extends AbstractCXFProtobufTest {
	AddressBookService serviceBean = new AddressBookProtos.AddressBookService() {
		List<Person> records = new ArrayList<Person>();

		public void listPeople(RpcController controller, NamePattern request,
				RpcCallback<AddressBook> done) {
			AddressBook.Builder addressbook = AddressBook.newBuilder();

			for (Person person : records) {
				if (person.getName().indexOf(request.getPattern()) >= 0) {
					addressbook.addPerson(person);
				}
			}

			done.run(addressbook.build());
		}

		/**
		 * @see com.google.code.cxf.protobuf.addressbook.AddressBookProtos.SearchService#add(com.google.protobuf.RpcController,
		 *      com.google.code.cxf.protobuf.addressbook.AddressBookProtos.Person,
		 *      com.google.protobuf.RpcCallback)
		 */
		public void addPerson(RpcController controller, Person request,
				RpcCallback<AddressBookSize> done) {
			records.add(request);
			done.run(AddressBookSize.newBuilder().setSize(records.size())
					.build());
		}
	};
	
	ProtobufMessageHandler addressBookMessageHandler = new ProtobufMessageHandler() {
        List<Person> records = new ArrayList<Person>();

        /**
         * @see com.google.code.cxf.protobuf.ProtobufMessageHandler#handleMessage(com.google.protobuf.Message)
         */
        public Message handleMessage(Message message) {
        	AddressBookServiceMessage searchServiceMessage = (AddressBookServiceMessage) message;
            
            if (searchServiceMessage.hasField(AddressBookServiceMessage.getDescriptor().findFieldByName("listPeople"))) {
                AddressBook.Builder addressbook = AddressBook.newBuilder();
                NamePattern request = searchServiceMessage.getListPeople();
                
                for (Person person : records) {
                    if (person.getName().indexOf(request.getPattern()) >= 0) {
                        addressbook.addPerson(person);
                    }
                }
                
                return addressbook.build();
            } else if (searchServiceMessage.hasField(AddressBookServiceMessage.getDescriptor().findFieldByName("addPerson"))) {
                Person request = searchServiceMessage.getAddPerson();
                records.add(request);
                return AddressBookSize.newBuilder().setSize(records.size()).build();
            } else {
                throw new RuntimeException("Payload not found in message " + message);
            }
        }
        
    };

	@Test
	public void testRPCServiceProtoDescription() throws Exception {
		ProtobufServerFactoryBean serverFactory = createServer(
				"local://localhost:8888/AddressBookService",
				LocalTransportFactory.TRANSPORT_ID, serviceBean,
				AddressBookServiceMessage.class);

		EndpointInfo endpointInfo = serverFactory.getServer().getEndpoint()
				.getEndpointInfo();
		byte[] expected = IOUtils.readBytesFromStream(getClass().getResourceAsStream("/com/google/code/cxf/protobuf/AddressBookService.proto"));
		ByteArrayOutputStream actual = new ByteArrayOutputStream();
		new ProtobufQueryHandler().writeResponse(null, null, endpointInfo, actual);
		Assert.assertTrue("generated proto description is incorrect", Arrays.equals(expected, actual.toByteArray()));
	}
	
	@Test
	public void testMessageHandlerProtoDescription() throws Exception {
		ProtobufServerFactoryBean serverFactory = createServer(
				"local://localhost:8888/AddressBookService",
				LocalTransportFactory.TRANSPORT_ID, addressBookMessageHandler,
				AddressBookServiceMessage.class);

		EndpointInfo endpointInfo = serverFactory.getServer().getEndpoint()
				.getEndpointInfo();
		byte[] expected = IOUtils.readBytesFromStream(getClass().getResourceAsStream("/com/google/code/cxf/protobuf/AddressBookServiceMessage.proto"));
		ByteArrayOutputStream actual = new ByteArrayOutputStream();
		new ProtobufQueryHandler().writeResponse(null, null, endpointInfo, actual);
		System.out.println("\nexpected: \n" + new String(expected));
		System.out.println("\nactual: \n" + new String(actual.toByteArray()));
		Assert.assertTrue("generated proto description is incorrect", Arrays.equals(expected, actual.toByteArray()));
	}
}
