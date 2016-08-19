package io.github.cutedb.runner.dto;

/**
 * Created by barmi83 on 05/08/16.
 */
public enum LintSeverity {
    low,
    medium,
    high,
    critical;

    private LintSeverity() {
    }

    public static LintSeverity getSeverity(String key) {
        for(LintSeverity v : values()){
            if( v.name().equals(key)){
                return v;
            }
        }
        return null;
    }
}
