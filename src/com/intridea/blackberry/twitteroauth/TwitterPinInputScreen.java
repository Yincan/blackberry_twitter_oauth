/*
 * Copyright 2010, Intridea, Inc.
 */
package com.intridea.blackberry.twitteroauth;

import com.intridea.blackberry.twitteroauth.oauth.ActionScreen;
import com.intridea.blackberry.twitteroauth.oauth.TwitterOAuthService;
import com.intridea.blackberry.twitteroauth.ui.BorderedFieldManager;
import com.intridea.blackberry.twitteroauth.ui.PleaseWaitPopupScreen;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

/**
 * Ask to input the PIN code. after user get the PIN code from twitter's authorization page.
 * @author yincan
 */
public class TwitterPinInputScreen extends ActionScreen {
    public TwitterPinInputScreen(final TwitterOAuthService consumer) {
        setTitle("Please input the 7 digital PIN code:");
        BorderedFieldManager instapaperAccountName = new BorderedFieldManager();
        Field title = new LabelField("Twitter authorization PIN code:", Field.USE_ALL_WIDTH);
        instapaperAccountName.add(title);
        final BasicEditField pin = new BasicEditField("", "", 7, Field.USE_ALL_WIDTH);
        pin.setMargin(5, 15, 5, 15);
        instapaperAccountName.add(pin);
        add(instapaperAccountName);

        add(new SeparatorField());
        
        HorizontalFieldManager tools = new HorizontalFieldManager(Field.USE_ALL_WIDTH);
        ButtonField save = new ButtonField("Process", Field.FIELD_HCENTER);
        save.setChangeListener(new FieldChangeListener() {
            public void fieldChanged(Field field, int context) {
                if (pin.getText() != null && pin.getText().trim().length() > 0 && pin.getText().trim().length() != 7) {
                    consumer.verifier = pin.getText();
                    UiApplication.getUiApplication().invokeLater(new Runnable() {
                        public void run() {
                            PleaseWaitPopupScreen.showScreenAndWait(new Runnable() {
                                public void run() {
                                    consumer.accessToken();
                                }
                            }, "Finishing the authorization.");
                            fireAction("success", consumer.tokenSecret);
                        }
                    });
                } else {
                    Dialog.alert("Please input the 7 digital PIN first.");
                }
            }
        });
        tools.add(save);
        ButtonField cancel = new ButtonField("Cancel", Field.FIELD_HCENTER);
        cancel.setChangeListener(new FieldChangeListener() {
            public void fieldChanged(Field field, int context) {
                UiApplication.getUiApplication().invokeLater(new Runnable() {
                    public void run() {
                        fireAction("cancel");
                    }
                });
            }
        });
        tools.add(cancel);
        add(tools);
    }

    public boolean isDirty() {
        return false;
    }

}
