package ru.antinform.cds.metrics;

import com.codahale.metrics.ObjectNameFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

class JmxObjectNameFactory implements ObjectNameFactory {

	@Override
	public ObjectName createName(String type, String domain, String name) {
		try {
			if (name.contains("=")) {
				return new ObjectName(domain + ":component=" + name);
			} else {
				return new ObjectName(domain, "name", ObjectName.quote(name));
			}
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException(e);
		}
	}

}
