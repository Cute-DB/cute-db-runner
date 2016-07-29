package io.github.cutedb.runner.dto;

import java.util.Arrays;
import java.util.List;

/**
 * Created by barmi83 on 13/06/16.
 */
public enum BuildStatus {
    RUNNING("Running"),
    PENDING("Pending"),
    FAILURE("Failure"),
    SUCCESS("Success");


    private final String descr;

    private BuildStatus(final String description){
        this.descr = description;
    }

    @Override
    public String toString() {
        return descr;
    }

    public static String valueOf(BuildStatus e) {
        return e.toString();
    }

    public static List<BuildStatus> listAll(){
        return Arrays.asList(values());
    }
}
