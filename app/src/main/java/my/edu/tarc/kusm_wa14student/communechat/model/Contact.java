package my.edu.tarc.kusm_wa14student.communechat.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * Created by Xeosz on 27-Sep-17.
 */

public class Contact {
    private int uid;
    private String username;
    private String nickname;
    private int gender;
    private String status;
    private int last_online;
    private String phone_number;
    private byte[] image;

    public Contact() {

    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public int getLast_online() {
        return last_online;
    }

    public void setLast_online(int last_online) {
        this.last_online = last_online;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    //Constructor w/o image
    public Contact(int uid, String username, String nickname, int gender, String status, int last_online, String phone_number) {
        this.uid = uid;
        this.username = username;
        this.nickname = nickname;
        this.gender = gender;
        this.last_online = last_online;
        this.status = status;
        this.phone_number = phone_number;

    }


    private class DbBitmapUtility {

        // convert from bitmap to byte array
        public byte[] getBytes(Bitmap bitmap) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        // convert from byte array to bitmap
        public Bitmap getImage(byte[] image) {
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
}