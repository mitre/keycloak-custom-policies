package org.keycloak;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.admin.client.Keycloak;
import javax.ws.rs.core.Response;
import java.io.*;
import static org.junit.jupiter.api.Assertions.fail;

public class PasswordMinTimePolicyTest{

    private static final String KEYCLAOK_URL="http://localhost:8080";
    private static final String CLIENT="admin-cli";
    private static ClientRepresentation clientBeforeChanges;
    private static final String TEST_USER="test-user";
    
    @BeforeAll
    public static void initRealmForTest()throws IOException{
        Keycloak keycloak=Keycloak.getInstance(KEYCLAOK_URL,"master","admin","admin",CLIENT);
        clientBeforeChanges=keycloak.realms().realm("master").clients().findByClientId(CLIENT).get(0);
        createTestUser("admin","admin","master",TEST_USER,"passwo1rd");
        setPasswordPolicy("digits");
    }
    @AfterAll
    public static void resetRealm(){
        Keycloak keycloak = Keycloak.getInstance(KEYCLAOK_URL,"master","admin","admin",CLIENT);
        UserRepresentation user = keycloak.realm("master").users().search(TEST_USER).get(0);
        keycloak.realm("master").users().delete(user.getId());
        if(clientBeforeChanges != null){
            keycloak.realms().realm("master").clients().get(clientBeforeChanges.getId()).update(clientBeforeChanges);
        }
    }
    


    @Test
    void policyPassTest() throws IOException{
       //This test assesses if password policy passes a password when password complies with policy
        try {
            //This password meets password policy, update should be successful
            resetPassword("admin", "admin", "master", "pass1Word"); 
        } catch (Exception e) {
            fail("Password met password policy. There's an error in updating passowrd");
        }

    }
    @Test
    void policyFailTest() throws IOException{
        //This test assesses if password policy fails a password when password does not comply with policy
        try {
            resetPassword("admin", "admin", "master", "password");
            // If an exception does not occur, the password policy is not enforced correctly.
            fail("An exception should occur. Password does not meet password policy");
        } catch (Exception e) {
        }

    }


    private static void setPasswordPolicy (String policy){
        Keycloak keycloak = Keycloak.getInstance(KEYCLAOK_URL,"master","admin","admin",CLIENT);
        RealmRepresentation realmRepresentation = keycloak.realms().realm("master").toRepresentation();
        realmRepresentation.setPasswordPolicy(policy);
        keycloak.realms().realm("master").update(realmRepresentation);
    }

    private static void createTestUser(String username, String password, String realmName, String newUsername, String newPassword){
        Keycloak keycloak = Keycloak.getInstance(KEYCLAOK_URL,"master",username,password,CLIENT);

        keycloak.serverInfo().getInfo().getPasswordPolicies();

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(newUsername);
        userRepresentation.setEnabled(true);
        Response response = keycloak.realms().realm(realmName).users().create(userRepresentation);

        response.close();

        resetPassword(username, password, realmName, newPassword);
    }

    private static void resetPassword(String username, String password, String realmName,String newPassword){
        Keycloak keycloak = Keycloak.getInstance(KEYCLAOK_URL,"master",username,password,CLIENT);
        UserRepresentation user = keycloak.realm("master").users().search(TEST_USER).get(0);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(newPassword);
        credentialRepresentation.setTemporary((false));


        keycloak.realms().realm(realmName).users().get(user.getId()).resetPassword(credentialRepresentation);
    }
}