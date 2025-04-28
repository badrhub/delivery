package fr.reservation.demo.dto;

import fr.reservation.demo.model.DeliveryMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReservationRequestDTO {
    @NotNull(message = "L'ID du créneau ne peut pas être nul")
    private Long timeSlotId;
    
    @NotBlank(message = "L'ID client ne peut pas être vide")
    @Size(min = 5, max = 50, message = "L'ID client doit contenir entre 5 et 50 caractères")
    private String customerId;
    
    @NotBlank(message = "L'adresse de livraison ne peut pas être vide")
    private String deliveryAddress;
    
    @NotNull(message = "Le mode de livraison ne peut pas être nul")
    private DeliveryMode deliveryMode;
}