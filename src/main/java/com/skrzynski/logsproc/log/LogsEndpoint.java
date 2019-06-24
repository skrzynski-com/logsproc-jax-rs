package com.skrzynski.logsproc.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.skrzynski.logsproc.log.LogConstants.ALERT;
import static com.skrzynski.logsproc.log.LogConstants.WITH_ALERT;
import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.OK;

@Component
@Path("/")
public class LogsEndpoint {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private LogsMapper logsMapper;

    @Autowired
    private LogsService logsService;

    @POST
    @Path("/logs")
    @Consumes({TEXT_PLAIN})
    public Response processLines(String lines) {
        Status responseStatus = OK;
        try {
            logsService.loadData(lines);
        } catch (RuntimeException e) {
            responseStatus = INTERNAL_SERVER_ERROR;
        }
        return Response.status(responseStatus).build();
    }

    @GET
    @Path("/logs")
    @Produces({APPLICATION_JSON})
    public List<LogEntry> getLogsWithOptionalAlertParam(@QueryParam(WITH_ALERT) Boolean withAlert) {
        return ofNullable(withAlert)
                .map(this::getLogsWithAlert)
                .orElse(logsMapper.getAll());
    }

    private List<LogEntry> getLogsWithAlert(boolean alert) {
        Map<String, Boolean> queryParams = new HashMap<>();
        queryParams.put(ALERT, alert);
        return logsMapper.getWithAlert(queryParams);
    }
}
