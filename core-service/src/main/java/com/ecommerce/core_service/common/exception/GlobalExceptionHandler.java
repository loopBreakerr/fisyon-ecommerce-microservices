package com.ecommerce.core_service.common.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Tum core-service controller'lari icin merkezi hata yoneticisi.
 * Her exception tipini anlamli bir HTTP status'a ve Spring'in standart
 * ProblemDetail (RFC 7807) govdesine cevirir.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // orElseThrow() cagrilarinin ureteceği varsayilan exception - kayit yok.
    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Kayıt bulunamadı");
    }

    // checkOwnership() gibi manuel yetki kontrollerinin firlattigi exception.
    // Guvenlik/audit acisindan hangi kullanicinin hangi kaynaga erismeye calistigini
    // WARN seviyesinde logluyoruz (IDOR denemesi olabilir, sessiz kalmasi kor nokta olurdu).
    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurity(SecurityException ex, HttpServletRequest request) {
        logger.warn("Yetkisiz erişim denemesi: user={}, path={}, message={}",
                currentUserId(), request.getRequestURI(), ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // @PreAuthorize reddi (rolu olmayan kullanicinin admin/seller endpoint'ine erisimi).
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        logger.warn("Erişim reddedildi (@PreAuthorize): user={}, path={}",
                currentUserId(), request.getRequestURI());
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, "Bu işlem için yetkiniz yok");
    }

    // Is kurali ihlalleri (ornek: bos sepetle checkout).
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // @Valid ile isaretli @RequestBody'lerdeki Bean Validation (jakarta.validation) ihlalleri.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Geçersiz istek");
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage()));
        problem.setProperty("fieldErrors", fieldErrors);
        return problem;
    }

    // Unique/foreign key ihlalleri (ornek: var olmayan categoryId ile urun olusturma).
    // Hangi constraint'in patladigini gormek icin WARN seviyesinde ex.getMessage() loglanir -
    // aksi halde production'da bu hatanin kok nedeni asla bilinemez.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        logger.warn("Veri bütünlüğü ihlali: {}", ex.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Veri bütünlüğü hatası (ilişkili kayıt bulunamadı veya benzersizlik ihlali)");
    }

    // core-service icindeki Feign clientlarin (ICatalogClient, ICartClient vb.) sardigi
    // downstream servis hatalari - status'u oldugu gibi yansitiyoruz. Hangi bagimli
    // servis cagrisinin hangi status ile basarisiz oldugu WARN seviyesinde loglanir.
    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeign(FeignException ex) {
        logger.warn("Bağımlı servis (Feign) çağrısı başarısız: status={}, message={}",
                ex.status(), ex.getMessage());
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            status = HttpStatus.BAD_GATEWAY;
        }
        return ProblemDetail.forStatusAndDetail(status, "Bağımlı servisten hata alındı");
    }

    // Beklenmeyen her sey icin son care.
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
