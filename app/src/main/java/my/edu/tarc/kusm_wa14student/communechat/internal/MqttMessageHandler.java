package my.edu.tarc.kusm_wa14student.communechat.internal;

import java.util.ArrayList;

import my.edu.tarc.kusm_wa14student.communechat.model.Contact;
import my.edu.tarc.kusm_wa14student.communechat.model.User;

/**
 * Created by Xeosz on 27-Sep-17.
 */

public class MqttMessageHandler {
    private final static String RESERVED_STRING = "000000000000000000000000";

    //Static Variables
    private static String REQ_AUTHENTICATION = "003801";
    private static String ACK_AUTHENTICATION = "003802";
    private static String REQ_CONTACT_LIST = "003810";
    private static String ACK_CONTACT_LIST = "003811";
    private static String REQ_CONTACT_DETAILS = "003812";
    private static String ACK_CONTACT_DETAILS = "003813";
    private static String REQ_NEARBY_FRIENDS = "003814";
    private static String ACK_NEARBY_FRIENDS = "003815";
    private static String REQ_SEARCH_USER = "003816";
    private static String ACK_SEARCH_USER = "003817";
    private static String REQ_RECOMMEND_FRIENDS = "003818";
    private static String ACK_RECOMMEND_FRIENDS = "003819";
    private static String REQ_FRIEND_REQUEST = "003820";
    private static String ACK_FRIEND_REQUEST = "003821";
    private static String REQ_FRIEND_REQUEST_LIST = "003822";
    private static String ACK_FRIEND_REQUEST_LIST = "003823";
    private static String REQ_RESPONSE_FRIEND_REQUEST = "003824";
    private static String ACK_RESPONSE_FRIEND_REQUEST = "003825";
    private static String KEEP_ALIVE = "003999";

    public MqttCommand mqttCommand;
    private String publish;
    private String received;
    //FORMAT

    //Command Range 003801 - 004000
    //Reserved 24Bytes / 24Chars

    public MqttMessageHandler() {
    }
    public String getPublish() {
        return publish;
    }
    public void setReceived(String received) {
        this.received = received;
        this.decode(received);
    }
    public void encode(MqttCommand command, Object data) {
        StringBuilder sb = new StringBuilder();
        String result = null;
        switch (command) {
            case REQ_AUTHENTICATION: {
                String[] credential = (String[]) data;
                sb.append(REQ_AUTHENTICATION
                        + RESERVED_STRING
                        + String.format("%03d", credential[0].length())
                        + credential[0]
                        + String.format("%03d", credential[1].length())
                        + credential[1]);
                result = sb.toString();
                break;
            }
            case REQ_CONTACT_LIST: {
                String uid;
                uid = (String) data;
                sb.append(REQ_CONTACT_LIST
                        + RESERVED_STRING
                        + uid);
                result = sb.toString();
                break;
            }
            case REQ_CONTACT_DETAILS: {
                String uid;
                uid = (String) data;
                sb.append(REQ_CONTACT_DETAILS
                        + RESERVED_STRING
                        + uid);
                result = sb.toString();
                break;
            }
            case REQ_NEARBY_FRIENDS: {
                String uid = (String) data;
                sb.append(REQ_NEARBY_FRIENDS
                        + RESERVED_STRING
                        + uid);
                result = sb.toString();
                break;
            }
            case REQ_SEARCH_USER: {
                String searchString = (String) data;
                sb.append(REQ_SEARCH_USER
                        + RESERVED_STRING
                        + searchString);
                result = sb.toString();
                break;
            }
            case REQ_RECOMMEND_FRIENDS: {
                String uid = (String) data;
                sb.append(REQ_RECOMMEND_FRIENDS
                        + RESERVED_STRING
                        + uid);
                result = sb.toString();
                break;
            }
            case REQ_FRIEND_REQUEST: {
                String[] id = (String[]) data;
                sb.append(REQ_FRIEND_REQUEST
                        + RESERVED_STRING
                        + id[0]
                        + id[1]);
                result = sb.toString();
                break;
            }
            case REQ_FRIEND_REQUEST_LIST: {
                String uid = (String) data;
                sb.append(REQ_FRIEND_REQUEST_LIST
                        + RESERVED_STRING
                        + uid);
                result = sb.toString();
                break;
            }
            case REQ_RESPONSE_FRIEND_REQUEST: {
                String[] id = (String[]) data;
                sb.append(REQ_RESPONSE_FRIEND_REQUEST
                        + RESERVED_STRING
                        + id[0]
                        + id[1]);
                result = sb.toString();
                break;
            }
            case KEEP_ALIVE: {
                String[] update = (String[]) data;
                sb.append(KEEP_ALIVE
                        + RESERVED_STRING
                        + update[0]
                        + update[1]
                        + String.format("%02d", update[2].length())
                        + update[2]
                        + String.format("%02d", update[3].length())
                        + update[3]);
                result = sb.toString();
                break;
            }
        }
        this.publish = result;
    }

