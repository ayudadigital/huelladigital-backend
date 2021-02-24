package com.huellapositiva.domain.model.entities;

import com.huellapositiva.domain.model.valueobjects.EmailAddress;
import com.huellapositiva.domain.model.valueobjects.EntityType;
import com.huellapositiva.domain.model.valueobjects.Id;
import com.huellapositiva.domain.model.valueobjects.Location;
import com.huellapositiva.infrastructure.orm.entities.JpaESAL;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.net.URL;
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
    private URL logoUrl;
    private final String website;
    @NotEmpty
    private final boolean registeredEntity;
    @NotEmpty
    private final EntityType entityType;
    @NotEmpty
    private final boolean privacyPolicy;
    @NotEmpty
    private final boolean dataProtectionPolicy;
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

    @SneakyThrows
    public static ESAL fromJpa(JpaESAL esal) {
        return ESAL.builder()
                .id(new Id(esal.getId()))
                .name(esal.getName())
                .logoUrl(esal.getLogoUrl() != null ? new URL(esal.getLogoUrl()) : null)
                .website(esal.getWebsite())
                .description(esal.getDescription())
                .registeredEntity(esal.isRegisteredEntity())
                .entityType(EntityType.valueOf(esal.getEntityType()))
                .location(Location.builder().island(esal.getLocation().getIsland()).zipCode(esal.getLocation().getZipCode()).build())
                .privacyPolicy(esal.isPrivacyPolicy())
                .dataProtectionPolicy(esal.isDataProtectionPolicy())
                .build();
    }
}
