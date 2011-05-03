/*
 * Copyright 2010, Intridea, Inc.
 */
package com.intridea.blackberry.twitteroauth;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.InputConnection;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldNavigationRequestHandler;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResourceRequestHandler;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.BackgroundFactory;

import org.w3c.dom.Document;

import com.intridea.blackberry.twitteroauth.logger.Logger;
import com.intridea.blackberry.twitteroauth.oauth.Action;
import com.intridea.blackberry.twitteroauth.oauth.ActionListener;
import com.intridea.blackberry.twitteroauth.oauth.TwitterOAuthService;
import com.intridea.blackberry.twitteroauth.ui.EvenlySpacedHorizontalFieldManager;
import com.intridea.blackberry.twitteroauth.ui.ProgressAnimationField;
import com.intridea.blackberry.twitteroauth.util.StringUtils;

/**
 * The login screen, most of the code is from Facebook-blackberry-SDK. By changing the connection-factory. and adding
 * the action-listener part.
 * 
 * @author yincan
 */
public class TwitterLoginScreen extends MainScreen implements BrowserFieldNavigationRequestHandler,
        BrowserFieldResourceRequestHandler {
    public static final String ACTION_SUCCESS = "success";
    public static final String ACTION_LOGGED_IN = "loggedIn";
    public static final String ACTION_ERROR = "error";
    private boolean inError = false;

    protected Vector actionListeners = new Vector();

    protected String url;
    protected String loadingText;

    protected BrowserFieldConfig bfc;
    protected BrowserField bf;
    protected MyBrowserFieldListener listener;

    protected VerticalFieldManager vfm;
    protected EvenlySpacedHorizontalFieldManager hfm1;
    protected EvenlySpacedHorizontalFieldManager hfm2;
    protected ProgressAnimationField spinner;
    private TwitterOAuthService consumer;
    
    protected void onUiEngineAttached(boolean attached) {
        if (!attached) {
            if (!inError && consumer.hasToken()) {
                fireAction(ACTION_LOGGED_IN);
            }
        } else {
            consumer.requestToken();
            if (consumer.hasToken()) {
                url = consumer.getAuthorizationUrl();
                fetch();
            } else {
                inError = true;
                fireAction(ACTION_ERROR);
            }
        }
    }

    public TwitterLoginScreen(TwitterOAuthService consumer) {
        super();
        this.consumer = consumer;
        loadingText = "Connecting to Twitter";

        bfc = new BrowserFieldConfig();
        bfc.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
        bfc.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
        bfc.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.TRUE);
        bfc.setProperty(BrowserFieldConfig.MDS_TRANSCODING_ENABLED, Boolean.FALSE);
        bfc.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
        bfc.setProperty(BrowserFieldConfig.VIEWPORT_WIDTH, new Integer(Display.getWidth()));
        bfc.setProperty(BrowserFieldConfig.CONNECTION_FACTORY, new ConnectionFactory());

        bf = new BrowserField(bfc);

        listener = new MyBrowserFieldListener();
        bf.addListener(listener);

        ((ProtocolController) bf.getController()).setNavigationRequestHandler("http", this);
        ((ProtocolController) bf.getController()).setResourceRequestHandler("http", this);
        ((ProtocolController) bf.getController()).setNavigationRequestHandler("https", this);
        ((ProtocolController) bf.getController()).setResourceRequestHandler("https", this);

        // Init Screen
        attachTransition(TransitionContext.TRANSITION_FADE);
        getMainManager().setBackground(BackgroundFactory.createSolidBackground(Color.WHITE));

        hfm1 = new EvenlySpacedHorizontalFieldManager(USE_ALL_WIDTH);
        hfm1.add(new LabelField("\n" + loadingText));

        hfm2 = new EvenlySpacedHorizontalFieldManager(USE_ALL_WIDTH);
        spinner = new ProgressAnimationField(Bitmap.getBitmapResource("spinner2.png"), 6, Field.FIELD_HCENTER);
        spinner.setMargin(15, 15, 15, 15);
        hfm2.add(spinner);

        vfm = new VerticalFieldManager(USE_ALL_WIDTH);
        vfm.add(hfm1);
        vfm.add(hfm2);
        
        showLoading();
    }

    protected void showContent() {
        if (vfm.getScreen() != null) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                delete(vfm);
            }
        }
        if (bf.getScreen() == null) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                add(bf);
            }
        }
    }

    protected void showLoading() {
        if (vfm.getScreen() == null) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                add(vfm);
            }
        }
        if (bf.getScreen() != null) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                delete(bf);
            }
        }
    }

    protected void fetch() {
        bf.requestContent(url);
    }

    protected boolean onSavePrompt() {
        return true;
    }

    protected void call() {
        if (!isDisplayed()) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                UiApplication.getUiApplication().pushScreen(this);
            }
        }
    }

    public void dismiss() {
        if (isDisplayed()) {
            synchronized (net.rim.device.api.system.Application.getEventLock()) {
                UiApplication.getUiApplication().popScreen(this);
            }
        }
    }

    public void handleNavigation(BrowserFieldRequest request) throws Exception {
        Logger.info("BF-Navigate: " + request.getURL());
        if (shouldFetchContent(request)) {
            request.setURL(StringUtils.fixHttpsUrlPrefix(request.getURL()));
            bf.displayContent(handleResource(request), request.getURL());
        }
    }

    public InputConnection handleResource(BrowserFieldRequest request) throws Exception {
        return bf.getConnectionManager().makeRequest(request);
    }

    protected boolean shouldFetchContent(BrowserFieldRequest request) {
        // waiting for subclasses to override.
        return true;
    }

    protected boolean shouldShowContent(BrowserField pbf, Document pdoc) {
        // waiting for subclasses to override.
        return true;
    }

    protected boolean postProcessing(BrowserField pbf, Document pdoc) {
        // waiting for subclasses to override.
        return true;
    }

    protected class MyBrowserFieldListener extends BrowserFieldListener {

        public void documentLoaded(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DocumentLoaded(): " + document.getDocumentURI());
            if (shouldShowContent(browserField, document)) {
                showContent();
            }
            postProcessing(browserField, document);
        }

        public void documentAborted(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DocumentAborted(): " + document.getDocumentURI());
        }

        public void documentCreated(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DocumentCreated(): " + document.getDocumentURI());
        }

        public void documentError(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DocumentError(): " + document.getDocumentURI());
        }

        public void documentUnloading(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DocumentUnloading(): " + document.getDocumentURI());
        }

        public void downloadProgress(BrowserField browserField, Document document) throws Exception {
            Logger.info("BF-DownloadProgress(): " + document.getDocumentURI());
        }

    }

    protected void attachTransition(int transitionType) {
        UiEngineInstance engine = Ui.getUiEngineInstance();
        TransitionContext pushAction = null;
        TransitionContext popAction = null;

        switch (transitionType) {

        case TransitionContext.TRANSITION_FADE:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_FADE);
            popAction = new TransitionContext(TransitionContext.TRANSITION_FADE);
            pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            break;

        case TransitionContext.TRANSITION_NONE:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
            popAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
            break;

        case TransitionContext.TRANSITION_SLIDE:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
            popAction = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
            pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            pushAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
            popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            popAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
            break;

        case TransitionContext.TRANSITION_WIPE:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_WIPE);
            popAction = new TransitionContext(TransitionContext.TRANSITION_WIPE);
            pushAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            pushAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);
            popAction.setIntAttribute(TransitionContext.ATTR_DURATION, 300);
            popAction.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_RIGHT);
            break;

        case TransitionContext.TRANSITION_ZOOM:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
            popAction = new TransitionContext(TransitionContext.TRANSITION_ZOOM);
            pushAction.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_IN);
            popAction.setIntAttribute(TransitionContext.ATTR_KIND, TransitionContext.KIND_OUT);
            break;

        default:
            pushAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
            popAction = new TransitionContext(TransitionContext.TRANSITION_NONE);
        }

        engine.setTransition(null, this, UiEngineInstance.TRIGGER_PUSH, pushAction);
        engine.setTransition(this, null, UiEngineInstance.TRIGGER_POP, popAction);
    }

    public void addActionListener(ActionListener actionListener) {
        if (actionListener != null) {
            actionListeners.addElement(actionListener);
        }
    }

    protected void fireAction(String action) {
        fireAction(action, null);
    }

    protected void fireAction(String action, Object data) {
        Enumeration listenersEnum = actionListeners.elements();
        while (listenersEnum.hasMoreElements()) {
            ((ActionListener) listenersEnum.nextElement()).onAction(new Action(this, action, data));
        }
    }

}
