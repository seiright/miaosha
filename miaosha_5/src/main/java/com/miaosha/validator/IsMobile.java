package com.miaosha.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/13 18:27
 */


@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {IsMobileValidator.class})
public @interface IsMobile {

    boolean required() default true;

    String message() default "手机号码格式有误";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
