package fr.reservation.demo.service;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.reservation.demo.controller.DeliveryController;
import fr.reservation.demo.dto.ReservationRequestDTO;
import fr.reservation.demo.dto.ReservationResponseDTO;
import fr.reservation.demo.dto.TimeSlotDTO;
import fr.reservation.demo.exception.EntityNotFoundException;
import fr.reservation.demo.model.DeliveryMode;
import fr.reservation.demo.model.Reservation;
import fr.reservation.demo.model.TimeSlot;
import fr.reservation.demo.repository.ReservationRepository;
import fr.reservation.demo.repository.TimeSlotRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

	private final TimeSlotRepository timeSlotRepository;
	private final ReservationRepository reservationRepository;

	public List<TimeSlot> getAvailableTimeSlots(DeliveryMode deliveryMode) {
		return timeSlotRepository.findByDeliveryModeAndAvailableIsTrue(deliveryMode);
	}

	@Transactional
	public Reservation bookTimeSlot(ReservationRequestDTO request) {
		TimeSlot timeSlot = timeSlotRepository.findById(request.getTimeSlotId())
				.orElseThrow(() -> new IllegalArgumentException("TimeSlot not found"));

		if (!timeSlot.isAvailable()) {
			throw new IllegalStateException("TimeSlot is already booked");
		}

		timeSlot.setAvailable(false);
		timeSlotRepository.save(timeSlot);

		Reservation booking = new Reservation();
		booking.setCustomerId(request.getCustomerId());
		booking.setTimeSlot(timeSlot);

		return reservationRepository.save(booking);
	}
	
    public Page<TimeSlotDTO> getAvailableTimeSlots(DeliveryMode mode, LocalDate date, Pageable pageable) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        
        return timeSlotRepository.findAvailableTimeSlots(mode, start, end, pageable)
                .map(this::convertToDTO);
    }
	
	@Transactional
    public ReservationResponseDTO createReservation(ReservationRequestDTO request) {
        TimeSlot slot = timeSlotRepository.findById(request.getTimeSlotId())
                .orElseThrow(() -> new IllegalArgumentException("Time slot not found"));
        
        if (!slot.isAvailable()) {
            throw new IllegalStateException("Time slot is not available");
        }
        
        Reservation reservation = new Reservation();
        reservation.setCustomerId(request.getCustomerId());
        reservation.setDeliveryMode(request.getDeliveryMode());
        reservation.setTimeSlot(slot);
        reservation.setDeliveryAddress(request.getDeliveryAddress());
        reservation.setReservationDate(LocalDate.now());
        reservation.setStatus("CONFIRMED");
        
        slot.setReservedCount(slot.getReservedCount() + 1);
        if (slot.getReservedCount() >= slot.getMaxCapacity()) {
            slot.setAvailable(false);
        }
        
        timeSlotRepository.save(slot);
        Reservation savedReservation = reservationRepository.save(reservation);
        
        return convertToResponseDTO(savedReservation);
    }
	
	@Cacheable("deliveryModes")
	public List<DeliveryMode> getAllDeliveryModes() {
	    return Arrays.stream(DeliveryMode.values())
	            .filter(DeliveryMode::isActive)
	            .collect(Collectors.toList());
	}

    public ReservationResponseDTO getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Reservation not found with id: " + id));
        
        return convertToResponseDTO(reservation)
                .add(linkTo(methodOn(DeliveryController.class).getReservation(id)).withSelfRel())
                .add(linkTo(methodOn(DeliveryController.class).getAvailableTimeSlots(reservation.getDeliveryMode(), 
                    reservation.getTimeSlot().getDeliveryDate(), PageRequest.of(0, 10))).withRel("available-slots"));
    }
    

    public ReservationResponseDTO convertToResponseDTO(Reservation reservation) {
        return ReservationResponseDTO.builder()
                .id(reservation.getId())
                .customerId(reservation.getCustomerId())
                .deliveryMode(reservation.getDeliveryMode())
                .reservationDate(reservation.getReservationDate())
                .startTime(reservation.getTimeSlot().getStartTime())
                .endTime(reservation.getTimeSlot().getEndTime())
                .status(reservation.getStatus())
                .build();
    }
    
    private TimeSlotDTO convertToDTO(TimeSlot slot) {
        TimeSlotDTO dto = new TimeSlotDTO();
        dto.setId(slot.getId());
        dto.setDeliveryMode(slot.getDeliveryMode());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setAvailable(slot.isAvailable());
        return dto;
    }

	
	
}