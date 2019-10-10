package fr.unice.polytech.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import static fr.unice.polytech.entities.NotificationMedium.EMAIL;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String name;
    private String firstName;
    private NotificationMedium medium = EMAIL;

    public Customer() {
    }

    public Customer(String name, String firstName) {
        this.name = name;
        this.firstName = firstName;
    }
}
