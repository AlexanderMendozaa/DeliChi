package com.example.demorestaurant.entities;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "ceos")
public class Ceo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String name;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String first_surname;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String second_surname;

    @Column(nullable = false, unique = true, length = 255)
    @NotBlank
    @Email
    private String email;

    @Column(nullable = false, length = 255)
    @NotBlank
    private String password;

    @Column(nullable = false, unique = true)
    @NotBlank
    private Long phone_number;

    @OneToMany(mappedBy = "ceo")
    private List<Restaurant> restaurants;

}
