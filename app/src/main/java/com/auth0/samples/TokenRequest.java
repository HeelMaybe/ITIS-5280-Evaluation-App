package com.auth0.samples;

public class TokenRequest {
    public String client_id;
    public String client_secret;
    public String audience;
    public String grant_type;

    public TokenRequest() {
        this.client_id = "vITbRWOxJx6e95BglMOHST2q5kHY7vfc";
        this.client_secret = "raU04BRx4lRG4G1oeHq8S-1JvNqR9svwTTiCLmO9G3qdejlct68pELuR4tPAkmYJ";
        this.audience = "https://itis5280-project10-api.herokuapp.com/";
        this.grant_type = "client_credentials";
    }
}
