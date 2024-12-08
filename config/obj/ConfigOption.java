package by.dragonsurvivalteam.dragonsurvival.config.obj;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigOption {
	ConfigSide side();
	String key();
	/** The category the option will be found in - the array defines a path (e.g. {"a", "b", "c"} results in the path a.b.c.key) */
	String[] category() default {};
	String[] comment();
	String localization() default "";

	boolean restart() default false;
}