package fr.unice.polytech.codemara.warehouse.repositories;

import fr.unice.polytech.codemara.warehouse.entities.Parcel;
import fr.unice.polytech.codemara.warehouse.entities.dto.CustomerOrder;
import org.springframework.data.repository.CrudRepository;

public interface ParcelRepository extends CrudRepository<Parcel, Integer> {

}
