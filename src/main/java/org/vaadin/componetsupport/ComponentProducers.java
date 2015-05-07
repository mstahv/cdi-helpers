
package org.vaadin.componetsupport;

import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;
import javax.enterprise.inject.Produces;

/**
 *
 * @author Matti Tahvonen
 */
public class ComponentProducers {
    
    @Produces
    public TextField textField() {
        return new TextField();
    }

    @Produces
    public Button button() {
        return new Button();
    }
}
