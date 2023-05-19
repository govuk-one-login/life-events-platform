package uk.gov.gdx.datashare.config

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import java.util.*

@RestControllerAdvice
class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    log.debug("Validation error (400) returned: {}", e.message)
    val message = if (e.hasFieldErrors()) {
      e.fieldError?.defaultMessage
    } else {
      e.message
    }
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = (BAD_REQUEST.value()),
          userMessage = "Validation failure: $message",
          developerMessage = (e.message),
        ),
      )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned with message {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          userMessage = "Forbidden: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(SupplierPermissionException::class)
  fun handleSupplierPermissionException(e: SupplierPermissionException): ResponseEntity<ErrorResponse> {
    log.debug("Forbidden (403) returned with message {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN,
          userMessage = "Forbidden: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(WebExchangeBindException::class)
  fun handleWebExchangeBindException(e: WebExchangeBindException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    val message = if (e.hasFieldErrors()) {
      e.fieldError?.defaultMessage
    } else {
      e.message
    }
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Validation failure: $message",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(NoDataFoundException::class)
  fun handleNoDataFoundException(e: NoDataFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("No data found exception caught: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Not Found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(EventNotFoundException::class)
  fun handleEventNotFoundException(e: EventNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Event not found exception caught: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Event Not Found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(SupplierSubscriptionNotFoundException::class)
  fun handleSupplierSubscriptionNotFoundException(e: SupplierSubscriptionNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Supplier subscription not found exception caught: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Supplier Subscription Not Found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(AcquirerSubscriptionNotFoundException::class)
  fun handleAcquirerSubscriptionNotFoundException(e: AcquirerSubscriptionNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Acquirer subscription not found exception caught: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Acquirer subscription Not Found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(AcquirerNotFoundException::class)
  fun handleAcquirerNotFoundException(e: AcquirerNotFoundException): ResponseEntity<ErrorResponse?>? {
    log.debug("Acquirer snot found exception caught: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND,
          userMessage = "Acquirer Not Found: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR,
          userMessage = "Unexpected error: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ServerWebInputException::class)
  fun handleServerWebInputException(e: ServerWebInputException): ResponseEntity<ErrorResponse> {
    log.error("Parameter conversion exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Parameter conversion failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(TypeMismatchException::class)
  fun handleTypeMismatchException(e: TypeMismatchException): ResponseEntity<ErrorResponse> {
    log.error("Parameter conversion exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST,
          userMessage = "Parameter conversion failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

class SupplierPermissionException(message: String) : Exception(message)
class EventNotFoundException(message: String) : Exception(message)
class AcquirerSubscriptionNotFoundException(message: String) : Exception(message) {
  constructor(id: UUID) : this("No acquirer subscription found with id $id")
}

class AcquirerNotFoundException(message: String) : Exception(message)

class SupplierSubscriptionNotFoundException(message: String) : Exception(message) {
  constructor(id: UUID) : this("No subscriber found with id $id")
}

class NoDataFoundException(message: String) : Exception(message)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Error Response")
data class ErrorResponse(
  @Schema(description = "Status of Error", example = "500", required = true)
  val status: Int,
  @Schema(description = "Error Code", example = "500", required = false)
  val errorCode: Int? = null,
  @Schema(
    description = "User Message of error",
    example = "Bad Data",
    required = false,
    maxLength = 200,
    pattern = "^[a-zA-Z\\d. _-]{1,200}\$",
  )
  val userMessage: String? = null,
  @Schema(
    description = "More detailed error message",
    example = "This is a stack trace",
    required = false,
    maxLength = 4000,
    pattern = "^[a-zA-Z\\d. _-]*\$",
  )
  val developerMessage: String? = null,
  @Schema(
    description = "More information about the error",
    example = "More info",
    required = false,
    maxLength = 4000,
    pattern = "^[a-zA-Z\\d. _-]*\$",
  )
  val moreInfo: String? = null,
) {
  constructor(
    status: HttpStatus,
    errorCode: Int? = null,
    userMessage: String? = null,
    developerMessage: String? = null,
    moreInfo: String? = null,
  ) :
    this(status.value(), errorCode, userMessage, developerMessage, moreInfo)
}
