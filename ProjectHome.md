# What is it? #

cxf-protobuf is a simple integration of Google's [Protocol Buffers](http://code.google.com/apis/protocolbuffers/) and [Apache CXF](http://cxf.apache.org/), an open source services framework for Java. The main features are

  * **Simple API to publish both RPC and non-RPC style services using CXF's transports**
  * **Simple client API to send messages to protocol buffer services using CXF's transport infrastructure**
  * **Simple configuration in Spring, integrated with CXF's configuration**
  * **?WSDL-style description generation for protocol buffer services (idea adopted from the SOAP world)**

cxf-protobuf is implemented as an extension to the CXF framework and makes use of CXF's standard (Spring-based) extension mechanisms.

See [User's Guide](http://code.google.com/p/cxf-protobuf/wiki/UsersGuide) for documentation with examples.