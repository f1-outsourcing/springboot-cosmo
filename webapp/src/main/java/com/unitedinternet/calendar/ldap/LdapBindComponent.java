package com.unitedinternet.calendar.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;

@Component
public class LdapBindComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LdapBindComponent.class);

    private String ldapAuthBase;

    private String ldapAuthUserPattern;

    private String ldapUrls;

    private String managerUsername;

    private String managerPassword;

    private String ldapTlsReqcert;

    private DirContext context;

    public LdapBindComponent(
            @Value("${ldap.auth.base}") String ldapAuthBase,
            @Value("${ldap.auth.user.pattern}") String ldapAuthUserPattern,
            @Value("${ldap.urls}") String ldapUrls,
            @Value("${ldap.auth.manager.username:#{null}}") String managerUsername,
            @Value("${ldap.auth.manager.password:#{null}}") String managerPassword,
            @Value("${ldap.tls-reqcert}") String ldapTlsReqcert
    ) {
        this.ldapAuthBase = ldapAuthBase;
        this.ldapAuthUserPattern = ldapAuthUserPattern;
        this.ldapUrls = ldapUrls;
        this.managerUsername = managerUsername;
        this.managerPassword = managerPassword;
        this.ldapTlsReqcert = ldapTlsReqcert;
    }

    public DirContext connect(Authentication authentication){

        String userName = authentication.getName();
        String password = authentication.getCredentials().toString();
        if(managerUsername !=null && !managerUsername.isEmpty()) {
            userName = managerUsername;
            password = managerPassword;
        }

        String ldapUser = ldapAuthUserPattern.replace("{0}",userName)+","+ldapAuthBase;

        Hashtable<String, String> env = new Hashtable<>();
        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(javax.naming.Context.PROVIDER_URL, ldapUrls);
        env.put(javax.naming.Context.SECURITY_AUTHENTICATION, "simple");
        env.put(javax.naming.Context.SECURITY_PRINCIPAL, ldapUser);
        env.put(javax.naming.Context.SECURITY_CREDENTIALS, password);

        env.put("java.naming.ldap.version", "3");

        env.put("java.naming.security.authentication", "simple");

        env.put("java.naming.ldap.attributes.binary", "tls_reqcert=" + ldapTlsReqcert);

        try {
            context = new InitialDirContext(env);
            LOGGER.info("LDAP bind successful...");
        } catch (NamingException e) {
            LOGGER.error("LDAP bind failed...");
            return null;
        }
        return context;
    }

    public DirContext getContext(){
        return context;
    }

}
