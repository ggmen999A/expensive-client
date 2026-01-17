package by.algorithm.alpha.api.modules.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(value = RetentionPolicy.RUNTIME)
public @interface ModuleAnnot {
    String name();
    int key() default 0;
    ModuleCategory type();
    String description() default "Описание не задано";
}