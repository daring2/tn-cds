package ru.antinform.cds.cassandra;

import com.datastax.driver.core.Session;
import ru.antinform.cds.metrics.MetricBuilder;

public class SessionMetrics {

	private final Session session;

	public SessionMetrics(Session session) {
		this.session = session;
		MetricBuilder mb = new MetricBuilder("Session");
		mb.gauge("connections", this::getConnections);
		mb.gauge("inFlightQueries", this::getInFlightQueries);
	}

	private int getConnections() {
		Session.State state = session.getState();
		return state.getConnectedHosts().stream().mapToInt(state::getOpenConnections).sum();
	}

	private int getInFlightQueries() {
		Session.State state = session.getState();
		return state.getConnectedHosts().stream().mapToInt(state::getInFlightQueries).sum();
	}

}
