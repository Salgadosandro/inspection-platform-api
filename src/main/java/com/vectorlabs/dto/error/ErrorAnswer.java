package com.vectorlabs.dto.error;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ErrorAnswer(int status, String mensagem, List<UnitError> errors) {

    public static ErrorAnswer answerDefault (String mensagem) {
        return new ErrorAnswer(HttpStatus.BAD_REQUEST.value(), mensagem,List.of());
    }
    public static ErrorAnswer unprocessableEntity (String mensagem) {
        return new ErrorAnswer(HttpStatus.UNPROCESSABLE_ENTITY.value(), mensagem, List.of());
    }
    public static ErrorAnswer conflictError (String mensagem) {
        return new ErrorAnswer(HttpStatus.CONFLICT.value(), mensagem,List.of());
    }
    public static ErrorAnswer notFound (String mensagem) {
        return new ErrorAnswer(HttpStatus.NOT_FOUND.value(), mensagem,List.of());
    }
    public static ErrorAnswer illegaArgument (String mensagem){
        return new ErrorAnswer(HttpStatus.BAD_REQUEST.value(), mensagem,List.of());
    }
    public static ErrorAnswer noAuthenticatedAcces (String mensagem){
        return new ErrorAnswer(HttpStatus.UNAUTHORIZED.value(), mensagem,List.of());
    }
    public static ErrorAnswer forbiddenAcessableEntity (String mensagem){
        return new ErrorAnswer(HttpStatus.FORBIDDEN.value(), mensagem,List.of());
    }
}
