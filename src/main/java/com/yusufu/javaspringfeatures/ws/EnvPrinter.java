package com.yusufu.javaspringfeatures.ws;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

@Component
public class EnvPrinter {

    private final Environment env;

    public EnvPrinter(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void printAll() {
        System.out.println("---- SPRING ENV VARIABLES ----");

        ((AbstractEnvironment) env).getPropertySources().forEach(propertySource -> {
            if (propertySource instanceof MapPropertySource mapSource) {
                mapSource.getSource().forEach((key, value) -> {
                    System.out.println(key + "=" + env.getProperty(key));
                });
            }
        });
    }
}