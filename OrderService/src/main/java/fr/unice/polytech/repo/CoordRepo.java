package fr.unice.polytech.repo;

import fr.unice.polytech.entities.Coord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface CoordRepo extends CrudRepository<Coord, Integer> {
}
