package com.tickethub.inventory.mapper;

import com.tickethub.inventory.dto.ReservationResponse;
import com.tickethub.inventory.dto.TicketInventoryResponseDTO;
import com.tickethub.inventory.entity.TicketInventory;
import com.tickethub.inventory.entity.TicketReservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    TicketInventoryResponseDTO toTicketInventoryResponseDTO(TicketInventory inventory);

    ReservationResponse toReservationResponse(TicketReservation reservation);
}
