package com.huellapositiva.application.dto.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.huellapositiva.application.dto.ProfileDto;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

public class DtoProfileDeserializer extends StdDeserializer<ProfileDto> {

    public DtoProfileDeserializer() {
        this(null);
    }

    public DtoProfileDeserializer(final Class<?> vc) {
        super(vc);
    }

    @Override
    public ProfileDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final JsonNode nodeName = node.get("name");
        final String name = nodeName.isNull() ? null : nodeName.asText();

        final JsonNode nodeSurname = node.get("surname");
        final String surname = nodeSurname.isNull() ? null : nodeSurname.asText();

        final JsonNode nodeBirthDate = node.get("birthDate");
        final String birthDate = nodeBirthDate.isNull() ? null : nodeBirthDate.asText();

        final JsonNode nodePhoneNumber = node.get("phoneNumber");
        final String phoneNumber = nodePhoneNumber.isNull() ? null : nodePhoneNumber.asText();

        final JsonNode nodeEmail = node.get("email");
        final String email = nodeEmail.isNull() ? null : nodeEmail.asText();

        final JsonNode nodeProvince = node.get("province");
        final String province = nodeProvince.isNull() ? null : nodeProvince.asText();

        final JsonNode nodeZipCode = node.get("zipCode");
        final String zipCode = nodeZipCode.isNull() ? null : nodeZipCode.asText();

        final JsonNode nodeTown = node.get("town");
        final String town = nodeTown.isNull() ? null : nodeTown.asText();

        final JsonNode nodeAddress = node.get("address");
        final String address = nodeAddress.isNull() ? null : nodeAddress.asText();

        final JsonNode nodeIsland = node.get("island");
        final String island = nodeIsland.isNull() ? null : nodeIsland.asText();

        final JsonNode nodeTwitter = node.get("twitter");
        final String twitter = nodeTwitter.isNull() ? null : nodeTwitter.asText();

        final JsonNode nodeInstagram = node.get("instagram");
        final String instagram = nodeInstagram.isNull() ? null : nodeInstagram.asText();

        final JsonNode nodeLinkedin = node.get("linkedin");
        final String linkedin = nodeLinkedin.isNull() ? null : nodeLinkedin.asText();

        final JsonNode nodeAdditionalInformation = node.get("additionalInformation");
        final String additionalInformation = nodeAdditionalInformation.isNull() ? null : nodeAdditionalInformation.asText();

        return ProfileDto.builder()
                .name(name)
                .surname(surname)
                .birthDate(parseToLocalDate(birthDate))
                .phoneNumber(phoneNumber)
                .email(email)
                .province(province)
                .zipCode(zipCode)
                .town(town)
                .address(address)
                .island(island)
                .twitter(twitter)
                .instagram(instagram)
                .linkedin(linkedin)
                .additionalInformation(additionalInformation)
                .build();
    }

    private LocalDate parseToLocalDate(String birthDate) {
        try {
            return LocalDate.parse(birthDate);
        } catch (DateTimeException ex) {
            return null;
        }
    }
}
