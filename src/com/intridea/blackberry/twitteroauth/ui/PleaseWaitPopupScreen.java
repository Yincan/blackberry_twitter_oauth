package com.intridea.blackberry.twitteroauth.ui;

import java.util.Vector;

import com.intridea.blackberry.twitteroauth.logger.Logger;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class PleaseWaitPopupScreen extends PopupScreen {

    private static final String TAG = "PleaseWaitPopupScreen";

    protected ProgressAnimationField spinner;
    private LabelField _ourLabelField = null;

    private PleaseWaitPopupScreen(String text) {
        super(new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER
                | HorizontalFieldManager.NO_HORIZONTAL_SCROLL | HorizontalFieldManager.NO_HORIZONTAL_SCROLLBAR));
        spinner = new ProgressAnimationField(Bitmap.getBitmapResource("spinner2.png"), 6, Field.FIELD_VCENTER);
        this.add(spinner);
        _ourLabelField = new LabelField(text, Field.FIELD_VCENTER);
        _ourLabelField.setPadding(0, 5, 0, 8);
        this.add(_ourLabelField);
    }

    public static void showScreenAndWait(final Runnable runThis) {
        showScreenAndWait(runThis, "Please wait while loading...");
    }
    public static void showScreenAndWait(final Runnable runThis, String title) {
        final PleaseWaitPopupScreen thisScreen = new PleaseWaitPopupScreen(title);
        Thread threadToRun = new Thread() {
            public void run() {
                // Run the code that must be executed in the Background
                try {
                    runThis.run();
                } catch (Throwable t) {
                    Logger.error(TAG + ", fail to execute the runnable job : " + t.toString());
                }
                // Now dismiss this screen
                UiApplication.getUiApplication().invokeLater(new Runnable() {
                    public void run() {
                        UiApplication.getUiApplication().popScreen(thisScreen);
                    }
                });
            }
        };
        threadToRun.start();
        UiApplication.getUiApplication().pushModalScreen(thisScreen);
    }

    public static void showScreenAndWait(final Vector runnables) {
        if (runnables.size() == 1) {
            showScreenAndWait((Runnable) runnables.elementAt(0));
        } else {
            final PleaseWaitPopupScreen thisScreen = new PleaseWaitPopupScreen("Please wait while loading...");

            final Thread[] threads = new Thread[runnables.size() - 1];
            for (int i = 1; i < runnables.size(); i++) {
                final Runnable runnable = (Runnable) runnables.elementAt(i);
                threads[i - 1] = new Thread(runnable);
            }

            Thread threadOne = new Thread() {
                public void run() {
                    // Run the code that must be executed in the Background
                    try {
                        ((Runnable) runnables.elementAt(0)).run();
                        for (int i = 0; i < threads.length; i++) {
                            try {
                                threads[i].join();
                            } catch (Throwable t) {
                                Logger.error(TAG + ", fail to execute the runnable job : " + t.toString());
                            }
                        }
                    } catch (Throwable t) {
                        Logger.error(TAG + ", fail to execute the runnable job : " + t.toString());
                    }

                    // Now dismiss this screen
                    UiApplication.getUiApplication().invokeLater(new Runnable() {
                        public void run() {
                            UiApplication.getUiApplication().popScreen(thisScreen);
                        }
                    });
                }
            };
            threadOne.start();
            for (int i = 0; i < threads.length; i++) {
                try {
                    threads[i].start();
                } catch (Throwable t) {
                    Logger.error(TAG + ", fail to execute the runnable job : " + t.toString());
                }
            }
            
            UiApplication.getUiApplication().pushModalScreen(thisScreen);
        }
    }
}