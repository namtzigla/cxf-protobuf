# User's Guide to cxf-protobuf #



## Introduction ##

cxf-protobuf is a simple integration of [Apache CXF](http://cxf.apache.org/), an open source services framework and [Google's Protocol Buffers](http://code.google.com/p/protobuf/). The main features are

  * **Simple API to publish both RPC and non-RPC style services using CXF's transports**
  * **Simple client API to send messages to protocol buffer services using CXF's transport infrastructure**
  * **Simple configuration in Spring, integration with CXF's configuration**
  * **?WSDL-style description generation for protocol buffer services (idea adopted from the SOAP world)**

cxf-protobuf is implemented as an extension to the CXF framework and makes use of CXF's standard (Spring-based) extension mechanisms.

## Examples ##

You can get the examples at the downloads page. You will need Maven to build them or, as an alternative, you can get the following jars manually and compile the examples against them with any tool or IDE you like:
  * [CXF jars and dependencies](http://www.apache.org/dyn/closer.cgi?path=%2Fcxf%2F2.2.2%2Fapache-cxf-2.2.2.zip) - everything from the lib folder
  * [cxf-protobuf jar](http://cxf-protobuf.googlecode.com/files/cxf-protobuf-0.3.jar)
You need to put these jars on your classpath to compile and run the examples.

If you want to use Maven, you need to do this:
  * download and install [Maven](http://maven.apache.org/download.html)
  * download the [cxf-protobuf project](http://cxf-protobuf.googlecode.com/files/cxf-protobuf-0.3-project.zip) and unzip it
  * go to its top level directory and build and install it in your local repo:
    * `mvn install`
  * download the project source of an example (e.g. the [addressbook-example](http://cxf-protobuf.googlecode.com/files/addressbook-example-0.3-project.zip)
  * build it:
    * `mvn jar:jar`
  * jar is generated in the target/ directory
  * run the `com.google.code.cxf.protobuf.example.addressbook.Server` class for the server and the `com.google.code.cxf.protobuf.example.addressbook.Client` class for the client

You can also import the example projects in Eclipse (you will need the [Eclipse Maven plugin](http://m2eclipse.codehaus.org/)).

### Example 1: AddressBook service ###

You can download this example [here](http://cxf-protobuf.googlecode.com/files/addressbook-example-0.3-project.zip).

All examples are built around the "address book" protocol buffer example that you can find in [Google's tutorial](http://code.google.com/apis/protocolbuffers/docs/javatutorial.html):


```
package tutorial;

option java_package = "com.example.tutorial";
option java_outer_classname = "AddressBookProtos";

message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;

  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  message PhoneNumber {
    required string number = 1;
    optional PhoneType type = 2 [default = HOME];
  }

  repeated PhoneNumber phone = 4;
}

message AddressBook {
  repeated Person person = 1;
}
```

To make it a service we need a service definition and a few extra messages:

```
service AddressBookService {
  rpc addPerson(Person) returns(AddressBookSize);
  rpc listPeople(NamePattern) returns(AddressBook);
}

message AddressBookSize {
  optional int32 size = 1;
}

message NamePattern {
  optional string pattern = 1;
}

```

So, we have the addPerson and listPeople operations: addPerson adds a new Person to the address book and returns the current size of the book; listPeople returns a subset of the address book that matches the name pattern passed.

Because protocol buffer messages are not self-describing, we need a "wrapper" message that the server expects to come in on the wire. The structure of the wrapper message is up to the developer of the service. In this case, for each operation in `AddressBookService` the wrapper message has a field with the same name and with the type of the operation's parameter:

```
message AddressBookServiceMessage {
  optional Person addPerson = 1;
  optional NamePattern listPeople = 2;
}
```

I chose the name `AddressBookServiceMessage` to make it clear that our `AddressBookService` implementation always expects this type of message as the request. The client can decide which operation he or she wants to invoke by filling in the corresponding field in `AddressBookServiceMessage` (because of this, each field is declared optional). It is important to note that this is just a convention, you can choose other ways to structure your request message.

All we need to do now is put these definitions in a file (addressbook.proto) and generate the java stub for the service and messages:

` protoc --java_out=. addressbook.proto `

This will generate the class `com.example.tutorial.AddressBookProtos`. This class contains the service interface and stub and all message types. The service interface (which is actually an abstract class) is called `com.example.tutorial.AddressBookProtos.AddressBookService`, you need to implement it like, for example:

```
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
		records.put(request.getId(), request);
		done.run(AddressBookSize.newBuilder().setSize(
				records.size()).build());
	}
}
```

To publish this class as a service (assuming you have the CXF dependency jars and the cxf-protobuf jar on your classpath):

```
		ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
		serverFactoryBean.setAddress("http://localhost:8888/AddressBookService");
		serverFactoryBean.setServiceBean(new AddressBookServiceImpl());
		serverFactoryBean.setMessageClass(AddressBookServiceMessage.class);
		serverFactoryBean.create();
```

and that is all, your service is listening at http://localhost:8888/AddressBookService. You can get the "description" of the service by entering http://localhost:8888/AddressBookService?proto in a browser. The generated description will contain the same information as the original addressbook.proto, so it can be used to generate a client that can call this service.

### Example 2: AddressBook client ###

You can download this example [here](http://cxf-protobuf.googlecode.com/files/addressbook-example-0.3-project.zip) (Client.java in the same project as the previous example).

In this example we create a client for the service we have just set up. First, you need to download the description of the service from http://localhost:8888/AddressBookService?proto

Let's say you saved it as addressbook.proto, now you can generate the java stub and message types:

` protoc --java_out=. addressbook.proto `

Now all you need to do is

```
	AddressBookService service = AddressBookService.newStub(new SimpleRpcChannel("http://localhost:8888/AddressBookService", AddressBookServiceMessage.class));
```

You can use this service interface to call the actual service methods via RPC, for example, if you want to add a person:

```
		Person.Builder person = Person.newBuilder();

		person.setId(1);
		person.setName("Alice");

	        SimpleRpcController controller = new SimpleRpcController();

		service.addPerson(controller, alice, new RpcCallback<AddressBookSize>() {
			public void run(AddressBookSize size) {
				System.out.println("\nThere are " + size.getSize()
						+ " person(s) in the address book now.");
			}
		});

```

### Example 3: AddressBook service configured in Spring ###

You can download this example [here](http://cxf-protobuf.googlecode.com/files/addressbook-spring-example-0.3-project.zip).

This example is basically the same as Example1, with the only difference that the server is now configured in the serverConfig.xml Spring config file:

```
...
	<bean class="com.google.code.cxf.protobuf.ProtobufServerFactoryBean"
		init-method="create">
		<property name="address" value="http://localhost:8888/AddressBookService" />
		<property name="serviceBean">
			<bean class="com.google.code.cxf.protobuf.example.addressbook.AddressBookServiceImpl" />
		</property>
		<property name="messageClass"
			value="com.example.tutorial.AddressBookProtos$AddressBookServiceMessage" />
	</bean>
...
```

and the java code to start up the server:

```
		new ClassPathXmlApplicationContext("classpath:/com/google/code/cxf/protobuf/example/addressbook/serverConfig.xml");
```

### Example 4: AddressBook message handler service ###

You can download this example [here](http://cxf-protobuf.googlecode.com/files/addressbook-handler-example-0.3-project.zip).

If you don't like RPC or don't want to define a service in your .proto file, or just simply want to handle the entire request message in your code, you can implement the `com.google.code.cxf.protobuf.ProtobufMessageHandler` interface:

```
public interface ProtobufMessageHandler<REQUEST extends Message> {

	Message handleMessage(REQUEST message);
}
```

If you implement this your `handleMessage` method will receive the entire request message directly (no dispatching to service methods):

```
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
```

## What if I don't want RPC or want to structure my request message differently? ##

You have two options:

  * You can implement the `ProtobufMessageHandler` interface (check out Example 4) which gives you full control over the request message and does not impose any particular RPC logic on you.

  * If you just don't like the simple default RPC method dispatching logic and wrapper message format (which matches the field name in the wrapper message with the method name in the service bean) you can plug in your own by implementing

```
public interface RpcDispatcher {

	Message dispatchMessage(Message message, Object serviceBean);
}
```

The `dispatchMessage` method will receive the request message and can decide how to call the target method on the service bean. You can inject your `RpcDispatcher` implementation using the `ProtobufServerFactoryBean.setRpcDispatcher(...)` method. Check out the default implementation in `SimpleRpcDispatcher`.

Similarly, on the client side you can implemenent the `RpcChannel` interface from protobuf as you wish. The default implementation here is `SimpleRpcChannel`.

## What if I want RPC but I find it ugly to extend the generated service class? ##

You can implement your service as a POJO instead. For example, instead of

```
	public void addPerson(RpcController controller,
			Person request, RpcCallback<AddressBookSize> done) {
         ...
	}
```

you can write

```
	public AddressBookSize addPerson(Person request) {
         ...
	}
```

and not extend `AddressBookService`.

## What if I don't want to use HTTP? ##

You can use whatever transport CXF supports, such as JMS. I am planning to add support for TCP sockets (if someone has not implemented it yet for CXF).

## Limitations ##
  * More features of `RpcController` could be supported
  * Raw (non HTTP) socket support would be nice