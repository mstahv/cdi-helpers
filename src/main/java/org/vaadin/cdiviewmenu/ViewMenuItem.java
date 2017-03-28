package org.vaadin.cdiviewmenu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.icons.VaadinIcons;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ViewMenuItem {
    
    public static final int END = Integer.MAX_VALUE;
    public static final int BEGINNING = 0;
    public static final int DEFAULT = 1000;
	
	public boolean enabled() default true;
	
	public String title() default "";
    
    public int order() default DEFAULT;
    
    public VaadinIcons icon() default VaadinIcons.FILE;

}
