package ru.antinform.cds.test;

import com.typesafe.config.Config;
import ru.antinform.cds.MainContext;
import java.util.Set;
import java.util.concurrent.Future;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.util.concurrent.Futures.immediateFuture;

@SuppressWarnings("WeakerAccess")
class TestContext extends MainContext
	implements SaveTagDataTest.Context, QueryTagDataTest.Context, TagDataServiceTest.Context
{

	Config testConfig = mainConfig().getConfig("cds.test");
	Set<String> enabledTests = newHashSet(testConfig.getString("enabledTests").split(","));

	Future<?> startQueryTest(String name) {
		if (enabledTests.contains(name)) {
			return new QueryTagDataTest(this, name).start();
		} else {
			return immediateFuture("");
		}
	}

}
