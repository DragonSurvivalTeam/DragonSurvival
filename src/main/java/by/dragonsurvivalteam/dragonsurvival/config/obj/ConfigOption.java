package by.dragonsurvivalteam.dragonsurvival.config.obj;

import net.minecraftforge.api.distmarker.Dist;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigOption{
	Dist side();

	String key();
	String[] category() default {};
	String[] comment();
	String localization() default "";

	boolean restart() default false;
}