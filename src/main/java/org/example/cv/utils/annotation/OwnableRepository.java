package org.example.cv.utils.annotation;

import java.lang.annotation.*;

import org.example.cv.utils.security.Ownable;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OwnableRepository {
    Class<? extends Ownable> entity();
}
