package ru.antinform.cds.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataServiceImpl;

@SuppressWarnings("WeakerAccess")
class TestContext implements AutoCloseable, TagDataServiceImpl.Context, SaveTest.Context {

	//TODO consider to use DI container

	Config mainConfig = ConfigFactory.load();
	Session session = createSession();
	TagDataService tagDataService = new TagDataServiceImpl(this);

	private Session createSession() {
		Config c = mainConfig.getConfig("cds.session");
		Cluster.Builder b = Cluster.builder();
		c.getStringList("contactPoints").forEach(b::addContactPoint);
		Cluster cluster = b.build();
		return cluster.connect(c.getString("keyspace"));
	}

	@Override
	public void close() {
		session.getCluster().close();
	}

	public Config mainConfig() { return mainConfig; }
	public Session session() { return session; }
	public TagDataService tagDataService() { return tagDataService; }

}
