package io.github.datromtool.domain.datafile.enumerations;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ForceNoDump {
    @JsonProperty("obsolete")
    OBSOLETE,
    @JsonProperty("required")
    REQUIRED,
    @JsonProperty("ignore")
    IGNORE
}
