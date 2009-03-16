import org.bazhenov.logging.transport.UdpTransport
import org.bazhenov.logging.storage.InMemoryLogStorage
import org.bazhenov.logging.marshalling.JDomMarshaller
import org.bazhenov.logging.transport.WriteToStorageTransportListener

beans = {

	logStorage(InMemoryLogStorage)

	marshaller(JDomMarshaller)

	transportListener(WriteToStorageTransportListener, ref("logStorage"), ref("marshaller"))

	transport(UdpTransport, 6578, ref("transportListener")) { bean ->
		bean.initMethod = 'start'
	}

}

