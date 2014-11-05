package org.vaadin.cdiviewmenu;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.maddon.button.MButton;
import org.vaadin.maddon.label.Header;
import org.vaadin.maddon.layouts.MHorizontalLayout;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.util.*;

/**
 * A helper to automatically create a menu from available Vaadin CDI view.
 * Listed views should be annotated with ViewMenuItem annotation to be listed
 * here, there you can also set icon, caption etc.
 *
 * You'll probably want something more sophisticated in your app, but this might
 * be handy prototyping small CRUD apps.
 *
 * By default the menu uses Valo themes responsive layout rules, but those can
 * easily be overridden.
 *
 */
@Dependent
public class ViewMenu extends CssLayout {

    @Inject
    BeanManager beanManager;

    public List<Bean<?>> getAvailableViews() {
        Set<Bean<?>> all = beanManager.getBeans(View.class,
                new AnnotationLiteral<Any>() {
                });

        final ArrayList<Bean<?>> list = new ArrayList<>();
        for (Bean<?> bean : all) {

            Class<?> beanClass = bean.getBeanClass();

            ViewMenuItem annotation = beanClass.
                    getAnnotation(ViewMenuItem.class);
            if (annotation != null && annotation.enabled()) {
                list.add(bean);
            }
        }

        Collections.sort(list, new Comparator<Bean<?>>() {

            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                ViewMenuItem a1 = o1.getBeanClass().
                        getAnnotation(ViewMenuItem.class);
                ViewMenuItem a2 = o2.getBeanClass().
                        getAnnotation(ViewMenuItem.class);
                if (a1.order() == a2.order()) {
                    return a1.title().compareTo(a2.title());
                } else {
                    return a1.order() - a2.order();
                }
            }
        });

        // TODO check if accessible for current user
        // TODO check if accessible for current user
        return list;
    }

    @PostConstruct
    void init() {
        createHeader();

        final Button showMenu = new Button("Menu", new ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                if (getStyleName().contains("valo-menu-visible")) {
                    removeStyleName("valo-menu-visible");
                } else {
                    addStyleName("valo-menu-visible");
                }
            }
        });
        showMenu.addStyleName(ValoTheme.BUTTON_PRIMARY);
        showMenu.addStyleName(ValoTheme.BUTTON_SMALL);
        showMenu.addStyleName("valo-menu-toggle");
        showMenu.setIcon(FontAwesome.LIST);
        addComponent(showMenu);

        CssLayout items = new CssLayout(getAsLinkButtons(getAvailableViews()));
        items.setPrimaryStyleName("valo-menuitems");
        addComponent(items);
    }

    protected void createHeader() {
        setPrimaryStyleName("valo-menu");
        addStyleName("valo-menu-part");
        MHorizontalLayout headercontent = new MHorizontalLayout(
                new Header("Simple CRM").setHeaderLevel(3)).withMargin(false).
                alignAll(Alignment.MIDDLE_CENTER);
        headercontent.setStyleName("valo-menu-title");
        addComponent(headercontent);
    }

    private HashMap<String, Button> nameToButton = new HashMap<>();
    private Button active;

    private Component[] getAsLinkButtons(
            List<Bean<?>> availableViews) {

        Collections.sort(availableViews, new Comparator<Bean<?>>() {

            @Override
            public int compare(Bean<?> o1, Bean<?> o2) {
                return 0;
            }
        });

        ArrayList<Button> buttons = new ArrayList<>();
        for (Bean<?> viewBean : availableViews) {

            Class<?> beanClass = viewBean.getBeanClass();

            ViewMenuItem annotation = beanClass.
                    getAnnotation(ViewMenuItem.class);
            if (annotation != null && !annotation.enabled()) {
                continue;
            }

            if (beanClass.getAnnotation(CDIView.class) != null) {
                MButton button = getButtonFor(beanClass);
                CDIView view = beanClass.getAnnotation(CDIView.class);
                nameToButton.put(view.value(), button);
                buttons.add(button);
            }
        }

        return buttons.toArray(new Button[0]);
    }

    protected MButton getButtonFor(final Class<?> beanClass) {
        final MButton button = new MButton(getNameFor(beanClass));
        button.setPrimaryStyleName("valo-menu-item");
        button.setIcon(getIconFor(beanClass));
        button.addClickListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                removeStyleName("valo-menu-visible");
                CDIView cdiview = beanClass.getAnnotation(CDIView.class);
                UI.getCurrent().getNavigator().navigateTo(cdiview.value());
            }
        });
        return button;
    }

    protected Resource getIconFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        return annotation.icon();
    }

    protected String getNameFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        if (!annotation.title().isEmpty()) {
            return annotation.title();
        }
        return viewType.getSimpleName();
    }

    public void setActive(String viewId) {
        if (active != null) {
            active.setEnabled(true);
        }
        active = nameToButton.get(viewId);
        if (active != null) {
            active.setEnabled(false);
        }
    }
}
