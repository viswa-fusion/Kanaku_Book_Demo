package com.example.domain.repository.response

enum class DataLayerErrorCode(val code:Int) {
    NOT_FOUND(404),
    FORBIDDEN(403),
    OPERATION_FAILED(402),
    INVALID_PHONE_NUMBER(412),
    INVALID_PASSWORD(413),
    FILE_NOT_FOUND(400),
    INVALID_FILE_FORMAT(410),
    INSERT_FAILED(411)
}