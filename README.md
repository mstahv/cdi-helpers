# cdi-helpers

This Vaadin add-on contains some generic helpers that are often needed in Vaadin CDI based applications.

Currently following helpers are available, feel free to contribute more or enhance existing ones.

 * ViewPortCDIServlet - Use if you have designed your app to work with small screen devices, but your are not using TouchKit
 * ViewMenu - A simple menu component that automatically generates a menu based on the views available in the application. This part expects you are using Vaadin Navigator for view changes. ViewMenuItem annotation is used to provide metadata like icons for the views.
 * ViewMenuLayout is a basic top level layout that can be used with the ViewMenu. Using this you can take advantage of the "responsive menu" features from Valo theme, similar as in demo.vaadin.com/valo-theme
 * ViewMenuUI is a UI with ViewMenuLayout, fully configured with CDIViewProvider. Just register that in you application and all you need to do is to add your actual views and annotate them with ViewMenuItem (and CDIView).

The vaadin-cdi is declared as transitive dependency, so only cdi-helpers is needed as a dependency.

