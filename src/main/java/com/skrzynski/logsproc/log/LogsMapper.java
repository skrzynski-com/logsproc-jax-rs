package com.skrzynski.logsproc.log;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface LogsMapper {

    @Select("SELECT id, state, timestamp, type, host, duration, alert, version " +
            "FROM logs " +
            "WHERE id = #{id}")
    LogEntry getById(String id);

    @Select("SELECT id, state, timestamp, type, host, duration, alert, version " +
            "FROM logs " +
            "ORDER BY timestamp")
    List<LogEntry> getAll();

    @Select("SELECT id, state, timestamp, type, host, duration, alert, version " +
            "FROM logs " +
            "WHERE alert = #{alert} " +
            "ORDER BY timestamp")
    List<LogEntry> getWithAlert(Map queryParams);

    @Insert("INSERT INTO logs (id, state, timestamp, type, host, duration, alert, version) " +
            "VALUES(#{id}, #{state}, #{timestamp}, #{type}, #{host}, #{duration}, #{alert}, #{version})")
    void insert(LogEntry logEntry);

    @Update("UPDATE logs SET duration = ABS(timestamp-#{timestamp}), version=1 WHERE id = #{id} AND version=0")
    long updateDuration(LogEntry logEntry);

    @Update("UPDATE logs SET alert=true WHERE id = #{id} AND version=1 AND duration>4")
    long updateAlert(LogEntry logEntry);


    @Delete("TRUNCATE TABLE logs")
    void truncate();
}
