package com.mcart.productcatalogsvc.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

import io.r2dbc.spi.ConnectionFactory;

@Configuration
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return null; // not needed if auto-configured
    }

    @Bean
    @Override
    public R2dbcCustomConversions r2dbcCustomConversions() {

        List<Object> converters = List.of(
                new MapToJsonConverter(),
                new JsonToMapConverter()
        );

        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }
}
