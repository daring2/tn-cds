package ru.antinform.cds.domain;

import java.util.Date;

@SuppressWarnings("WeakerAccess")
public class TagData {

	public String tag;
	public Date time;
	public double value;
	public int quality;

	public TagData() {
	}

	public TagData(String tag, Date time, double value, int quality) {
		this.tag = tag;
		this.time = time;
		this.value = value;
		this.quality = quality;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	@Override
	public String toString() {
		return "TagData{" +
			"tag='" + tag + '\'' +
			", time=" + time +
			", value=" + value +
			", quality=" + quality +
			'}';
	}
}
