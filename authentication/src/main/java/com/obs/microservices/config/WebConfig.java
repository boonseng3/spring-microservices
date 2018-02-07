package com.obs.microservices.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.obs.microservices.CustomPermission;
import com.obs.microservices.dto.AccessControlEntryDto;
import com.obs.microservices.dto.AclDto;
import com.obs.microservices.dto.ObjectIdentityDto;
import com.obs.microservices.mixin.CustomPermissionMixin;
import com.obs.microservices.mixin.IdMixin;
import com.obs.microservices.mixin.IdentifierMixin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfig {

    @Bean
    public Module module() {
        SimpleModule m = new SimpleModule();
        m.setMixInAnnotation(CustomPermission.class, CustomPermissionMixin.class);
        m.setMixInAnnotation(ObjectIdentityDto.class, IdentifierMixin.class);
        m.setMixInAnnotation(AclDto.class, IdMixin.class);
        m.setMixInAnnotation(AccessControlEntryDto.class, IdMixin.class);
        return m;
    }
}
