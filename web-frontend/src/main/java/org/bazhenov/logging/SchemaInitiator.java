package org.bazhenov.logging;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

import static org.bazhenov.logging.storage.sql.SqlLogStorage.loadDump;

/**
 * Вспомогательный класс для spring, который позволят при запуске контейнера инициализировать
 * схему данных из заданного дампа.
 * <p/>
 * Пример конфигурирования посредством spring:
 * <pre>
 * &lt;bean class="org.bazhenov.logging.SchemaInitiator" lazy-init="false">
 *   &lt;property name="datasource" ref="datasource" //>
 *   &lt;property name="dump" value="classpath:/dump-init.mysql.sql" //>
 * &lt;/bean>
 * </pre>
 * Этот класс написан специально для использования в контексте spring, поэтому он использует
 * setter injection и имплементирует интерфейс {@link InitializingBean}, для того чтобы
 * не указывать <code>init-method</code> в конфигурации контейнера. Тем не менее,
 * указание <code>lazy-init="false"</code> все же требуется, так как по-умолчанию spring
 * создает объекты в контейнере только по требованию.
 * <p/>
 * Учтите так же то, что dump будет заливатся при каждом старте приложения. Это значит что в dump'е
 * должны находится условные инструкции создания таблиц и индексов (<code>IF EXISTS</code>) для того
 * чтобы повторная заливка dump'а не приводила к ошибке.
 */
public class SchemaInitiator implements InitializingBean {

	private DataSource datasource;
	private Resource dump;

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}

	public void setDump(Resource dump) {
		this.dump = dump;
	}

	public void afterPropertiesSet() throws Exception {
		loadDump(datasource, dump.getInputStream());
	}
}
