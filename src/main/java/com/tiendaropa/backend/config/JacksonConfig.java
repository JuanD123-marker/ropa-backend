package com.tiendaropa.backend.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Le enseña a Jackson (la librería que convierte los objetos a JSON) a manejar
 * los "proxies" perezosos de Hibernate (como Prenda.tienda, que no siempre
 * viene cargada de la base de datos). Sin esto, cualquier endpoint que
 * devuelva una entidad con una relación lazy falla al convertirla a JSON.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        return new Hibernate6Module();
    }
}