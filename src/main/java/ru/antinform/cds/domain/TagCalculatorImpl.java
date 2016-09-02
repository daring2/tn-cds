package ru.antinform.cds.domain;

import com.typesafe.config.Config;
import ru.antinform.cds.utils.BaseBean;
import java.util.List;

public class TagCalculatorImpl extends BaseBean implements TagCalculator {

	final boolean enabled = config.getBoolean("enabled");

	public TagCalculatorImpl(Context ctx) {
		super(ctx.mainConfig(), "cds.TagCalculator");
	}

	@Override
	public List<TagData> calculate(List<TagData> data) {
		if (!enabled)
			return data;
		return null;
	}

	public interface Context {
		Config mainConfig();
	}

}
