package fr.reservation.demo.dto;


import lombok.Data;

import java.time.LocalTime;

import fr.reservation.demo.model.DeliveryMode;

@Data
public class TimeSlotDTO {
    private Long id;
    private DeliveryMode deliveryMode;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean available;
    private int maxCapacity;
    private int reservedCount;
}
