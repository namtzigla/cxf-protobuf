package com.googlecode.cxf.protobuf;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.MessageSenderInterceptor;
import org.apache.cxf.interceptor.OneWayProcessorInterceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.interceptor.ServiceInvokerInterceptor;
import org.apache.cxf.transport.local.LocalTransportFactory;
import org.junit.Test;

import com.example.tutorial.AddressBookProtos;
import com.example.tutorial.AddressBookProtos.AddressBook;
import com.example.tutorial.AddressBookProtos.AddressBookService;
import com.example.tutorial.AddressBookProtos.AddressBookServiceMessage;
import com.example.tutorial.AddressBookProtos.AddressBookSize;
import com.example.tutorial.AddressBookProtos.NamePattern;
import com.example.tutorial.AddressBookProtos.Person;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.cxf.protobuf.client.SimpleRpcChannel;
import com.googlecode.cxf.protobuf.client.SimpleRpcController;
import com.googlecode.cxf.protobuf.interceptor.ProtobufMessageInInterceptor;
import com.googlecode.cxf.protobuf.interceptor.ProtobufMessageOutInterceptor;

public class AddressBookRPCServiceImplTest extends AbstractCXFProtobufTest {
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
		 * @see com.example.tutorial.AddressBookProtos.SearchService#add(com.google.protobuf.RpcController,
		 *      com.example.tutorial.AddressBookProtos.Person,
		 *      com.google.protobuf.RpcCallback)
		 */
		public void addPerson(RpcController controller, Person request,
				RpcCallback<AddressBookSize> done) {
			records.add(request);
			done.run(AddressBookSize.newBuilder().setSize(records.size())
					.build());
		}
	};

	@Test
	public void testInterceptors() throws Exception {
		ProtobufServerFactoryBean serverFactoryBean = createServer(
				"http://localhost:8888/SearchService",
				LocalTransportFactory.TRANSPORT_ID, serviceBean,
				AddressBookServiceMessage.class);

		List<Interceptor> interceptors = serverFactoryBean.getServer()
				.getEndpoint().getInInterceptors();
		System.out.println(interceptors);
		assertEquals(0, interceptors.size());

		interceptors = serverFactoryBean.getServer().getEndpoint()
				.getOutInterceptors();
		System.out.println(interceptors);
		assertEquals(1, interceptors.size());
		assertTrue(hasInterceptor(interceptors, MessageSenderInterceptor.class));

		interceptors = serverFactoryBean.getServiceFactory().getService()
				.getInInterceptors();
		System.out.println(interceptors);
		assertEquals(3, interceptors.size());
		assertTrue(hasInterceptor(interceptors, ServiceInvokerInterceptor.class));
		assertTrue(hasInterceptor(interceptors, OutgoingChainInterceptor.class));
		assertTrue(hasInterceptor(interceptors,
				OneWayProcessorInterceptor.class));

		interceptors = serverFactoryBean.getServiceFactory().getService()
				.getOutInterceptors();
		System.out.println(interceptors);
		assertEquals(0, interceptors.size());

		interceptors = serverFactoryBean.getServer().getEndpoint().getBinding()
				.getInInterceptors();
		System.out.println(interceptors);
		assertEquals(1, interceptors.size());
		assertTrue(hasInterceptor(interceptors,
				ProtobufMessageInInterceptor.class));

		interceptors = serverFactoryBean.getServer().getEndpoint().getBinding()
				.getOutInterceptors();
		System.out.println(interceptors);
		assertEquals(1, interceptors.size());
		assertTrue(hasInterceptor(interceptors,
				ProtobufMessageOutInterceptor.class));

		Person.Builder person = Person.newBuilder();

		person.setId(10);
		person.setName("Bela");

		AddressBookServiceMessage searchServiceMessage = AddressBookServiceMessage
				.newBuilder().setAddPerson(person).build();

		AddressBookSize response = AddressBookSize.parseFrom(testUtilities
				.invokeBytes("http://localhost:8888/SearchService",
						LocalTransportFactory.TRANSPORT_ID,
						toBytes(searchServiceMessage)));
		System.out.println(response);
		assertEquals(1, response.getSize());
	}

	@Test
	public void testService() throws Exception {
		createServer("http://localhost:8888/SearchService",
				LocalTransportFactory.TRANSPORT_ID, serviceBean,
				AddressBookServiceMessage.class);

		Person.Builder person = Person.newBuilder();

		person.setId(10);
		person.setName("Bela");

		AddressBookServiceMessage searchServiceMessage = AddressBookServiceMessage
				.newBuilder().setAddPerson(person).build();

		AddressBookSize response = AddressBookSize.parseFrom(testUtilities
				.invokeBytes("http://localhost:8888/SearchService",
						LocalTransportFactory.TRANSPORT_ID,
						toBytes(searchServiceMessage)));
		System.out.println(response);
		assertEquals(1, response.getSize());
	}

	private AddressBookSize size;
	private AddressBook book;

	@Test
	public void testServiceWithClient() throws Exception {
		Person.Builder person = Person.newBuilder();

		person.setId(10);
		person.setName("Bela");
		Person bela = person.build();

		createServer("local://localhost:8888/AddressBookService",
				LocalTransportFactory.TRANSPORT_ID, serviceBean,
				AddressBookServiceMessage.class);

		AddressBookService service = AddressBookService
				.newStub(new SimpleRpcChannel(
						"local://localhost:8888/AddressBookService",
						AddressBookServiceMessage.class));

		System.out.println(bela);

		SimpleRpcController controller = new SimpleRpcController();

		final CountDownLatch latch = new CountDownLatch(2);

		service.addPerson(controller, bela, new RpcCallback<AddressBookSize>() {
			public void run(AddressBookSize size) {
				AddressBookRPCServiceImplTest.this.size = size;
				System.out.println("There are " + size.getSize()
						+ " person(s) in the address book.");
				latch.countDown();
			}
		});

		controller.reset();

		service.listPeople(controller, NamePattern.newBuilder().setPattern("B")
				.build(), new RpcCallback<AddressBook>() {
			public void run(AddressBook response) {
				AddressBookRPCServiceImplTest.this.book = response;
				System.out.println("List of persons found: " + response);
				latch.countDown();
			}
		});

		if (!latch.await(3000, TimeUnit.MILLISECONDS)) {
			throw new RuntimeException("Test timed out.");
		}

		Assert.assertEquals(1, size.getSize());
		Assert.assertEquals(book.getPersonCount(), 1);
		Assert.assertEquals(book.getPerson(0).getName(), "Bela");
	}
}
