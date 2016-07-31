package ru.antinform.cds.domain;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;

public interface TagDataService {

	ListenableFuture<?> saveAll(List<TagData> data);

	List<TagData> findByPeriod(long start, long end);

	long selectCountByPeriod(long start, long end);

}
