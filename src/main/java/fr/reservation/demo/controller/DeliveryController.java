package fr.reservation.demo.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PageableDefault;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import fr.reservation.demo.dto.ReservationRequestDTO;
import fr.reservation.demo.dto.ReservationResponseDTO;
import fr.reservation.demo.dto.TimeSlotDTO;
import fr.reservation.demo.model.DeliveryMode;
import fr.reservation.demo.service.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/delivery")
@Tag(name = "Delivery API", description = "Gestion des livraisons")
@RequiredArgsConstructor
public class DeliveryController {

	private final DeliveryService deliveryService;
	private final PagedResourcesAssembler<TimeSlotDTO> pagedResourcesAssembler;

	@Operation(summary = "Liste tous les modes de livraison")
	@GetMapping("/modes")
	public ResponseEntity<CollectionModel<EntityModel<DeliveryMode>>> getAllDeliveryModes() {
		List<DeliveryMode> deliveryModes = deliveryService.getAllDeliveryModes();

		List<EntityModel<DeliveryMode>> modeResources = deliveryModes.stream().map(mode -> {
			EntityModel<DeliveryMode> resource = EntityModel.of(mode);
			resource.add(linkTo(methodOn(DeliveryController.class).getAvailableTimeSlots(mode, LocalDate.now(),
					PageRequest.of(0, 10))).withRel("slots"));
			resource.add(linkTo(methodOn(DeliveryController.class).getAllDeliveryModes()).withSelfRel());
			return resource;
		}).collect(Collectors.toList());

		CollectionModel<EntityModel<DeliveryMode>> collectionModel = CollectionModel.of(modeResources);
		collectionModel.add(linkTo(methodOn(DeliveryController.class).getAllDeliveryModes()).withSelfRel());

		return ResponseEntity.ok(collectionModel);
	}

	@Operation(summary = "Trouve les créneaux disponibles")
	@GetMapping("/slots")
	public ResponseEntity<PagedModel<EntityModel<TimeSlotDTO>>> getAvailableTimeSlots(
	        @RequestParam DeliveryMode mode,
	        @RequestParam(required = false) LocalDate date,
	        @PageableDefault(size = 10) Pageable pageable) {

		Page<TimeSlotDTO> slots = deliveryService.getAvailableTimeSlots(mode, date, pageable);

		PagedModel<EntityModel<TimeSlotDTO>> model = pagedResourcesAssembler.toModel(slots,
				slot -> EntityModel.of(slot,
						linkTo(methodOn(DeliveryController.class).getAvailableTimeSlots(mode, date, pageable))
								.withSelfRel(),
						linkTo(methodOn(DeliveryController.class).createReservation(new ReservationRequestDTO()))
								.withRel("reserve")));

		return ResponseEntity.ok(model);
	}

	@Operation(summary = "Récupère une réservation par son ID")
	@GetMapping("/reservations/{id}")
	public ResponseEntity<ReservationResponseDTO> getReservation(
			@Parameter(description = "ID de la réservation") @PathVariable Long id) {
		ReservationResponseDTO response = deliveryService.getReservationById(id);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Créer une nouvelle réservation")
	@PostMapping("/reservations")
	public ResponseEntity<ReservationResponseDTO> createReservation(@Valid @RequestBody ReservationRequestDTO request) {
		ReservationResponseDTO response = deliveryService.createReservation(request);
		return ResponseEntity
				.created(linkTo(methodOn(DeliveryController.class).getReservation(response.getId())).toUri())
				.body(response);
	}

}
