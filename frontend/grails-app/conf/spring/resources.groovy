import org.bazhenov.logging.transport.UdpTransport
import org.bazhenov.logging.marshalling.JDomMarshaller
import org.bazhenov.logging.transport.WriteToStorageTransportListener
import org.bazhenov.logging.storage.sql.*
import org.apache.commons.dbcp.BasicDataSource

beans = {

	datasource(BasicDataSource) {
		driverClassName = "org.h2.Driver"
		username = "sa"
		password = ""
		url = "jdbc:h2:./database/logging"
	}

	mapperRules(SqlMatcherMapperRules)

	matcherMapper(AnnotationDrivenMatcherMapperImpl, ref("mapperRules"))

	logStorage(SqlLogStorage, ref("datasource"), ref("marshaller"), ref("matcherMapper"))

	marshaller(JDomMarshaller)

	transportListener(WriteToStorageTransportListener, ref("logStorage"), ref("marshaller"))

	transport(UdpTransport, 6578, ref("transportListener")) { bean ->
		bean.initMethod = 'start'
		bean.destroyMethod = 'stop'
		bean.lazyInit = false

		bufferSize = 100000
	}

}

