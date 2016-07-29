package io.github.cutedb.runner.dto;

import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Created by barmi83 on 29/07/16.
 */
public class Run {

    private static final int CRITICAL_WEIGHT = 5;
    private static final int HIGH_WEIGHT = 3;
    private static final int MEDIUM_WEIGHT = 2;
    private static final int LOW_WEIGHT = 1;

    private Long id = null;
    private String uuid = null;
    private String jdbcUrl = null;
    private String server = null;
    private String host = null;
    private String user = null;
    private String databaseProductName = null;
    private BuildStatus status = null;
    private Timestamp started = null;
    private Timestamp ended = null;
    private Integer criticalHits = 0;
    private Integer highHits = 0;
    private Integer mediumHits = 0;
    private Integer lowHits = 0;

    public Run(){
        started = new Timestamp(new DateTime().getMillis());
        status = BuildStatus.PENDING;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }
    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getServer() {
        return server;
    }
    public void setServer(String server) {
        this.server = server;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }
    public void setDatabaseProductName(String databaseProductName) {
        this.databaseProductName = databaseProductName;
    }

    public BuildStatus getStatus() {
        return status;
    }
    public void setStatus(BuildStatus status) {
        this.status = status;
    }

    public Timestamp getStarted() {
        return started;
    }

    public void setStarted(Timestamp started) {
        this.started = started;
    }

    public Timestamp getEnded() {
        return ended;
    }

    public void setEnded(Timestamp ended) {
        this.ended = ended;
    }

    public Integer getCriticalHits() {
        return criticalHits;
    }

    public void setCriticalHits(Integer criticalHits) {
        this.criticalHits = criticalHits;
    }

    public Integer getHighHits() {
        return highHits;
    }

    public void setHighHits(Integer highHits) {
        this.highHits = highHits;
    }

    public Integer getMediumHits() {
        return mediumHits;
    }

    public void setMediumHits(Integer mediumHits) {
        this.mediumHits = mediumHits;
    }

    public Integer getLowHits() {
        return lowHits;
    }

    public void setLowHits(Integer lowHits) {
        this.lowHits = lowHits;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Run run = (Run) o;
        return Objects.equals(id, run.id) &&
                Objects.equals(jdbcUrl, run.jdbcUrl) &&
                Objects.equals(server, run.server) &&
                Objects.equals(host, run.host) &&
                Objects.equals(user, run.user) &&
                Objects.equals(databaseProductName, run.databaseProductName) &&
                Objects.equals(status, run.status) &&
                Objects.equals(started, run.started) &&
                Objects.equals(ended, run.ended)&&
                Objects.equals(uuid, run.uuid)&&
                Objects.equals(criticalHits, run.criticalHits)&&
                Objects.equals(highHits, run.highHits)&&
                Objects.equals(mediumHits, run.mediumHits)&&
                Objects.equals(lowHits, run.lowHits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, jdbcUrl, server, host, user, databaseProductName, status, started, ended, uuid, criticalHits, highHits, mediumHits, lowHits);
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();
        sb.append("class Run {\n");

        sb.append("  id: ").append(id).append("\n");
        sb.append("  uuid: ").append(uuid).append("\n");
        sb.append("  jdbcUrl: ").append(jdbcUrl).append("\n");
        sb.append("  server: ").append(server).append("\n");
        sb.append("  host: ").append(host).append("\n");
        sb.append("  user: ").append(user).append("\n");
        sb.append("  databaseProductName: ").append(databaseProductName).append("\n");
        sb.append("  status: ").append(status).append("\n");
        sb.append("  criticalHits: ").append(criticalHits).append("\n");
        sb.append("  highHits: ").append(highHits).append("\n");
        sb.append("  mediumHits: ").append(mediumHits).append("\n");
        sb.append("  lowHits: ").append(lowHits).append("\n");
        sb.append("}\n");
        return sb.toString();
    }

    public Integer getScore(){
        return criticalHits >= 1 ? 0 : (highHits >= 1 ? 1 : (mediumHits >= 1 ? 2 : (lowHits >= 1 ? 3 : 4)));
    }

    public Integer getWeightedScore(){
        int totalHits = criticalHits+highHits+mediumHits+lowHits;
        if(totalHits == 0)
            return 0;

        return (criticalHits*CRITICAL_WEIGHT + highHits*HIGH_WEIGHT + mediumHits*MEDIUM_WEIGHT + lowHits*LOW_WEIGHT)/totalHits;
    }
}