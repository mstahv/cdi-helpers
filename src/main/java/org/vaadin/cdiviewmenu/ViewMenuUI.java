package org.vaadin.cdiviewmenu;

import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.UI;
import javax.inject.Inject;

/**
 * A helper class with basic main layout with ViewMenu and ViewMenuLayout,
 * configures Navigator automatically. This way you'll get professional looking
 * basic application structure for free.
 *
 * In your own app, override this class and map it with CDIUI annotation.
 */
public class ViewMenuUI extends UI {

    @Inject
    protected CDIViewProvider viewProvider;

    @Inject
    protected ViewMenuLayout viewMenuLayout;

    @Override
    protected void init(VaadinRequest request) {
        Navigator navigator = new Navigator(this, viewMenuLayout.
                getMainContent());
        navigator.addProvider(viewProvider);
        setContent(viewMenuLayout);
    }

    public ViewMenuLayout getViewMenuLayout() {
        return viewMenuLayout;
    }
    
    public CssLayout getContentLayout() {
        return viewMenuLayout.getMainContent();
    }
    
    public static ViewMenu getMenu() {
        return ((ViewMenuUI)UI.getCurrent()).getViewMenuLayout().getViewMenu();
    }

}
