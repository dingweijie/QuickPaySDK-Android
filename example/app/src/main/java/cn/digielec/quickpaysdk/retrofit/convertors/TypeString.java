package cn.digielec.quickpaysdk.retrofit.convertors;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**自定义注解类型：TypeString
 * Created by dw on 2016/3/31.
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface TypeString {
}
