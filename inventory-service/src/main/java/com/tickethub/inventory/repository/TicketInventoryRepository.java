package com.tickethub.inventory.repository;

import com.tickethub.inventory.entity.TicketInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketInventoryRepository extends JpaRepository<TicketInventory, String> {

    Optional<TicketInventory> findByTicketTypeId(String ticketTypeId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE TicketInventory inventory
            SET inventory.availableQuantity = inventory.availableQuantity - :quantity,
                inventory.reservedQuantity = inventory.reservedQuantity + :quantity,
                inventory.version = inventory.version + 1
            WHERE inventory.ticketTypeId = :ticketTypeId
              AND inventory.availableQuantity >= :quantity
            """)
    int reserveTickets(@Param("ticketTypeId") String ticketTypeId, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE TicketInventory inventory
            SET inventory.reservedQuantity = inventory.reservedQuantity - :quantity,
                inventory.soldQuantity = inventory.soldQuantity + :quantity,
                inventory.version = inventory.version + 1
            WHERE inventory.ticketTypeId = :ticketTypeId
              AND inventory.reservedQuantity >= :quantity
            """)
    int confirmReservedTickets(@Param("ticketTypeId") String ticketTypeId, @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE TicketInventory inventory
            SET inventory.availableQuantity = inventory.availableQuantity + :quantity,
                inventory.reservedQuantity = inventory.reservedQuantity - :quantity,
                inventory.version = inventory.version + 1
            WHERE inventory.ticketTypeId = :ticketTypeId
              AND inventory.reservedQuantity >= :quantity
            """)
    int releaseReservedTickets(@Param("ticketTypeId") String ticketTypeId, @Param("quantity") int quantity);
}
