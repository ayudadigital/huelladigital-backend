package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ESAL {
    @NotEmpty
    private final String name;
    @NotEmpty
    private final Id id;
    private final String description;
    private final String logoUrl;
    private final String webpage;
    @NotEmpty
    private final Boolean registeredEntity;
    @NotEmpty
    private final String entityType;
    @NotEmpty
    private final Boolean privacyPolicy;
    @NotEmpty
    private final Boolean dataProtectionPolicy;
    @NotEmpty
    private final Location location;

    private EmailAddress contactPersonEmail;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ESAL)) return false;
        ESAL esal = (ESAL) o;
        return id.equals(esal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public static ESAL parseJpa(JpaESAL esal){
        return ESAL.builder()
                .id(new Id(esal.getId()))
                .name(esal.getName())
                .logoUrl(esal.getLogoUrl())
                .webpage(esal.getWebpage())
                .description(esal.getDescription())
                .registeredEntity(esal.getRegisteredEntity())
                .entityType(esal.getEntityType())
                .location(Location.builder().island(esal.getLocation().getIsland()).zipCode(esal.getLocation().getZipCode()).build())
                .privacyPolicy(esal.getPrivacyPolicy())
                .dataProtectionPolicy(esal.getDataProtectionPolicy())
                .build();
    }
}
