package org.example.cv.utils.annotation;

import org.example.cv.utils.userSecurity.Ownable;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OwnableRepository {
    Class<? extends Ownable> entity();
}
