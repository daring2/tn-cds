package ru.antinform.cds.domain;

import com.datastax.driver.core.Session;
import com.typesafe.config.Config;

public interface TagDataServiceContext {

	Config mainConfig();

	Session session();

}
