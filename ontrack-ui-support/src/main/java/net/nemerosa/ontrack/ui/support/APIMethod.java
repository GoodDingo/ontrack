package net.nemerosa.ontrack.ui.support;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface APIMethod {

    String value();

    String description() default "";

}
