package com.skrzynski.logsproc.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class LogsService {

    private final LogsMapper logsMapper;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = getLogger(LogsService.class);

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public LogsService(LogsMapper logsMapper, ObjectMapper objectMapper) {
        this.logsMapper = logsMapper;
        this.objectMapper = objectMapper;
    }

    public void loadData(String lines) {
        if (LOGGER.isDebugEnabled() && lines != null) {
            LOGGER.debug("loadData() started with data: {}", lines.substring(0, 100));
        }
        try (BufferedReader reader = new BufferedReader(new StringReader(lines))) {
            reader.lines()
                    .parallel()
                    .map(this::deserializeLogEntry)
                    .forEach(this::persistLogEntry);
            LOGGER.info("loadData() has loaded data");
        } catch (Exception e) {
            LOGGER.error("loadData() encountered problem: {}", e.getMessage());
            throw new LogsServiceException(e);
        }
    }

    private LogEntry deserializeLogEntry(String line) {
        try {
            return objectMapper.readValue(line, LogEntry.class);
        } catch (IOException e) {
            LOGGER.error("deserializeLogEntry() encountered problem: {} with line: {}", e.getMessage(), line);
            throw new LogsServiceException(e);
        }
    }

    private void persistLogEntry(LogEntry logEntry) {
        try {
            LOGGER.debug("persistLogEntry() will insert: {}", logEntry);
            logsMapper.insert(logEntry);
        } catch (DuplicateKeyException e) {
            // first event has been already inserted
            LOGGER.debug("persistLogEntry() will update duration and alert flag: {}", logEntry);
            logsMapper.updateDuration(logEntry);
            logsMapper.updateAlert(logEntry);
        }
    }

    public static class LogsServiceException extends RuntimeException {
        public LogsServiceException(Throwable cause) {
            super(cause);
        }
    }
}
