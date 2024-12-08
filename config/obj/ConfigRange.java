package by.dragonsurvivalteam.dragonsurvival.config.obj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target( ElementType.FIELD)
public @interface ConfigRange{
	double min() default -1;
	double max() default -1;
}