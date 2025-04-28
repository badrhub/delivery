package fr.reservation.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.reservation.demo.model.DeliveryMode;
import fr.reservation.demo.model.TimeSlot;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

	List<TimeSlot> findByDeliveryModeAndAvailableIsTrue(DeliveryMode deliveryMode);

	Page<TimeSlot> findByDeliveryModeAndStartTimeBetweenAndAvailableIsTrue(DeliveryMode deliveryMode,
			LocalDateTime start, LocalDateTime end, Pageable pageable);
	
    @Query("SELECT t FROM TimeSlot t WHERE t.deliveryMode = :mode AND t.available = true AND t.startTime >= :start AND t.endTime <= :end")
    Page<TimeSlot> findAvailableTimeSlots(
        @Param("mode") DeliveryMode mode,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable);
}
