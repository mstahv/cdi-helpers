package org.vaadin.cdiviewmenu;

import com.vaadin.annotations.Title;
import com.vaadin.cdi.CDIView;
import com.vaadin.cdi.UIScoped;
import com.vaadin.cdi.internal.Conventions;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.ValoTheme;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MHorizontalLayout;

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
@UIScoped
public class ViewMenu extends CssLayout {

    @Inject
    BeanManager beanManager;

    private Header header = new Header(null).setHeaderLevel(3);

    private Button selectedButton;

    private HashMap<String, Button> nameToButton = new HashMap<>();
    private Button active;
    private Component secondaryComponent;
    private CssLayout items;

    public List<Bean<?>> getAvailableViews() {
        Set<Bean<?>> all = beanManager.getBeans(View.class,
                new AnnotationLiteral<Any>() {
                });

        final ArrayList<Bean<?>> list = new ArrayList<>();
        for (Bean<?> bean : all) {

            Class<?> beanClass = bean.getBeanClass();

            ViewMenuItem annotation = beanClass.
                    getAnnotation(ViewMenuItem.class);
            if (annotation == null || annotation.enabled()) {
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
                if(a1 == null && a2 == null) {
                   return 0; // don't care about the order if no annotations
                } else if(a1 != null && a2 == null) {
                    return 1;
                } else if (a1 == null) {
                    return -1;
                } else if (a1.order() == a2.order()) {
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

        items = new CssLayout(getAsLinkButtons(getAvailableViews()));
        items.setPrimaryStyleName("valo-menuitems");
        addComponent(items);

        addAttachListener(new AttachListener() {
            @Override
            public void attach(AttachEvent event) {
                if (getMenuTitle() == null) {
                    setMenuTitle(detectMenuTitle());
                }
                Navigator navigator = UI.getCurrent().getNavigator();
                if (navigator != null) {
                    String state = navigator.getState();
                    if (state == null) {
                        state = "";
                    }
                    Button b = nameToButton.get(state);
                    if (b != null) {
                        emphasisAsSelected(b);
                    }
                }
            }
        }
        );
    }

    protected void createHeader() {
        setPrimaryStyleName("valo-menu");
        addStyleName("valo-menu-part");
        MHorizontalLayout headercontent = new MHorizontalLayout(
                header).withMargin(false).
                alignAll(Alignment.MIDDLE_CENTER);
        headercontent.setStyleName("valo-menu-title");
        addComponent(headercontent);
    }

    private Component[] getAsLinkButtons(List<Bean<?>> availableViews) {

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
                    getAnnotation(ViewMenuItem.class
                    );
            if (annotation
                    != null && !annotation.enabled()) {
                continue;
            }

            if (beanClass.getAnnotation(CDIView.class
            ) != null) {
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
                navigateTo(beanClass);
            }
        });
        return button;
    }

    protected void emphasisAsSelected(Button button) {
        if (selectedButton != null) {
            selectedButton.removeStyleName("selected");
        }
        button.addStyleName("selected");
        selectedButton = button;
    }

    protected Resource getIconFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        if(annotation == null) {
            return FontAwesome.FILE;
        }
        return annotation.icon();
    }

    protected String getNameFor(Class<?> viewType) {
        ViewMenuItem annotation = viewType.getAnnotation(ViewMenuItem.class);
        if (annotation != null && !annotation.title().isEmpty()) {
            return annotation.title();
        }
        String simpleName = viewType.getSimpleName();
        // remove trailing view
        simpleName = simpleName.replaceAll("View$", "");
        // decamelcase
        simpleName = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase(
                simpleName), " ");
        return simpleName;
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

    public String getMenuTitle() {
        return header.getText();
    }

    public void setMenuTitle(String menuTitle) {
        this.header.setText(menuTitle);
    }

    private String detectMenuTitle() {
        // try to dig a sane default from Title annotation in UI or class name
        final Class<? extends UI> uiClass = getUI().getClass();
        Title title = uiClass.getAnnotation(Title.class
        );
        if (title
                != null) {
            return title.value();
        } else {
            String simpleName = uiClass.getSimpleName();
            return simpleName.replaceAll("UI", "");
        }
    }

    public View navigateTo(final Class<?> viewClass) {
        CDIView cdiview = viewClass.getAnnotation(CDIView.class);
        String viewId = cdiview.value();
        if(CDIView.USE_CONVENTIONS.equals(viewId)) {
            viewId = Conventions.deriveMappingForView(viewClass);
        }
        return navigateTo(viewId);
    }

    public View navigateTo(final String viewId) {
        removeStyleName("valo-menu-visible");
        Button button = nameToButton.get(viewId);
        if (button != null) {
            final Navigator navigator = UI.getCurrent().getNavigator();

            final MutableObject<View> view = new MutableObject<>();

            ViewChangeListener l = new ViewChangeListener() {

                @Override
                public boolean beforeViewChange(
                        ViewChangeListener.ViewChangeEvent event) {
                    return true;
                }

                @Override
                public void afterViewChange(
                        ViewChangeListener.ViewChangeEvent event) {
                    view.setValue(event.getNewView());
                }
            };

            navigator.addViewChangeListener(l);
            navigator.navigateTo(viewId);
            navigator.removeViewChangeListener(l);
            emphasisAsSelected(button);
            return view.getValue();
        }
        return null;
    }

    public void setSecondaryComponent(Component component) {
        if (secondaryComponent != component) {
            if (secondaryComponent != null) {
                removeComponent(secondaryComponent);
            }
            secondaryComponent = component;
            addComponent(component, 1);
        }
    }

    /**
     * Adds a custom button to the menu.
     *
     * @param button
     */
    public void addMenuItem(Button button) {
        button.setPrimaryStyleName("valo-menu-item");
        items.addComponent(button);
    }

}
