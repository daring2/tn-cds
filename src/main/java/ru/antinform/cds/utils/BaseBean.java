package ru.antinform.cds.utils;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

@SuppressWarnings("WeakerAccess")
public abstract class BaseBean {

	protected final Logger log = getLogger(getClass());

	protected final Config config;

	public BaseBean(Config config) {
		this.config = config;
	}

	public BaseBean(Config mainConfig, String path) {
		this(mainConfig.getConfig(path));
	}

}
