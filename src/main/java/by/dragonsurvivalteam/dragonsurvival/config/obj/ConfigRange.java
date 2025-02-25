package by.dragonsurvivalteam.dragonsurvival.config.obj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigRange {
    /** Defaults to the min. value of the field data type */
    double min() default Double.NaN;

    /** Defaults to the max. value of the field data type */
    double max() default Double.NaN;
}