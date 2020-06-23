package ru.biomedis.biomedismair3.social.remote_client

import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError
import java.lang.Exception

/**
 * Представит ошибку валидации полей запроса
 */
class ValidationError(val errors: List<ApiValidationError>): Exception()
