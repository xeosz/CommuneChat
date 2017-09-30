package my.edu.tarc.kusm_wa14student.communechat.model;

/**
 * Created by Xeosz on 28-Sep-17.
 */

public enum MqttCommand {
    REQ_CONTACT_LIST,
    ACK_CONTACT_LIST,
    REQ_CONTACT_DETAILS,
    ACK_CONTACT_DETAILS,
    REQ_USER_PROFILE,
    ACK_USER_PROFILE,
    REQ_SEARCH_USER,
    ACK_SEARCH_USER,
    KEEP_ALIVE;
}
