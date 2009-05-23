package com.googlecode.cxf.protobuf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.cxf.BusFactory;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.test.AbstractCXFTest;
import org.junit.Before;
import org.junit.BeforeClass;

import com.google.protobuf.Message;
import com.googlecode.cxf.protobuf.utils.CXFUtils;

public abstract class AbstractCXFProtobufTest extends AbstractCXFTest {

	@BeforeClass
	public static void checkBus() throws Exception {
		if (BusFactory.getDefaultBus(false) != null) {
			throw new Exception(
					"Bus was not null, check cleanup of previously run tests!");
		}
	}

	@Before
	public void setUpBus() throws Exception {
		super.setUpBus();

		CXFUtils.registerProtobufExtensionsOnBus(BusFactory.getDefaultBus());
	}

	protected byte[] toBytes(Message message) throws IOException {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		message.writeTo(buf);

		return buf.toByteArray();
	}

	protected boolean hasInterceptor(List<Interceptor> interceptors,
			Class<? extends Interceptor> c) {
		for (Interceptor<?> i : interceptors) {
			if (i.getClass().equals(c)) {
				return true;
			}
		}

		return false;
	}

	protected ProtobufServerFactoryBean createServer(String address,
			String transportId, Object serviceBean,
			Class<? extends Message> messageClass) {
		ProtobufServerFactoryBean serverFactoryBean = new ProtobufServerFactoryBean();
		serverFactoryBean.setAddress(address);
		serverFactoryBean.setTransportId(transportId);
		serverFactoryBean.setServiceBean(serviceBean);
		serverFactoryBean.setMessageClass(messageClass);
		Server server = serverFactoryBean.create();

		return serverFactoryBean;
	}
}
