package fr.unice.polytech.codemara.warehouse.entities;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class Warehouse {

   public static List<Order> getPendings() {
       return new ArrayList<>();
   }

}
