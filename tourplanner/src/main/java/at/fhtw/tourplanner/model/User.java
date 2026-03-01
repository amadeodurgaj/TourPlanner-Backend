package at.fhtw.tourplanner.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;
import java.util.Date;

@Getter
@Setter
public class User{
    private UUID id;
    private String username;
    private Date registrationDate;
    private String passwordHash;

}