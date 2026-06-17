package app.neuland.office;

import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.Optional;

@Path("/api/v1/office")
@Produces(MediaType.APPLICATION_JSON)
public class OfficePresenceResource {

    @Inject
    MemberAuth memberAuth;

    @Inject
    OfficePresenceService service;

    @GET
    @Path("/presence")
    public PresenceStatusResponse getPresence(@Context HttpHeaders headers) {
        String sub = memberAuth.memberSub(headers);
        Optional<Instant> expiresAt = service.expiresAt(sub);
        return new PresenceStatusResponse(
                service.count(),
                expiresAt.isPresent(),
                expiresAt.map(Instant::toString).orElse(null)
        );
    }

    @POST
    @Path("/presence")
    public PresenceMutationResponse checkIn(@Context HttpHeaders headers) {
        Instant expiresAt = service.checkIn(memberAuth.memberSub(headers));
        return new PresenceMutationResponse(true, expiresAt.toString());
    }

    @DELETE
    @Path("/presence")
    public PresenceMutationResponse checkOut(@Context HttpHeaders headers) {
        service.checkOut(memberAuth.memberSub(headers));
        return new PresenceMutationResponse(false, null);
    }

    public record PresenceStatusResponse(int count, boolean registered, String expiresAt) {}

    public record PresenceMutationResponse(boolean registered, String expiresAt) {}
}
