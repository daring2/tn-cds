package ru.antinform.cds.sandbox;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

public class CassandraSandbox {

	public static void main(String[] args) throws Exception {
		String address = "127.0.0.1";
//		String address = "192.168.100.144";
		Cluster.Builder clusterBuilder = Cluster.builder().addContactPoint(address);
		try (Cluster cluster = clusterBuilder.build()) {
			Session session = cluster.connect("test");
			new CassandraInsertTest(session, 1000, 1000).run();
		}
	}

}
