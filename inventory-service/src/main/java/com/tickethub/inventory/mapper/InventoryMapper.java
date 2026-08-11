package com.tickethub.inventory.mapper;

import com.tickethub.inventory.dto.TicketReservationDTO;
import com.tickethub.inventory.dto.TicketInventoryResponse;
import com.tickethub.inventory.entity.TicketInventory;
import com.tickethub.inventory.entity.TicketReservation;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    TicketInventoryResponse toTicketInventoryResponseDTO(TicketInventory inventory);

    TicketReservationDTO toReservationResponse(TicketReservation reservation);
}
