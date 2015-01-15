package com.riccardonoviello.simplesqlmapper.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author novier
 */
@Target(value = {ElementType.METHOD, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Relationship {

    public String member() default "";
    
    public String column() default "";
    
    public boolean single() default false;
    
    public boolean multiple() default false; 

}