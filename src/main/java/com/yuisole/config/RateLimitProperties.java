package com.yuisole.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Yuisole
 */
@ConfigurationProperties(prefix = "storage")
public class RateLimitProperties {
    private String mode = "local";

    private String tokenName = "token";

    private String codePrefix = "raleLimit_Code_Key";

    private String countPrefix = "raleLimit_Count_Key";


    private String coolPrefix = "raleLimit_Cool_Key";


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public String getCodePrefix() {
        return codePrefix;
    }

    public void setCodePrefix(String codePrefix) {
        this.codePrefix = codePrefix;
    }

    public String getCountPrefix() {
        return countPrefix;
    }

    public void setCountPrefix(String countPrefix) {
        this.countPrefix = countPrefix;
    }

    public String getCoolPrefix() {
        return coolPrefix;
    }

    public void setCoolPrefix(String coolPrefix) {
        this.coolPrefix = coolPrefix;
    }
}
