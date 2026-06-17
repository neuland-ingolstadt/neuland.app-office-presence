package app.neuland.office;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;

import java.util.List;

@ApplicationScoped
public class MemberAuth {

    private static final String MEMBER_GROUP = "mitglieder";

    @ConfigProperty(name = "neuland.authentik.issuer")
    String issuer;

    @ConfigProperty(name = "neuland.authentik.client-id")
    String clientId;

    private JwtConsumer jwtConsumer;

    @PostConstruct
    void init() {
        String jwksUrl = issuer.endsWith("/") ? issuer + "jwks/" : issuer + "/jwks/";
        jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(60)
                .setExpectedIssuer(issuer)
                .setExpectedAudience(clientId)
                .setVerificationKeyResolver(
                        new HttpsJwksVerificationKeyResolver(new HttpsJwks(jwksUrl))
                )
                .build();
    }

    public String memberSub(HttpHeaders headers) {
        String auth = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (auth == null || !auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw unauthorized("Missing bearer token");
        }

        try {
            JwtClaims claims = jwtConsumer.processToClaims(auth.substring(7).trim());
            List<String> groups = claims.getStringListClaimValue("groups");
            if (groups == null || !groups.contains(MEMBER_GROUP)) {
                throw unauthorized("Not a member");
            }

            return claims.getSubject();
        } catch (Exception e) {
            throw unauthorized("Invalid token");
        }
    }

    private static WebApplicationException unauthorized(String message) {
        return new WebApplicationException(
                Response.status(Response.Status.UNAUTHORIZED)
                        .type(MediaType.TEXT_PLAIN)
                        .entity(message)
                        .build()
        );
    }
}