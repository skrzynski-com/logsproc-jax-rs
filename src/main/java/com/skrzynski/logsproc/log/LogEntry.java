package com.skrzynski.logsproc.log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogEntry {

    private String id;
    private LogState state;
    private long timestamp;
    private String type;
    private String host;
    private long duration;
    private boolean alert;
    private int version;

    public String getId() {
        return id;
    }

    public LogEntry setId(String id) {
        this.id = id;
        return this;
    }

    public LogState getState() {
        return state;
    }

    public LogEntry setState(LogState state) {
        this.state = state;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public LogEntry setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getType() {
        return type;
    }

    public LogEntry setType(String type) {
        this.type = type;
        return this;
    }

    public String getHost() {
        return host;
    }

    public LogEntry setHost(String host) {
        this.host = host;
        return this;
    }

    public long getDuration() {
        return duration;
    }

    public LogEntry setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public boolean isAlert() {
        return alert;
    }

    public LogEntry setAlert(boolean alert) {
        this.alert = alert;
        return this;
    }

    public int getVersion() {
        return version;
    }

    public LogEntry setVersion(int version) {
        this.version = version;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogEntry logEntry = (LogEntry) o;
        return Objects.equals(id, logEntry.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "id='" + id + '\'' +
                ", state=" + state +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                ", host='" + host + '\'' +
                ", duration=" + duration +
                ", alert=" + alert +
                ", version=" + version +
                '}';
    }
}