    private void decode(String msg) {
        if (!msg.isEmpty() && msg.length() >= 30) {
            String data = msg.substring(0, 6);

            if (data.equalsIgnoreCase(ACK_AUTHENTICATION)) {
                this.mqttCommand = MqttCommand.ACK_AUTHENTICATION;

            } else if (data.equalsIgnoreCase(ACK_CONTACT_LIST)) {
                this.mqttCommand = MqttCommand.ACK_CONTACT_LIST;

            } else if (data.equalsIgnoreCase(ACK_CONTACT_DETAILS)) {
                this.mqttCommand = MqttCommand.ACK_CONTACT_DETAILS;

            } else if (data.equalsIgnoreCase(ACK_NEARBY_FRIENDS)) {
                this.mqttCommand = MqttCommand.ACK_NEARBY_FRIENDS;

            } else if (data.equalsIgnoreCase(ACK_SEARCH_USER)) {
                this.mqttCommand = MqttCommand.ACK_SEARCH_USER;

            } else if (data.equalsIgnoreCase(ACK_RECOMMEND_FRIENDS)) {
                this.mqttCommand = MqttCommand.ACK_RECOMMEND_FRIENDS;

            } else if (data.equalsIgnoreCase(ACK_FRIEND_REQUEST)) {
                this.mqttCommand = MqttCommand.ACK_FRIEND_REQUEST;

            } else if (data.equalsIgnoreCase(ACK_FRIEND_REQUEST_LIST)) {
                this.mqttCommand = MqttCommand.ACK_FRIEND_REQUEST_LIST;

            } else if (data.equalsIgnoreCase(ACK_RESPONSE_FRIEND_REQUEST)) {
                this.mqttCommand = MqttCommand.ACK_RESPONSE_FRIEND_REQUEST;
            } else
                this.mqttCommand = null;
        }
    }
    public int isLoginAuthenticated() {
        if (this.mqttCommand == MqttCommand.ACK_AUTHENTICATION) {
            String data = received.substring(30);
            if (data.equalsIgnoreCase("1")) {
                return 1;
            } else if (data.equalsIgnoreCase("2")) {
                return 2;
            } else
                return 3;
        } else
            return 4;
    }

