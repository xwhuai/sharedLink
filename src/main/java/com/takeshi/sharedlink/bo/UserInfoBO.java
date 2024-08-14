package com.takeshi.sharedlink.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serial;
import java.io.Serializable;

/**
 * UserInfoBO
 *
 * @author 七濑武【Nanase Takeshi】
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoBO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Boolean state;

    private Data data;

    private String error_msg;

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getError_msg() {
        return error_msg;
    }

    public void setError_msg(String error_msg) {
        this.error_msg = error_msg;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String uid;

        private String uname;

        private Vip vip;

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUname() {
            return uname;
        }

        public void setUname(String uname) {
            this.uname = uname;
        }

        public Vip getVip() {
            return vip;
        }

        public void setVip(Vip vip) {
            this.vip = vip;
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Vip implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private Boolean is_vip;

        private Boolean is_forever;

        private Boolean vip;

        private String expire;

        private String expire_str;

        public Boolean getIs_vip() {
            return is_vip;
        }

        public void setIs_vip(Boolean is_vip) {
            this.is_vip = is_vip;
        }

        public Boolean getIs_forever() {
            return is_forever;
        }

        public void setIs_forever(Boolean is_forever) {
            this.is_forever = is_forever;
        }

        public Boolean getVip() {
            return vip;
        }

        public void setVip(Boolean vip) {
            this.vip = vip;
        }

        public String getExpire() {
            return expire;
        }

        public void setExpire(String expire) {
            this.expire = expire;
        }

        public String getExpire_str() {
            return expire_str;
        }

        public void setExpire_str(String expire_str) {
            this.expire_str = expire_str;
        }

    }

}
