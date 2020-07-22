package com.huellapositiva.domain;

import com.huellapositiva.infrastructure.orm.model.OrganizationMember;

public class ExpressRegistrationOrganization {
    private final String name;
    private final OrganizationMember organizationMember;

    public ExpressRegistrationOrganization(String name, OrganizationMember organizationMember) {
        this.name = name;
        this.organizationMember = organizationMember;
    }

    public String getName() {
        return this.name;
    }

    public OrganizationMember getOrganizationMember() {
        return this.organizationMember;
    }
}