    public User getUserData() {
        User user = new User();
        if (this.mqttCommand == MqttCommand.ACK_AUTHENTICATION) {
            received = received.substring(30);
            int temp = 0;
            String data = received;

            user.setStudent_id(data.substring(0, 10));
            data = data.substring(10);

            user.setFaculty(data.substring(0, 4));
            data = data.substring(4);

            user.setCourse(data.substring(0, 3));
            data = data.substring(3);

            user.setTutorial_group(Integer.parseInt(data.substring(0, 2)));
            data = data.substring(2);

            user.setIntake(data.substring(0, 6));
            data = data.substring(6);

            user.setAcademic_year(Integer.parseInt(data.substring(0, 1)));
            data = data.substring(1);

            user.setUid(Integer.parseInt(data.substring(0, 10)));
            data = data.substring(10);

            user.setGender(Integer.parseInt(data.substring(0, 1)));
            data = data.substring(1);

            user.setBirth_year(Integer.parseInt(data.substring(0, 4)));
            data = data.substring(4);

            user.setBirth_month(Integer.parseInt(data.substring(0, 2)));
            data = data.substring(2);

            user.setBirth_day(Integer.parseInt(data.substring(0, 2)));
            data = data.substring(2);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setUsername(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setNickname(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setPassword(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setStatus(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setPhone_number(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setEmail(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setAddress(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setTown(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setState(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setPostal_code(data.substring(0, temp));
            data = data.substring(temp);


            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            user.setCountry(data.substring(0, temp));

        }
        return user;
    }

    public ArrayList<Contact> getContactList() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (this.mqttCommand == MqttCommand.ACK_CONTACT_LIST) {
            received = received.substring(30);
            int temp = 0;
            String data = received;
            // id  / sizeof / nickname / sizeof / status
            while (!data.isEmpty()) {
                Contact contact = new Contact();

                contact.setUid(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setNickname(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setStatus(data.substring(0, temp));
                data = data.substring(temp);

                contacts.add(contact);
            }
        }
        return contacts;
    }

    public Contact getContactDetails() {
        Contact contact = new Contact();
        if (this.mqttCommand == MqttCommand.ACK_CONTACT_DETAILS) {
            received = received.substring(30);
            int temp;
            String data = received;

            contact.setUid(Integer.parseInt(data.substring(0, 10)));
            data = data.substring(10);

            contact.setGender(Integer.parseInt(data.substring(0, 1)));
            data = data.substring(1);

            contact.setLast_online(Integer.parseInt(data.substring(0, 10)));
            data = data.substring(10);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            contact.setUsername(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            contact.setNickname(data.substring(0, temp));
            data = data.substring(temp);

            temp = Integer.parseInt(data.substring(0, 3));
            data = data.substring(3);
            contact.setStatus(data.substring(0, temp));
        }
        return contact;
    }

    public ArrayList<Contact> getNearbyFriends() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (mqttCommand == MqttCommand.ACK_NEARBY_FRIENDS) {
            received = received.substring(30);
            int temp;
            String data = received;
            while (!data.isEmpty()) {
                Contact contact = new Contact();

                contact.setUid(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setNickname(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 1));
                data = data.substring(1);
                contact.setDistance(Integer.parseInt(data.substring(0, temp)));
                data = data.substring(temp);

                contacts.add(contact);
            }
        }
        return contacts;
    }

    public ArrayList<Contact> getSearchResultByName() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (mqttCommand == MqttCommand.ACK_SEARCH_USER) {
            received = received.substring(30);
            int temp;
            String data = received;
            while (!data.isEmpty()) {
                Contact contact = new Contact();

                contact.setUid(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                contact.setGender(Integer.parseInt(data.substring(0, 1)));
                data = data.substring(1);

                contact.setLast_online(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setUsername(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setNickname(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setStatus(data.substring(0, temp));
                data = data.substring(temp);

                contacts.add(contact);
            }
        }
        return contacts;
    }

    public ArrayList<Contact> getRecommendedFriends() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (this.mqttCommand == MqttCommand.ACK_RECOMMEND_FRIENDS) {
            received = received.substring(30);
            int temp;
            String data = received;
            while (!data.isEmpty()) {
                Contact contact = new Contact();

                contact.setUid(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setNickname(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 1));
                data = data.substring(1);
                contact.setEdges(Integer.parseInt(data.substring(0, temp)));
                data = data.substring(temp);

                contacts.add(contact);
            }
        }
        return contacts;
    }

    public ArrayList<Contact> getFriendRequestList() {
        ArrayList<Contact> contacts = new ArrayList<>();
        if (this.mqttCommand == MqttCommand.ACK_FRIEND_REQUEST_LIST) {
            received = received.substring(30);
            int temp = 0;
            String data = received;

            while (!data.isEmpty()) {
                Contact contact = new Contact();

                contact.setUid(Integer.parseInt(data.substring(0, 10)));
                data = data.substring(10);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setNickname(data.substring(0, temp));
                data = data.substring(temp);

                temp = Integer.parseInt(data.substring(0, 3));
                data = data.substring(3);
                contact.setStatus(data.substring(0, temp));
                data = data.substring(temp);

                contacts.add(contact);
            }
        }
        return contacts;
    }

    protected boolean isReceiving() {
        return (this.mqttCommand == MqttCommand.ACK_AUTHENTICATION ||
                this.mqttCommand == MqttCommand.ACK_CONTACT_LIST ||
                this.mqttCommand == MqttCommand.ACK_SEARCH_USER ||
                this.mqttCommand == MqttCommand.ACK_CONTACT_DETAILS ||
                this.mqttCommand == MqttCommand.ACK_NEARBY_FRIENDS ||
                this.mqttCommand == MqttCommand.ACK_FRIEND_REQUEST ||
                this.mqttCommand == MqttCommand.ACK_FRIEND_REQUEST_LIST ||
                this.mqttCommand == MqttCommand.ACK_RECOMMEND_FRIENDS ||
                this.mqttCommand == MqttCommand.ACK_RESPONSE_FRIEND_REQUEST);
    }

    public enum MqttCommand {
        REQ_AUTHENTICATION,
        ACK_AUTHENTICATION,
        REQ_CONTACT_LIST,
        ACK_CONTACT_LIST,
        REQ_CONTACT_DETAILS,
        ACK_CONTACT_DETAILS,
        REQ_NEARBY_FRIENDS,
        ACK_NEARBY_FRIENDS,
        REQ_FRIEND_REQUEST,
        ACK_FRIEND_REQUEST,
        REQ_FRIEND_REQUEST_LIST,
        ACK_FRIEND_REQUEST_LIST,
        REQ_RESPONSE_FRIEND_REQUEST,
        ACK_RESPONSE_FRIEND_REQUEST,
        REQ_SEARCH_USER,
        ACK_SEARCH_USER,
        REQ_RECOMMEND_FRIENDS,
        ACK_RECOMMEND_FRIENDS,
        KEEP_ALIVE;
    }

}
