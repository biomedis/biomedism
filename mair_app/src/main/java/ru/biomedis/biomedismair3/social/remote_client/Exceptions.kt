package ru.biomedis.biomedismair3.social.remote_client

import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError
import java.lang.Exception

/**
 * Представит ошибку валидации полей запроса
 */
class ValidationError(val errors: List<ApiValidationError>): Exception()

/**
 * Требуется аутентификация вводом логина и пароля
 */
class NeedAuthByLogin: Exception()

/**
 * Ошибка в процессе обработки ответа или запроса в приложении
 */
class RequestClientException(cause: Exception): Exception(cause)
