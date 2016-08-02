package ru.antinform.cds;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.Session;
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataServiceImpl;
import ru.antinform.cds.metrics.MetricReporter;

@SuppressWarnings("WeakerAccess")
public class MainContext implements AutoCloseable, TagDataServiceImpl.Context {

	//TODO consider to use DI container

	Config mainConfig = ConfigFactory.load();
	Session session = createSession();
	TagDataService tagDataService = new TagDataServiceImpl(this);
	MetricReporter metricReporter = new MetricReporter(mainConfig);

	private Session createSession() {
		Config c = mainConfig.getConfig("cds.session");
		Cluster.Builder b = Cluster.builder();
		c.getStringList("contactPoints").forEach(b::addContactPoint);
		Cluster cluster = b.build();
		CodecRegistry codecs = cluster.getConfiguration().getCodecRegistry();
		codecs.register(new SimpleTimestampCodec());
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