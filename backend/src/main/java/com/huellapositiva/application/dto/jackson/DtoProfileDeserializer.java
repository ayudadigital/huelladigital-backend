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

    /**
     * Custom deserializer done for deserialize the date
     *
     * @param jsonParser    The JSON that arrives from the frontend.
     * @param deserializationContext    Context for the process of deserialization a single root-level value. Used to allow passing in configuration settings and reusable temporary objects (scrap arrays, containers).
     */
    @Override
    public ProfileDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        final JsonNode nodeName = node.get("name");
        final String name = returnValue(nodeName);

        final JsonNode nodeSurname = node.get("surname");
        final String surname = returnValue(nodeSurname);

        final JsonNode nodeBirthDate = node.get("birthDate");
        final String birthDate = returnValue(nodeBirthDate);

        final JsonNode nodePhoneNumber = node.get("phoneNumber");
        final String phoneNumber = returnValue(nodePhoneNumber);

        final JsonNode nodeEmail = node.get("email");
        final String email = returnValue(nodeEmail);

        final JsonNode nodeProvince = node.get("province");
        final String province = returnValue(nodeProvince);

        final JsonNode nodeZipCode = node.get("zipCode");
        final String zipCode = returnValue(nodeZipCode);

        final JsonNode nodeTown = node.get("town");
        final String town = returnValue(nodeTown);

        final JsonNode nodeAddress = node.get("address");
        final String address = returnValue(nodeAddress);

        final JsonNode nodeIsland = node.get("island");
        final String island = returnValue(nodeIsland);

        final JsonNode nodePhoto = node.get("photo");
        final String photo = returnValue(nodePhoto);

        final JsonNode nodeCurriculumVitae = node.get("curriculumVitae");
        final String curriculum = returnValue(nodeCurriculumVitae);

        final JsonNode nodeTwitter = node.get("twitter");
        final String twitter = returnValue(nodeTwitter);

        final JsonNode nodeInstagram = node.get("instagram");
        final String instagram = returnValue(nodeInstagram);

        final JsonNode nodeLinkedin = node.get("linkedin");
        final String linkedin = returnValue(nodeLinkedin);

        final JsonNode nodeAdditionalInformation = node.get("additionalInformation");
        final String additionalInformation = returnValue(nodeAdditionalInformation);

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
                .photo(photo)
                .curriculumVitae(curriculum)
                .twitter(twitter)
                .instagram(instagram)
                .linkedin(linkedin)
                .additionalInformation(additionalInformation)
                .build();
    }

    private String returnValue(JsonNode node) {
        return node.isNull() ? null : node.asText();
    }

    /**
     * Parse a date as a string to LocalDate
     *
     * @param birthDate The birth date with format YYYY-mm-dd
     */
    private LocalDate parseToLocalDate(String birthDate) {
        try {
            return LocalDate.parse(birthDate);
        } catch (DateTimeException ex) {
            return null;
        }
    }
}
