package com.obs.microservices.entity;

import javax.persistence.*;

@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @OneToOne
    private User user;

    public Long getId() {
        return id;
    }

    public Profile setId(Long id) {
        this.id = id;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public Profile setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public Profile setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public User getUser() {
        return user;
    }

    public Profile setUser(User user) {
        this.user = user;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (id != null ? !id.equals(profile.id) : profile.id != null) return false;
        if (firstName != null ? !firstName.equals(profile.firstName) : profile.firstName != null) return false;
        if (lastName != null ? !lastName.equals(profile.lastName) : profile.lastName != null) return false;
        return user != null ? user.equals(profile.user) : profile.user == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        return result;
    }
}
