package org.webtop.module.polarization;

public class VRMLEvent {
	private final String name;
	private final Filter filter;

	public VRMLEvent(String name_, Filter filter_) {
		name = name_;
		filter = filter_;
	}

	public String getName() {
		return name;
	}

	public Filter getFilter() {
		return filter;
	}
}
