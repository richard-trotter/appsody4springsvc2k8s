package inventory.jpa;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Inventory Repository
 */

@Repository("inventoryRepo")
@Transactional
public interface InventoryRepo extends CrudRepository<InventoryItem, Long> {

    // find one by id like /inventory/id/{id}
    Optional<InventoryItem> findById(long id);

    // find all by naming like /inventory/name/{name}
    List<InventoryItem> findByNameContaining(String name);

    // find all whose price is less than or equal to /inventory/price/{price}
    List<InventoryItem> findByPriceLessThanEqual(int price);
}