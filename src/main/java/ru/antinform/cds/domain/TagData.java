package ru.antinform.cds.domain;

@SuppressWarnings("WeakerAccess")
public class TagData {

	public String tag;
	public long time;
	public double value;
	public int quality;

	public TagData() {
	}

	public TagData(String tag, long time, double value, int quality) {
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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
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
