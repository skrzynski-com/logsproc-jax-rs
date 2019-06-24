package com.skrzynski.logsproc.log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
public class LogsEndpointTest {

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    LogsMapper logsMapper;

    @Autowired
    LogsService logsService;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    private static final String URL = "%s:%d%s";
    private static final String SERVER = "http://localhost";
    private static final String REST_LOGS = "/logs";
    private static final String REST_LOGS_WITH_ALERT_PARAM = "/logs?withAlert={alert}";
    private static final String ID_1 = "scsmbstgra";
    private static final String ID_2 = "scsmbstgrb";
    private static final String ID_3 = "scsmbstgrc";
    private static final String TYPE_APPLICATION_LOG = "APPLICATION_LOG";
    private static final String HOST_12345 = "12345";
    private static final String CHARSET_UTF_8 = "charset=UTF-8";

    @Before
    public void truncate() {
        logsMapper.truncate();
    }

    @Test
    public void processLinesEndpointMethodShouldProcessDataAndShouldStoreToDatabaseAndShouldReturn200HttpStatus() throws IOException, URISyntaxException {
        // given
        String address = format(URL, SERVER, port, REST_LOGS);
        String lines = new String(readAllBytes(get("src", "test", "resources", "logs.txt")));
        HttpHeaders headers = new HttpHeaders();
        headers.add(CONTENT_TYPE, format("%s;%s", TEXT_PLAIN_VALUE, CHARSET_UTF_8));
        HttpEntity<String> httpEntity = new HttpEntity<>(lines, headers);

        // when
        ResponseEntity<Void> responseEntity = restTemplate.exchange(new URI(address), POST, httpEntity, Void.class);

        // then
        assertThat(responseEntity).extracting(ResponseEntity::getStatusCode).isEqualTo(OK);

        LogEntry logEntry1 = logsMapper.getById(ID_1);
        checkLogEntryWithId1(logEntry1);

        LogEntry logEntry2 = logsMapper.getById(ID_2);
        checkLogEntryWithId2(logEntry2);

        LogEntry logEntry3 = logsMapper.getById(ID_3);
        checkLogEntryWithId3(logEntry3);
    }

    @Test
    public void getLogsWithAlertParamTrueShouldReturnLogEntriesWithAlertSetToTrue() {
        // given
        insertLogEntriesToDatabase();

        String address = format(URL, SERVER, port, REST_LOGS_WITH_ALERT_PARAM);
        HttpHeaders headers = new HttpHeaders();
        headers.add(ACCEPT, APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<List<LogEntry>> responseEntity = restTemplate.exchange(address, GET, httpEntity, new ParameterizedTypeReference<List<LogEntry>>() {
        }, true);

        // then
        assertThat(responseEntity).extracting(ResponseEntity::getStatusCode).isEqualTo(OK);

        List<LogEntry> entries = responseEntity.getBody();
        assertThat(entries).hasSize(2);

        Iterator<LogEntry> logEntries = entries.iterator();
        LogEntry logEntryFromRest1 = logEntries.next();
        checkLogEntryWithId1(logEntryFromRest1);

        LogEntry logEntryFromRest3 = logEntries.next();
        checkLogEntryWithId3(logEntryFromRest3);
    }

    @Test
    public void getLogsWithAlertParamFalseShouldReturnLogEntriesWithAlertSetToFalse() {
        // given
        insertLogEntriesToDatabase();

        String address = format(URL, SERVER, port, REST_LOGS_WITH_ALERT_PARAM);
        HttpHeaders headers = new HttpHeaders();
        headers.add(ACCEPT, APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<List<LogEntry>> responseEntity = restTemplate.exchange(address, GET, httpEntity, new ParameterizedTypeReference<List<LogEntry>>() {
        }, false);

        // then
        assertThat(responseEntity).extracting(ResponseEntity::getStatusCode).isEqualTo(OK);

        List<LogEntry> entries = responseEntity.getBody();
        assertThat(entries).hasSize(1);

        Iterator<LogEntry> logEntries = entries.iterator();
        LogEntry logEntryFromRest2 = logEntries.next();
        checkLogEntryWithId2(logEntryFromRest2);
    }

    @Test
    public void getLogsWithoutAlertParamShouldReturnAllLogEntries() {
        // given
        insertLogEntriesToDatabase();

        String address = format(URL, SERVER, port, REST_LOGS);
        HttpHeaders headers = new HttpHeaders();
        headers.add(ACCEPT, APPLICATION_JSON_UTF8_VALUE);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        // when
        ResponseEntity<List<LogEntry>> responseEntity = restTemplate.exchange(address, GET, httpEntity, new ParameterizedTypeReference<List<LogEntry>>() {
        });

        // then
        assertThat(responseEntity).extracting(ResponseEntity::getStatusCode).isEqualTo(OK);

        List<LogEntry> entries = responseEntity.getBody();
        assertThat(entries).hasSize(3);

        Iterator<LogEntry> logEntries = entries.iterator();
        LogEntry logEntryFromRest1 = logEntries.next();
        checkLogEntryWithId1(logEntryFromRest1);

        LogEntry logEntryFromRest2 = logEntries.next();
        checkLogEntryWithId2(logEntryFromRest2);

        LogEntry logEntryFromRest3 = logEntries.next();
        checkLogEntryWithId3(logEntryFromRest3);
    }

    private void insertLogEntriesToDatabase() {
        LogEntry logEntry1 = new LogEntry().setId(ID_1).setType(TYPE_APPLICATION_LOG).setHost(HOST_12345).setDuration(5L).setAlert(true);
        logsMapper.insert(logEntry1);
        LogEntry logEntry2 = new LogEntry().setId(ID_2).setDuration(3L);
        logsMapper.insert(logEntry2);
        LogEntry logEntry3 = new LogEntry().setId(ID_3).setDuration(8L).setAlert(true);
        logsMapper.insert(logEntry3);
    }

    private void checkLogEntryWithId1(LogEntry logEntry) {
        assertThat(logEntry).extracting(LogEntry::getType).isEqualTo(TYPE_APPLICATION_LOG);
        assertThat(logEntry).extracting(LogEntry::getHost).isEqualTo(HOST_12345);
        assertThat(logEntry).extracting(LogEntry::getDuration).isEqualTo(5L);
        assertThat(logEntry).extracting(LogEntry::isAlert).isEqualTo(true);
    }

    private void checkLogEntryWithId2(LogEntry logEntry) {
        assertThat(logEntry).extracting(LogEntry::getDuration).isEqualTo(3L);
        assertThat(logEntry).extracting(LogEntry::isAlert).isEqualTo(false);
    }

    private void checkLogEntryWithId3(LogEntry logEntry) {
        assertThat(logEntry).extracting(LogEntry::getDuration).isEqualTo(8L);
        assertThat(logEntry).extracting(LogEntry::isAlert).isEqualTo(true);
    }
}
