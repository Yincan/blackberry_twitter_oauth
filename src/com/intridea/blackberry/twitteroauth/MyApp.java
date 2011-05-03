package com.intridea.blackberry.twitteroauth;

import net.rim.device.api.ui.UiApplication;

/**
 * This class extends the UiApplication class, providing a graphical user interface.
 */
public class MyApp extends UiApplication {
    
    public static final long APPLICATION_GUID =  0xd82bfc8989308ea7L;
    public static final String APPLICATION_NAME = "OAUTH_TWITTER_BLACKBERRY";
    /**
     * Entry point for application
     * 
     * @param args
     *            Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create a new instance of the application and make the currently
        // running thread the application's event dispatch thread.
        MyApp theApp = new MyApp();
        theApp.enterEventDispatcher();
    }

    /**
     * Creates a new MyApp object
     */
    public MyApp() {
        // Push a screen onto the UI stack for rendering.
        pushScreen(new MyScreen());
    }
}
