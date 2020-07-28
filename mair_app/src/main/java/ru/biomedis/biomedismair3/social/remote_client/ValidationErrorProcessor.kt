package ru.biomedis.biomedismair3.social.remote_client

import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiError
import ru.biomedis.biomedismair3.social.remote_client.dto.error.ApiValidationError

/**
 * Проверяет наличие ошибки валидации, возвращает список ошибок валидации
 */
object ValidationErrorProcessor{
    @JvmStatic
    fun process(apiError: ApiError): List<ApiValidationError>{
        if(!apiError.isValidationError) return listOf()

       return apiError.subErrors.asSequence()
                .filter { it is ApiValidationError }
                .map { it as ApiValidationError }
                .toList()
    }
}
