package com.huellapositiva.domain;

import com.huellapositiva.infrastructure.orm.model.OrganizationEmployee;

public class ExpressRegistrationOrganization {
    private final String name;
    private final OrganizationEmployee employee;

    public ExpressRegistrationOrganization(String name, OrganizationEmployee employee) {
        this.name = name;
        this.employee = employee;
    }

    public String getName() {
        return this.name;
    }

    public OrganizationEmployee getEmployee() {
        return this.employee;
    }
}
