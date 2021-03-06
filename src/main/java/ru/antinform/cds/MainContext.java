package ru.antinform.cds;

import com.datastax.driver.core.*;
import com.datastax.driver.extras.codecs.date.SimpleTimestampCodec;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ru.antinform.cds.cassandra.SessionMetrics;
import ru.antinform.cds.domain.TagCalculator;
import ru.antinform.cds.domain.TagCalculatorImpl;
import ru.antinform.cds.domain.TagDataService;
import ru.antinform.cds.domain.TagDataServiceImpl;
import ru.antinform.cds.metrics.MetricReporter;
import java.util.concurrent.ExecutorService;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SuppressWarnings("WeakerAccess")
public class MainContext implements AutoCloseable,
	TagCalculatorImpl.Context, TagDataServiceImpl.Context
{

	//TODO consider to use DI container

	Config mainConfig = ConfigFactory.load();
	ExecutorService executor = newCachedThreadPool();
	Session session = createSession();
	SessionMetrics sessionMetrics = new SessionMetrics(session);
	TagCalculator tagCalculator = new TagCalculatorImpl(this);
	TagDataService tagDataService = new TagDataServiceImpl(this);
	MetricReporter metricReporter = new MetricReporter(mainConfig);

	private Session createSession() {
		Config c = mainConfig.getConfig("cds.session");
		System.setProperty("com.datastax.driver.NON_BLOCKING_EXECUTOR_SIZE", c.getString("executorSize"));
		Cluster.Builder b = Cluster.builder();
		c.getStringList("contactPoints").forEach(b::addContactPoint);
		b.withAuthProvider(new PlainTextAuthProvider(c.getString("user"), c.getString("password")));
		b.withSocketOptions(new SocketOptions().
			setReadTimeoutMillis((int) c.getDuration("readTimeout", MILLISECONDS))
		);
		int connectionsPerHost = c.getInt("connectionsPerHost");
		b.withPoolingOptions(new PoolingOptions().
			setConnectionsPerHost(HostDistance.LOCAL, connectionsPerHost, connectionsPerHost).
			setMaxRequestsPerConnection(HostDistance.LOCAL, c.getInt("maxRequestsPerConnection"))
		);
		b.withQueryOptions(new QueryOptions().
			setDefaultIdempotence(true).
			setFetchSize(c.getInt("fetchSize"))
		);
		Cluster cluster = b.build();
		CodecRegistry codecs = cluster.getConfiguration().getCodecRegistry();
		codecs.register(new SimpleTimestampCodec());
		return cluster.connect(c.getString("keyspace"));
	}

	@Override
	public void close() throws Exception {
		metricReporter.close();
		session.getCluster().close();
		executor.shutdown();
	}

	public Config mainConfig() { return mainConfig; }
	public ExecutorService executor() { return executor; }
	public Session session() { return session; }
	public TagCalculator tagCalculator() { return tagCalculator; }
	public TagDataService tagDataService() { return tagDataService; }

}
