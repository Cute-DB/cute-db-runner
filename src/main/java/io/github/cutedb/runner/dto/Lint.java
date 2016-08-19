package io.github.cutedb.runner.dto;


import java.io.Serializable;

/**
 * Created by barmi83 on 05/08/16.
 */
public class Lint implements Serializable{

    Long id;
    String uuid;
    String linter;
    String objectName;
    LintSeverity severity;
    String message;
    String value;
    String runUuid;

    public Lint(){

    }

    public Lint(String uuid, String linter, String objectName, String severity, String message, String value, String runUuid){
        this.uuid = uuid;
        this.linter = linter;
        this.objectName = objectName;
        this.severity = LintSeverity.getSeverity(severity);
        this.message = message;
        this.value = value;
        this.runUuid = runUuid;
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

    public String getLinter() {
        return linter;
    }

    public void setLinter(String linter) {
        this.linter = linter;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public LintSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(LintSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRunUuid() {
        return runUuid;
    }

    public void setRunUuid(String runUuid) {
        this.runUuid = runUuid;
    }
}
