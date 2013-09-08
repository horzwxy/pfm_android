package me.horzwxy.app.pfm.android.model;

/**
 * Created by horz on 9/8/13.
 */
public class Person {

    private String nickname;
    private String account;

    public Person( String account ) {
        setAccount( account );
        setNickname( account ); // default nickname is account
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
