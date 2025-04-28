package fr.reservation.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.hateoas.RepresentationModel;

import fr.reservation.demo.model.DeliveryMode;

import java.time.LocalDate;
import java.time.LocalTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ReservationResponseDTO extends RepresentationModel<ReservationResponseDTO> {
	private Long id;
	private String customerId;
	private DeliveryMode deliveryMode;
	private LocalDate reservationDate;
	private String status;
	private String deliveryAddress;
	private TimeSlotDTO timeSlot;
	private LocalTime startTime; // Début du créneau réservé
	private LocalTime endTime;

	public boolean isConfirmed() {
		return "CONFIRMED".equalsIgnoreCase(status);
	}

	public boolean isCancellable() {
		return isConfirmed() && reservationDate.isAfter(LocalDate.now().plusDays(1));
	}
}