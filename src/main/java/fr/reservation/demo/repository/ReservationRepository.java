package fr.reservation.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.reservation.demo.model.DeliveryMode;
import fr.reservation.demo.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
	
	@Query("SELECT DISTINCT r.deliveryMode FROM Reservation r WHERE r.status = 'ACTIVE'")
	List<DeliveryMode> findActiveDeliveryModes();

	// 1. Basic paged search
	Page<Reservation> findAll(Pageable pageable);

	// 2. Find reservations by delivery method (paginated)
	Page<Reservation> findByDeliveryMode(DeliveryMode deliveryMode, Pageable pageable);

	// 3. Find reservations by customer (sorted by descending date)
	List<Reservation> findByCustomerIdOrderByReservationDateDesc(String customerId);

	// 4. Search between two dates (for reports)
	@Query("SELECT r FROM Reservation r WHERE r.reservationDate BETWEEN :start AND :end")
	List<Reservation> findBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	// 5. Reservations for a specific time slot
	@Query("SELECT r FROM Reservation r WHERE r.timeSlot.id = :timeSlotId")
	List<Reservation> findByTimeSlotId(@Param("timeSlotId") Long timeSlotId);

	// 6. Availability check (for reservation creation)
	@Query("SELECT COUNT(r) = 0 FROM Reservation r WHERE r.timeSlot.id = :timeSlotId")
	boolean isTimeSlotAvailable(@Param("timeSlotId") Long timeSlotId);

	// 7. Statistics by delivery method (for the dashboard)
	@Query("SELECT r.deliveryMode, COUNT(r) FROM Reservation r GROUP BY r.deliveryMode")
	List<Object[]> countByDeliveryMode();

	// 8. Method optimized for the HATEOAS endpoint
	@Query("SELECT r FROM Reservation r JOIN FETCH r.timeSlot WHERE r.id = :id")
	Reservation findWithTimeSlot(@Param("id") Long id);
	
	
    @EntityGraph(attributePaths = {"timeSlot"})
    Optional<Reservation> findWithTimeSlotById(Long id);
   

}
