package com.intridea.blackberry.twitteroauth;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import com.intridea.blackberry.twitteroauth.logger.Logger;
import com.intridea.blackberry.twitteroauth.oauth.Action;
import com.intridea.blackberry.twitteroauth.oauth.ActionListener;
import com.intridea.blackberry.twitteroauth.oauth.TwitterOAuthService;
import com.intridea.blackberry.twitteroauth.ui.PleaseWaitPopupScreen;
import com.intridea.blackberry.twitteroauth.util.StringUtils;

import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.Persistable;

/**
 * A class extending the MainScreen class, which provides default standard behavior for BlackBerry GUI applications.
 */
public final class MyScreen extends MainScreen implements ActionListener {

    private TwitterOAuthService consumer = null;
    private TwitterLoginScreen loginScreen;
    private PersistentObject store;
    private TwitterPinInputScreen inputScreen = null;
    private String titleToShare = null;

    /**
     * Creates a new MyScreen object
     */
    public MyScreen() {
        // Set the displayed title of the screen
        setTitle("Twitter blackberry oauth example app");
        titleToShare = "title title title title title";
        
        ButtonField button = new ButtonField("Twitter");
        button.setChangeListener(new FieldChangeListener(){
            public void fieldChanged(Field field, int context) {
                twitter();
            }
            
        });
        add(button);
    }

    private void twitter() {
        UiApplication.getUiApplication().invokeLater(new Runnable() {
            public void run() {
                store = PersistentStore.getPersistentObject(MyApp.APPLICATION_GUID);
                synchronized (store) {
                    boolean exists = false;
                    Vector settings = null;
                    if (store.getContents() == null) {
                        settings = new Vector();
                    } else {
                        settings = (Vector) store.getContents();
                        for (Enumeration it = settings.elements(); it.hasMoreElements();) {
                            Persistable setting = (Persistable) it.nextElement();
                            if (setting instanceof TwitterSetting) {
                                exists = true;
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        settings.addElement(new TwitterSetting(null, null));
                        store.setContents(settings);
                        store.commit();
                    }
                }

                TwitterSetting twitterSetting = null;
                Vector settings = (Vector) store.getContents();
                for (Enumeration it = settings.elements(); it.hasMoreElements();) {
                    Object setting = it.nextElement();
                    if (setting instanceof TwitterSetting) {
                        twitterSetting = (TwitterSetting) setting;
                        break;
                    }
                }
                consumer = new TwitterOAuthService(twitterSetting.token, twitterSetting.tokenSecret);
                if (consumer.hasToken()) {
                    shareToTwitter();
                } else {
                    loginScreen = new TwitterLoginScreen(consumer);
                    loginScreen.addActionListener(MyScreen.this);
                    UiApplication.getUiApplication().pushScreen(loginScreen);
                }
            }

        });
    }

    private void shareToTwitter() {
        TwitterBlackberryTask task = new TwitterBlackberryTask();
        PleaseWaitPopupScreen.showScreenAndWait(task, "Sharing to Twitter");
        Logger.debug("The result = " + task.result);
        if (task.result == HttpConnection.HTTP_OK) {
            Dialog.inform("Article successfully shared on Twitter.");
        } else {
            Dialog.alert("Sorry, there was a problem sharing with Twitter.");
        }
    }

    private class TwitterBlackberryTask implements Runnable {
        private int result;

        public void run() {
            // 1.short then url
            String url = "http://www.facebook.com";

            int length = url.length();
            int remaining = 140 - length;
            String title = titleToShare;
            if (title != null && title.length() > remaining) {
                int lastIndex = title.lastIndexOf(' ', remaining);
                if (lastIndex != -1) {
                    title = title.substring(0, lastIndex);
                } else {
                    title = title.substring(0, remaining);
                }
            }
            String status = "status=" + StringUtils.encode("Sharing:" + (title == null ? " Feed " : title) + " " + url);
            Logger.debug(status);
            result = consumer.updateStatus(status);
        }
    }

    public void onAction(Action event) {
        if (event.getSource() == loginScreen) {
            if (event.getAction().equals(TwitterLoginScreen.ACTION_ERROR)) {
                UiApplication.getUiApplication().popScreen(loginScreen);
                Dialog.alert("Fail to connect to Twitter, please try later.");
            } else if (event.getAction().equals(TwitterLoginScreen.ACTION_LOGGED_IN)) {
                inputScreen = new TwitterPinInputScreen(consumer);
                inputScreen.addActionListener(MyScreen.this);
                UiApplication.getUiApplication().pushScreen(inputScreen);
            }
        } else if (event.getSource() == inputScreen) {
            UiApplication.getUiApplication().popScreen(inputScreen);
            if (event.getAction().equals("success")) {
                // save the token/token secret
                if (!consumer.hasToken() || !consumer.accessTokenSucess) {
                    consumer.clean();
                    persistTwitterOauth();
                    Dialog.alert("Fail to got authorization info from Twitter, please try later.");
                } else {
                    persistTwitterOauth();
                    shareToTwitter();
                }
            } else if (event.getAction().equals("cancel")) {
                consumer.clean();
                persistTwitterOauth();
            }
        }

    }

    private void persistTwitterOauth() {
        TwitterSetting linkedInSetting = new TwitterSetting(consumer.token, consumer.tokenSecret);
        synchronized (store) {
            Vector settings = (Vector) store.getContents();
            int linkedInSettingIndex = 0;
            for (int index = 0; index < settings.size(); index++) {
                if (settings.elementAt(index) instanceof TwitterSetting) {
                    linkedInSettingIndex = index;
                    break;
                }
            }
            settings.setElementAt(linkedInSetting, linkedInSettingIndex);
            store.setContents(settings);
            store.commit();
        }
    }
}
