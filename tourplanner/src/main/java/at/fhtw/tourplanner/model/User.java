package at.fhtw.tourplanner.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
        private UUID id;
        private String username;
        private Date registrationDate;
        private String passwordHash;
        private String email;

}