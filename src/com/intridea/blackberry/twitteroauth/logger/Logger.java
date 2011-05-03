/*
 * Copyright 2010, Intridea, Inc.
 */
package com.intridea.blackberry.twitteroauth.logger;

import com.intridea.blackberry.twitteroauth.MyApp;

import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.EventLogger;

/**
 * <p>
 * The logger class takes the logging of the app by:
 * <ul>
 * <li>Adding the log into the Device Event Log, which can be viewed by user. (Alt + lglg)</li>
 * <li>If it is simulator, will print out the message when debugging.</li>
 * </ul>
 * </p>
 * 
 * @author yincan
 */
public class Logger {

    public static int LEVEL_INFO = EventLogger.INFORMATION;
    public static int LEVEL_WARNING = EventLogger.WARNING;
    public static int LEVEL_ERROR = EventLogger.ERROR;
    public static int LEVEL_SEVERE_ERROR = EventLogger.SEVERE_ERROR;

    public static String LOG_PREFIX_INFO = "I: ";
    public static String LOG_PREFIX_WARN = "W: ";
    public static String LOG_PREFIX_ERROR = "E: ";
    public static String LOG_PREFIX_SEVERE = "S: ";

    public static boolean DEBUG = false;

    static {
        EventLogger.register(MyApp.APPLICATION_GUID, MyApp.APPLICATION_NAME,
                EventLogger.VIEWER_STRING);
    }

    // ----------------------------------------------
    // Shared code
    // ----------------------------------------------

    /**
     * Debug. Not actually used in this project, here in case useful for someone else.
     */
    public static void debug(String eventData) {
        // output debug for JDE debugging
        if (DEBUG) {
            Logger.logEvent(true, Logger.LEVEL_INFO, "D : " + eventData);
        }
    }

    // -----------------------------------------------------------------------------
    // Quick Access to Logging.

    public static void info(String eventData) {
        Logger.logEvent(true, Logger.LEVEL_INFO, Logger.LOG_PREFIX_INFO + eventData);
    }

    public static void warn(String eventData) {
        Logger.logEvent(true, Logger.LEVEL_WARNING, Logger.LOG_PREFIX_WARN + eventData);
    }

    public static void error(String eventData) {
        Logger.logEvent(true, Logger.LEVEL_ERROR, Logger.LOG_PREFIX_ERROR + eventData);
    }

    public static void serve(String eventData) {
        Logger.logEvent(true, Logger.LEVEL_SEVERE_ERROR, Logger.LOG_PREFIX_SEVERE + eventData);
    }

    /**
     * Log Event.
     */
    public static void logEvent(boolean logging, int level, String eventData) {
        if (logging) {
            // Trap event into the BB log.
            EventLogger.logEvent(MyApp.APPLICATION_GUID, eventData.getBytes(), EventLogger.ALWAYS_LOG);
            System.out.println(eventData);
        } else if (DeviceInfo.isSimulator()) {
            System.out.println(eventData);
        }
    }

}
