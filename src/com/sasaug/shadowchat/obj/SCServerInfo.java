package com.sasaug.shadowchat.obj;

public class SCServerInfo {
	public static final int VERIFICATION_METHOD_EMAIL = 0;
    public static final int VERIFICATION_METHOD_SMS = 1;

    public static final int SECURITY_NONE = 0;  //no security
    public static final int SECURITY_E2E_NORMAL = 1;    //end to end, only single chat
    public static final int SECURITY_E2E_COMPLETE = 2;  //end to end include group chat


    private boolean register = false;
    private boolean register_email = false;
    private boolean register_phone = false;
    private boolean register_password = false;

    private int verification_method = VERIFICATION_METHOD_EMAIL;


    private int security = SECURITY_E2E_NORMAL;

    public boolean isRegister() {
        return register;
    }

    public void setRegister(boolean register) {
        this.register = register;
    }

    public boolean isRegisterEmail() {
        return register_email;
    }

    public void setRegisterEmail(boolean register_email) {
        this.register_email = register_email;
    }

    public boolean isRegisterPhone() {
        return register_phone;
    }

    public void setRegisterPhone(boolean register_phone) {
        this.register_phone = register_phone;
    }

    public boolean isRegisterPassword() {
        return register_password;
    }

    public void setRegisterPassword(boolean register_password) {
        this.register_password = register_password;
    }

    public int getVerificationMethod() {
        return verification_method;
    }

    public void setVerificationMethod(int verification_method) {
        this.verification_method = verification_method;
    }

    public int getSecurity() {
        return security;
    }

    public void setSecurity(int security) {
        this.security = security;
    }
}
