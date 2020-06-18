package com.huellapositiva.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.huellapositiva.application.dto.JwtResponseDto;
import com.huellapositiva.application.exception.InvalidJwtTokenException;
import com.huellapositiva.infrastructure.orm.model.Role;
import com.huellapositiva.infrastructure.orm.repository.JpaRoleRepository;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Slf4j
@AllArgsConstructor
@Component
public class JwtService {

    private static final String ROLE_CLAIM = "roles";

    private static final Map<String, Date> revokedAccessTokens = new HashMap<>();

    @Autowired
    private final JwtProperties jwtProperties;

    @Autowired
    private final JpaRoleRepository roleRepository;

    public JwtResponseDto refresh(String refreshToken) throws InvalidJwtTokenException {
        String username = getUserDetails(refreshToken).getFirst();
        List<String> roles = roleRepository.findAllByEmailAddress(username).stream().map(Role::getName).collect(Collectors.toList());

        return create(username, roles);
    }

    public JwtResponseDto create(String username, List<String> roles) {
        revokeAccessTokens(username);
        String newAccessToken = createToken(username, roles, jwtProperties.getAccessToken().getExpirationTime());
        String newRefreshToken = createToken(username, Collections.emptyList(), jwtProperties.getRefreshToken().getExpirationTime());
        try {

            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(2048);

            // generate the key pair
            KeyPair keyPair = keyPairGenerator.genKeyPair();

            // create KeyFactory and RSA Keys Specs
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKeySpec publicKeySpec = keyFactory.getKeySpec(keyPair.getPublic(), RSAPublicKeySpec.class);
            RSAPrivateKeySpec privateKeySpec = keyFactory.getKeySpec(keyPair.getPrivate(), RSAPrivateKeySpec.class);

            // generate (and retrieve) RSA Keys from the KeyFactory using Keys Specs
            RSAPublicKey publicRsaKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
            RSAPrivateKey privateRsaKey = (RSAPrivateKey) keyFactory.generatePrivate(privateKeySpec);


            JWTClaimsSet.Builder jwtClaimsSet = new JWTClaimsSet.Builder();
            jwtClaimsSet.issuer("https://my-auth-server.com");
            jwtClaimsSet.subject("John Kerr");
            jwtClaimsSet.expirationTime(new Date(new Date().getTime() + 1000*60*10));
            jwtClaimsSet.notBeforeTime(new Date());
            jwtClaimsSet.jwtID(UUID.randomUUID().toString());

            EncryptedJWT encryptedJWT = new EncryptedJWT(new JWEHeader(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A128CBC_HS256),
                    jwtClaimsSet.build());

            String beforeEncryption = encryptedJWT.getParsedString();

            RSAEncrypter encrypter = new RSAEncrypter(publicRsaKey);
            encryptedJWT.encrypt(encrypter);

            String afterEncryption = encryptedJWT.getParsedString();

            String jwtString = encryptedJWT.serialize();


            RSADecrypter decrypter = new RSADecrypter(privateRsaKey);
            EncryptedJWT decryptedJwt = EncryptedJWT.parse(jwtString);


            String beforeDecryption = decryptedJwt.getParsedString();
            decryptedJwt.decrypt(decrypter);
            String afterDecryption = decryptedJwt.getParsedString();


        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JwtResponseDto(newAccessToken, newRefreshToken);
    }

    public Pair<String, List<String>> getUserDetails(String token) throws InvalidJwtTokenException {
        DecodedJWT decodedRefreshToken = decodeToken(token);
        String username = decodedRefreshToken.getSubject();
        List<String> roles = decodedRefreshToken.getClaim(ROLE_CLAIM).asList(String.class);
        return Pair.of(username, roles);
    }

    private DecodedJWT decodeToken(String token) throws InvalidJwtTokenException {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.require(Algorithm.HMAC512(jwtProperties.getSecret().getBytes())).build().verify(token);
        } catch (TokenExpiredException e) {
            throw new InvalidJwtTokenException("Token is expired: " + token, e);
        } catch (Exception e) {
            log.warn("Invalid token: {}", token, e);
            throw new InvalidJwtTokenException("Unable to decode token: " + token, e);
        }

        if (isRevoked(decodedJWT)) {
            log.warn("Token is revoked: {}, {}, {}", decodedJWT.getSubject(), decodedJWT.getIssuedAt(), revokedAccessTokens.get(decodedJWT.getSubject()));
            throw new InvalidJwtTokenException("Token is revoked: " + token);
        }
        return decodedJWT;
    }

    private String createToken(String username, List<String> roles, long duration) {
        Instant issuedAt = Instant.now();
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(Date.from(issuedAt))
                .withExpiresAt(Date.from(issuedAt.plusMillis(duration)))
                .withArrayClaim(ROLE_CLAIM, roles.toArray(new String[0]))
                .sign(HMAC512(jwtProperties.getSecret().getBytes()));
    }

    public void revokeAccessTokens(String username) {
        Date issuedBefore = Date.from(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        Date revokedBeforeDate = revokedAccessTokens.get(username);
        if (revokedBeforeDate == null || revokedBeforeDate.before(issuedBefore)) {
            revokedAccessTokens.put(username, issuedBefore);
        }
    }

    private boolean isRevoked(DecodedJWT decodedJWT) {
        String username = decodedJWT.getSubject();
        Date issuedAt = decodedJWT.getIssuedAt();
        Date revokedIssuedBefore = revokedAccessTokens.get(username);
        return revokedIssuedBefore != null && issuedAt.before(revokedIssuedBefore);
    }
}
