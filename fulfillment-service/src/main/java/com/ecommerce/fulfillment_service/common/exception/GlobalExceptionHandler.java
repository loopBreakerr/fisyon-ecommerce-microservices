package com.ecommerce.fulfillment_service.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

import java.util.NoSuchElementException;

/**
 * fulfillment-service icin minimal merkezi hata yoneticisi - core-service'teki
 * GlobalExceptionHandler'in kucuk bir alt kumesi (sadece bu servisin ihtiyaci olan
 * durumlar icin): ownership ihlalleri (SecurityException), bulunamayan kayitlar
 * (NoSuchElementException, orElseThrow()) ve core-service'e RestClient ile yapilan
 * cagrilarin (bkz. shipping.client.OrderClient) dondurdugu hatalar artik generic
 * 500 yerine anlamli bir HTTP status donsun diye eklendi.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Yetkisiz erisim denemesi (orn. ownership ihlali) - guvenlik/audit acisindan
    // hangi kullanicinin hangi kaynaga erismeye calistigini WARN seviyesinde logluyoruz.
    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurity(SecurityException ex, HttpServletRequest request) {
        logger.warn("Yetkisiz erişim denemesi: user={}, path={}, message={}",
                currentUserId(), request.getRequestURI(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Kayıt bulunamadı");
    }

    // core-service'e (orn. OrderClient) yapilan RestClient cagrisi 4xx/5xx donerse,
    // o status'u oldugu gibi yansitiyoruz (core-service'teki FeignException
    // handler'iyla ayni mantik). Hangi bagimli servis cagrisinin hangi status ile
    // basarisiz oldugu WARN seviyesinde loglanir - aksi halde bu tamamen sessiz kalirdi.
    @ExceptionHandler(RestClientResponseException.class)
    public ProblemDetail handleRestClientError(RestClientResponseException ex) {
        logger.warn("Bağımlı servis (RestClient) çağrısı başarısız: status={}, message={}",
                ex.getStatusCode(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return ProblemDetail.forStatusAndDetail(status, "Bağımlı servisten hata alındı");
    }

    // Beklenmeyen her sey icin son care - core-service'teki handleGeneric() ile ayni
    // desen: tam stack trace loglanmadan bu servis daha once tamamen sessizdi.
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu");
    }

    private String currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "unknown";
    }
}
