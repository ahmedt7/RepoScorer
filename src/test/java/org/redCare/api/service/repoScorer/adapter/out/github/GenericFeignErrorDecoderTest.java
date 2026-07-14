package org.redCare.api.service.repoScorer.adapter.out.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpHeaders.RETRY_AFTER;


import feign.Request;
import feign.Response;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.redCare.api.service.repoScorer.configuration.error.GenericFeignErrorDecoder;
import org.redCare.api.service.repoScorer.configuration.error.client.BadRequestException;
import org.redCare.api.service.repoScorer.configuration.error.client.ForbiddenException;
import org.redCare.api.service.repoScorer.configuration.error.client.GenericClientErrorException;
import org.redCare.api.service.repoScorer.configuration.error.client.NotFoundException;
import org.redCare.api.service.repoScorer.configuration.error.server.BadGatewayException;
import org.redCare.api.service.repoScorer.configuration.error.server.GatewayTimeoutException;
import org.redCare.api.service.repoScorer.configuration.error.server.InternalServerErrorException;
import org.redCare.api.service.repoScorer.configuration.error.server.NotImplementedException;
import org.redCare.api.service.repoScorer.configuration.error.server.ServiceUnavailableException;

class GenericFeignErrorDecoderTest {

    private final GenericFeignErrorDecoder decoder = new GenericFeignErrorDecoder();

    @Test
    void decodesClientErrorsWithMappedBodyMessages() {
        assertThatThrownBy(() -> decoder.decode("client#badRequest", response(400, """
                {"message":"invalid query"}
                """)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("invalid query");

        assertThatThrownBy(() -> decoder.decode("client#generic", response(418, """
                [{"message":"teapot"}]
                """)))
                .isInstanceOf(GenericClientErrorException.class)
                .hasMessageContaining("teapot");
    }

    @Test
    void decodesForbiddenAndNotFoundResponses() {
        assertThatThrownBy(() -> decoder.decode("client#forbidden", response(403, null)))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> decoder.decode("client#notFound", response(404, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void decodesServerErrors() {
        assertThatThrownBy(() -> decoder.decode("client#badGateway", response(500, null)))
                .isInstanceOf(BadGatewayException.class);
        assertThatThrownBy(() -> decoder.decode("client#badGateway", response(502, null)))
                .isInstanceOf(BadGatewayException.class);
        assertThatThrownBy(() -> decoder.decode("client#notImplemented", response(501, """
                {"message":"not implemented"}
                """)))
                .isInstanceOf(NotImplementedException.class)
                .hasMessageContaining("not implemented");
        assertThatThrownBy(() -> decoder.decode("client#timeout", response(504, null)))
                .isInstanceOf(GatewayTimeoutException.class);
    }

    @Test
    void decodesServiceUnavailableWithRetryAfterHeader() {
        assertThatThrownBy(() -> decoder.decode("client#unavailable", response(503, null, Map.of(RETRY_AFTER, List.of("30")))))
                .isInstanceOf(ServiceUnavailableException.class)
                .satisfies(throwable -> assertThat(((ServiceUnavailableException) throwable).getRetryAfter()).isEqualTo(30));
    }

    @Test
    void fallsBackToDefaultMessageWhenBodyCannotBeMapped() {
        assertThatThrownBy(() -> decoder.decode("client#notAcceptable", response(406, null)))
                .isInstanceOf(InternalServerErrorException.class)
                .hasMessageContaining("Not Acceptable");
    }

    private Response response(int status, String body) {
        return response(status, body, Map.of());
    }

    private Response response(int status, String body, Map<String, Collection<String>> headers) {
        Response.Builder builder = Response.builder()
                .status(status)
                .reason(status == 406 ? "Not Acceptable" : "reason")
                .request(Request.create(
                        Request.HttpMethod.GET,
                        "https://api.github.test/search/repositories",
                        Map.of(),
                        null,
                        StandardCharsets.UTF_8,
                        null))
                .headers(headers);
        if (body != null) {
            builder.body(body, StandardCharsets.UTF_8);
        }
        return builder.build();
    }
}
