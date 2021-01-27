package com.sscctv.seeeyesonvif.Utils;

public class IPUtilsCGI {
    public static final String REQUEST_HTTP = "http://";
    public static final String CGI_GET = "/cgi-bin/config.cgi";
    public static final String CGI_SET = "/cgi-bin/setup.cgi";
    public static final String CGI_ID = "?id=";
    public static final String CGI_PASS = "&pw=";
    public static final String CGI_MODE_GET = "&mode=get";
    public static final String CGI_MODE_SET = "&mode=set";
    public static final String CGI_GROUP = "&group=";

    /*  Service Group - System  */

    public static final String CGI_SYSTEM = "system";
    public static final String CGI_SYSTEM_INFO = ".sysinfo";
    public static final String CGI_SYSTEM_TIME = ".systime";
    public static final String CGI_SYSTEM_TIME_NTP = ".systime.ntp";
    public static final String CGI_SYSTEM_LOG = ".log";
    public static final String CGI_SYSTEM_USER = ".user";
    public static final String CGI_SYSTEM_INIT = ".initialize";
    public static final String CGI_SYSTEM_SDCARD = ".sdcard";

    /*  Service Group - Video  */

    public static final String CGI_VIDEO = "video";
    public static final String CGI_CAMERA_SET = "camera";
    public static final String CGI_STREAM_SET = "streaming";
    public static final String CGI_TEXT_OVERLAY = "overlay";
    public static final String CGI_PRIVACY_MASK = "privacymask";

    /*  Service Group - Audio   */

    public static final String CGI_AUDIO = "audio";

    /*  Service Group - Network */

    public static final String CGI_NETWORK = "network";
    public static final String CGI_IP_SET = "ipaddress";
    public static final String CGI_WEB_PORT_SET = "http";
    public static final String CGI_SMTP_SET = "smtp";
    public static final String CGI_FTP_SET = "ftp";
    public static final String CGI_HTTP_DIGEST = "security";
    public static final String CGI_RTSP = "rtsp";
    public static final String CGI_SNMP = "snmp";
    public static final String CGI_UPNP = "upnp";
    public static final String CGI_QoS = "qos";
    public static final String CGI_ETHERNET = "ethernet";
    public static final String CGI_IP_FILTERING = "ipfiltering";

    /*  Service Group - Event */

    public static final String CGI_EVENT = "event";
    public static final String CGI_MOTION_SET = "motion";
    public static final String CGI_TAMPERING_SET = "tampering";
    public static final String CGI_ALARM_IN_SET = "alaram_in_mode";
    public static final String CGI_EVENT_TRIGGER = "trigger";

    public static final String CGI_ALARM_STATUS = "root.alarm_io.status=";
    public static final String CGI_BASIC_ALARM_STATUS = "/Basic.cgi?root_action_list=alarmstatus";
    public static final String CGI_EM_SET = "/cgi-bin/setup_emergency.cgi?group=config.interface&mode=";

}
