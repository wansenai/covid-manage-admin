package javax.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该类控制事物会出错，请使用 org.springframework.transaction.annotation.Transactional
 **/
@Deprecated
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Transactional {

    TxType value() default TxType.REQUIRED;

    Class[] rollbackOn() default {};

    Class[] dontRollbackOn() default {};

    enum TxType {
        REQUIRED,

        REQUIRES_NEW,

        MANDATORY,

        SUPPORTS,

        NOT_SUPPORTED,

        NEVER
    }

}
