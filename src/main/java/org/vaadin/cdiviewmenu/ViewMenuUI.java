package org.vaadin.cdiviewmenu;

import com.vaadin.cdi.CDIViewProvider;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Notification;
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
                getMainContent()) {

                    @Override
                    public void navigateTo(String navigationState) {
                        try {
                            super.navigateTo(navigationState);
                        } catch (Exception e) {
                            handleNavigationError(navigationState, e);
                        }
                    }

                };
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
        return ((ViewMenuUI) UI.getCurrent()).getViewMenuLayout().getViewMenu();
    }

    /**
     * Workaround for issue 1, related to vaadin issues: 13566, 14884
     *
     * @param navigationState the view id that was requested
     * @param e the exception thrown by Navigator
     */
    protected void handleNavigationError(String navigationState, Exception e) {
        Notification.show(
                "The requested view (" + navigationState + ") was not available, "
                + "entering default screen.", Notification.Type.WARNING_MESSAGE);
        if (navigationState != null && !navigationState.isEmpty()) {
            getNavigator().navigateTo("");
        }
        getSession().getErrorHandler().error(new com.vaadin.server.ErrorEvent(e));
    }

}
