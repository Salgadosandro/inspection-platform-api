package com.vectorlabs.controller.common;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

public interface GenericController {

    default URI generateHeaderLocation(UUID id){
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path(
                        "/{id}"
                ).buildAndExpand(id)
                .toUri();
    }

    default URI locationOf(String resourcePath, UUID id) {
        return ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(resourcePath)
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();
    }


}