package com.android.webex.spinsai.models;

public class User {

    private static String patient = "patient";

    public static int PATIENT = 1;
    private static int PROVIDER = 2;

    private String provider;
    private String csnId;
    private String patId;
    private String provName;
    private String patName;
    private String provId;

    public User(String provider, String csnId,
                String patId, String provName, String patName, String provId) {
        this.provider = provider;
        this.csnId = csnId;
        this.patId = patId;
        this.provName = provName;
        this.patName = patName;
        this.provId = provId;
    }

    public User(String csnId, String patId) {
        provider = patient;
        this.csnId = csnId;
        this.patId = patId;
    }

    public int getProvider() {
        if (provider != null && provider.equalsIgnoreCase(patient)) {
            return PATIENT;
        } else {
            return PROVIDER;
        }
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCsnId() {
        return csnId;
    }

    public void setCsnId(String csnId) {
        this.csnId = csnId;
    }

    public String getPatId() {
        return patId;
    }

    public void setPatId(String patId) {
        this.patId = patId;
    }

    public String getProvName() {
        return provName;
    }

    public void setProvName(String provName) {
        this.provName = provName;
    }

    public String getPatName() {
        return patName;
    }

    public void setPatName(String patName) {
        this.patName = patName;
    }

    public String getProvId() {
        return provId;
    }

    public void setProvId(String provId) {
        this.provId = provId;
    }
}
