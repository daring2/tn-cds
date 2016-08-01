package ru.antinform.cds.domain;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.stream.Stream;

public interface TagDataService {

	ListenableFuture<?> saveAll(List<TagData> data);

	Stream<TagData> findByPeriod(long start, long end);

	long selectCountByPeriod(long start, long end);

}
