package ru.antinform.cds.domain;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Date;
import java.util.List;

public interface TagDataService {

	ListenableFuture<?> saveAll(List<TagData> data);

	List<TagData> findByPeriod(Date start, Date end);

	long selectCountByPeriod(Date start, Date end);

}
