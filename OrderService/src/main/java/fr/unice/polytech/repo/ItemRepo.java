package fr.unice.polytech.repo;

import fr.unice.polytech.entities.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepo extends CrudRepository<Item, Integer> {

    Item findItemById(Integer id);

}
