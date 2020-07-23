package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import lombok.Getter;

@Getter
public class Organization {

    private final String name;
    private User member;

    public Organization(String name) {
        this.name = name;
    }

    /** The user is required to be already registered
     *
     * @param member
     */
    public void addUserAsMember(User member) {
        this.member = member;
    }

    public EmailAddress getEmail() {
        return this.member.getEmailAddress();
    }
}
