package ru.antinform.cds.domain;

import java.util.List;

public interface TagCalculator {

	List<TagData> calculate(List<TagData> data);

}
