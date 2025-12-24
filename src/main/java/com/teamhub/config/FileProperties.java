package com.teamhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "file")
@Getter
@Setter
public class FileProperties {

    private String uploadDir;
    private long maxSize;
    private String allowedTypes;

    public List<String> getAllowedTypeList() {
        return Arrays.asList(allowedTypes.split(","));
    }
}
