package org.vaadin.viewportservlet;

import com.vaadin.cdi.server.VaadinCDIServlet;
import com.vaadin.server.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.servlet.ServletException;

/**
 * A servlet implementation that overrides the default host page to include
 * viewport tag to force "native" resolution. You'll want this in case you have
 * designed your application for mobile usage, but you are not using Vaadin
 * TouchKit.
 *
 * Normally Vaadin CDI introduces Servlet automatically. To use this, extend
 * this class in you project and annotate with WebServlet annotation. E.g.
 * 
 * <pre>
    &amp;WebServlet(urlPatterns = "/*")
    public class Servlet extends ViewPortCDIServlet {

    }
 * </pre>
 *
 * TODO support good values automatically for various devices, current is good
 * for modern ios devices.
 * 
 * @author matti@vaadin.com
 */
public class ViewPortCDIServlet extends VaadinCDIServlet {

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(new SessionInitListener() {

            @Override
            public void sessionInit(SessionInitEvent event) throws ServiceException {
                event.getSession().addBootstrapListener(
                        new BootstrapListener() {

                            @Override
                            public void modifyBootstrapFragment(
                                    BootstrapFragmentResponse response) {
                                        log("Warning, ViewPortCDIServlet does not support fragments.");
                                    }

                                    @Override
                                    public void modifyBootstrapPage(
                                            BootstrapPageResponse response) {
                                                // <meta name="viewport" content="user-scalable=no,initial-scale=1.0">
                                                Document d = response.
                                                getDocument();
                                                Element el = d.
                                                createElement("meta");
                                                el.attr("name", "viewport");
                                                el.attr("content",
                                                        getViewPortConfiguration(
                                                                response));
                                                d.getElementsByTag(
                                                        "head").get(
                                                        0).appendChild(
                                                        el);
                                            }

                        });

            }
        });

    }

    protected String getViewPortConfiguration(
            BootstrapPageResponse response) {
        return "user-scalable=no,initial-scale=1.0";
    }

}
