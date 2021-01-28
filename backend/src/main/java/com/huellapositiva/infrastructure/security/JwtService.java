package com.huellapositiva.infrastructure.security;

import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.domain.model.valueobjects.Roles;
import com.huellapositiva.infrastructure.orm.entities.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.huellapositiva.domain.util.StringUtils.maskEmailAddress;
import static com.nimbusds.jose.JOSEObjectType.JWT;
import static com.nimbusds.jose.JWSAlgorithm.HS512;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtService {

    private static final String ROLE_CLAIM = "roles";

    private static final Map<String, Date> revokedAccessTokens = new HashMap<>();

    @Autowired
    private final JwtProperties jwtProperties;

    @Autowired
    private final JpaRoleRepository roleRepository;

    private JWSHeader signatureHeader;

    private JWSSigner jwsSigner;

    private JWSVerifier jwsVerifier;

    private DirectEncrypter encrypter;

    private DirectDecrypter decrypter;

    private JWEHeader encryptionHeader;

    @PostConstruct
    public void init() throws JOSEException {
        String signatureSecret = jwtProperties.getSignature().getSecret();
        log.debug("Signing key length: {}", signatureSecret.getBytes().length * 8);
        signatureHeader = new JWSHeader.Builder(HS512).type(JWT).build();
        jwsSigner = new MACSigner(signatureSecret);
        jwsVerifier = new MACVerifier(signatureSecret);
        String encryptionSecret = jwtProperties.getEncryption().getSecret();
        log.debug("Signing key length: {}", encryptionSecret.getBytes().length * 8);
        encrypter = new DirectEncrypter(encryptionSecret.getBytes());
        decrypter = new DirectDecrypter(jwtProperties.getEncryption().getSecret().getBytes());
        encryptionHeader = new JWEHeader.Builder(JWEAlgorithm.DIR, EncryptionMethod.A256GCM).contentType("JWT").build();
    }

    public JwtResponseDto refresh(String refreshToken) throws InvalidJwtTokenException {
        String accountId = getUserDetails(refreshToken).getFirst();
        return create(accountId);
    }

    public JwtResponseDto create(String accountId) {
        List<String> roles = roleRepository.findAllByAccountId(accountId).stream().map(Role::getName).collect(Collectors.toList());
        return create(accountId, roles);
    }

    public JwtResponseDto create(String accountId, List<String> roles) {
        revokeAccessTokens(accountId);
        String newAccessToken = createToken(accountId, roles, jwtProperties.getAccessToken().getExpirationTime());
        String newRefreshToken = createToken(accountId, Collections.emptyList(), jwtProperties.getRefreshToken().getExpirationTime());
        return new JwtResponseDto(newAccessToken, newRefreshToken, roles.stream().map(Roles::valueOf).collect(Collectors.toSet()));
    }

    @SuppressWarnings("unchecked")
    public Pair<String, List<String>> getUserDetails(String token) throws InvalidJwtTokenException {
        JWTClaimsSet claims = decodeToken(token);
        String accountId = claims.getSubject();
        List<String> roles = (List<String>) claims.getClaim(ROLE_CLAIM);
        return Pair.of(accountId, roles);
    }

    @SuppressWarnings("unchecked")
    private JWTClaimsSet decodeToken(String token) throws InvalidJwtTokenException {

        JWEObject jweObject;
        try {
            jweObject = JWEObject.parse(token);

            jweObject.decrypt(decrypter);
        } catch (ParseException | JOSEException e) {
            log.error("Failed to decrypt token: {}", token, e);
            throw new InvalidJwtTokenException("Unable to decrypt token: " + token, e);
        }

        SignedJWT signedJwt = jweObject.getPayload().toSignedJWT();

        JWTClaimsSet claims;
        try {
            if (!signedJwt.verify(jwsVerifier)) {
                throw new InvalidJwtTokenException("Token signature is invalid: " + token);
            }
            claims = signedJwt.getJWTClaimsSet();
        } catch (ParseException | IllegalStateException | JOSEException | InvalidJwtTokenException e) {
            log.error("Invalid token: {}", token, e);
            throw new InvalidJwtTokenException("Unable to decode token: " + token, e);
        }

        if (claims.getExpirationTime().before(new Date())) {
            throw new InvalidJwtTokenException("Token is expired: " + token);
        }

        boolean isRefresh = ((List<String>) claims.getClaim(ROLE_CLAIM)).isEmpty();
        if (!isRefresh && isRevoked(claims)) {
            throw new InvalidJwtTokenException("Token is revoked: " + token);
        }

        return claims;
    }

    private String createToken(String accountId, List<String> roles, long duration) {
        Instant issuedAt = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(accountId)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(issuedAt.plusMillis(duration)))
                .claim(ROLE_CLAIM, roles)
                .build();
        SignedJWT signedJWT = new SignedJWT(signatureHeader, claims);
        try {
            signedJWT.sign(jwsSigner);
        } catch (JOSEException e) {
            log.error("Unable to sign token: {}", signedJWT.toString());
            throw new IllegalStateException("Unable to create a token.", e);
        }

        JWEObject jweObject = new JWEObject(encryptionHeader, new Payload(signedJWT));
        try {
            jweObject.encrypt(encrypter);
        } catch (JOSEException e) {
            throw new IllegalArgumentException("Failed to encrypt token", e);
        }

        return jweObject.serialize();
    }

    public void revokeAccessTokens(String accountId) {
        Date issuedBefore = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        Date revokedBeforeDate = revokedAccessTokens.get(accountId);
        if (revokedBeforeDate == null || revokedBeforeDate.before(issuedBefore)) {
            revokedAccessTokens.put(accountId, issuedBefore);
            log.debug("Revoking access tokens for user {} at {}", maskEmailAddress(accountId), issuedBefore);
        }
    }

    private boolean isRevoked(JWTClaimsSet claims) {
        String accountId = claims.getSubject();
        Date issuedAt = claims.getIssueTime();
        Date revokedIssuedBefore = revokedAccessTokens.get(accountId);
        return revokedIssuedBefore != null && issuedAt.before(revokedIssuedBefore);
    }
}
