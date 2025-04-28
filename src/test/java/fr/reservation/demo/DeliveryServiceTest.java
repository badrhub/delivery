package fr.reservation.demo;  // Package cohérent avec les imports

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import fr.reservation.demo.dto.ReservationRequestDTO;
import fr.reservation.demo.dto.TimeSlotDTO;
import fr.reservation.demo.model.DeliveryMode;
import fr.reservation.demo.model.Reservation;
import fr.reservation.demo.model.TimeSlot;
import fr.reservation.demo.repository.ReservationRepository;
import fr.reservation.demo.repository.TimeSlotRepository;
import fr.reservation.demo.service.DeliveryService;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;
    
    @Mock
    private ReservationRepository reservationRepository;
    
    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void getAllDeliveryModes_ShouldReturnAllModes() {
        // Implémentation inchangée
    }

    @Test
    void getAvailableTimeSlots_ShouldReturnSlots() {
    	// Given
        TimeSlot slot = new TimeSlot();
        slot.setId(1L);
        slot.setDeliveryMode(DeliveryMode.DELIVERY);
        slot.setStartTime(LocalTime.now());
        slot.setEndTime(LocalTime.now().plusHours(2));
        slot.setAvailable(true);
        
        // Create a proper Page object
        Page<TimeSlot> mockPage = new PageImpl<>(
            List.of(slot), 
            PageRequest.of(0, 10), 
            1  // total elements
        );
        
        // Mock the repository call
        when(timeSlotRepository.findAvailableTimeSlots(
            any(DeliveryMode.class),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            any(Pageable.class))
        ).thenReturn(mockPage);
        
        // When
        Page<TimeSlotDTO> result = deliveryService.getAvailableTimeSlots(
            DeliveryMode.DELIVERY, 
            LocalDate.now(), 
            PageRequest.of(0, 10));
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getContent().get(0).getId());
    }

    @Test
    void bookTimeSlot_ShouldCreateReservation() {
        // Given
        TimeSlot slot = new TimeSlot();
        slot.setId(1L);
        slot.setAvailable(true);
        slot.setMaxCapacity(5);
        slot.setReservedCount(0);
        
        when(timeSlotRepository.findById(1L)).thenReturn(Optional.of(slot));
        when(reservationRepository.save(any())).thenAnswer(inv -> {
            Reservation r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });
        
        ReservationRequestDTO request = new ReservationRequestDTO();
        request.setCustomerId("cust-123");
        request.setDeliveryMode(DeliveryMode.DELIVERY);
        request.setTimeSlotId(1L);
        request.setDeliveryAddress("123 Main St");
        
        // When
        Reservation booking = deliveryService.bookTimeSlot(request);
        
        // Then
        assertNotNull(booking);
        assertEquals(100L, booking.getId());
        assertEquals("cust-123", booking.getCustomerId());
        
        verify(timeSlotRepository, times(1)).save(any());
        verify(reservationRepository, times(1)).save(any());
    }
}