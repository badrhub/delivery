package fr.reservation.demo.model;

public enum DeliveryMode {
	DRIVE(true),
	DELIVERY(true),
	DELIVERY_TODAY(true),
	DELIVERY_ASAP(true);

	private final boolean active;

	DeliveryMode(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}
}
