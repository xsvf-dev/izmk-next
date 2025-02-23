package ovo.xsvf.izmk.injection.accessor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Whether the target field is final or not.
 * <p>
 *     If the access method is a getter, setting this to true is optional,
 *     but it can be very useful to improve performance (~500% faster).
 *     <br>
 *     If the access method is a setter, setting this to true is <b>mandatory</b>,
 *     or a {@link IllegalAccessException} will be thrown.
 * </p>
 * @return true if this accessor is final, false otherwise.
 */
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD})
public @interface Final {
}
