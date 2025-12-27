package ru.shokhinsergey.springproject.exceptionhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidArgumentException;
import ru.shokhinsergey.springproject.exceptionhandler.exception.NotValidIdException;

import java.util.NoSuchElementException;

@RestControllerAdvice

public class AppExceptionHandler
//        extends ResponseEntityExceptionHandler
{
    private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);

    @Value("${springproject.response.exception.not-found}")
    private String RESPONSE_NOT_FOUND;
    @Value("${springproject.response.exception.not-unique}")
    private String RESPONSE_NOT_UNIQUE;
    @Value("${springproject.response.exception.bad-id}")
    private String RESPONSE_BAD_ID;

    @ExceptionHandler(NotValidIdException.class)
    public ResponseEntity<String> HandlerBadId (NotValidIdException exception) {
        log.error(RESPONSE_BAD_ID);
        return ResponseEntity.badRequest().body(RESPONSE_BAD_ID);
    }

    @ExceptionHandler(NotValidArgumentException.class)
    public ResponseEntity<String> HandlerBadArgumentMethod (NotValidArgumentException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> HandlerNoSuchElement (NoSuchElementException exception) {
        log.error(RESPONSE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(RESPONSE_NOT_FOUND);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> HandlerNotUniqueParameter (DataIntegrityViolationException exception) {
        log.error(RESPONSE_NOT_UNIQUE);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(RESPONSE_NOT_UNIQUE);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> HandlerOtherException (Throwable exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<String> HandlerNotUniqueParameter (MethodArgumentNotValidException exception) {
//        String message = parseMessage(exception.getMessage());
//        log.warn(message);
//        return ResponseEntity.badRequest().body(message);
//    }

//    private String parseMessage(String message) {
//        int begin = message.lastIndexOf("[");
//        return  message.substring(begin+1, message.length()-3);
//    }


}
