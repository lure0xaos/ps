module password.store {
    requires javafx.controls;
    requires java.prefs;
    requires javafx.web;
    exports gargoyle.ps to javafx.graphics;
    opens gargoyle.ps;
    opens gargoyle.ps.css;
    opens gargoyle.ps.icons;
    opens gargoyle.ps.images;
}
