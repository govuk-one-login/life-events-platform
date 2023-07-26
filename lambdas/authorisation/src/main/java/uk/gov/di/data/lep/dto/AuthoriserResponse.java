package uk.gov.di.data.lep.dto;

import java.util.Map;

public class AuthoriserResponse {
    public String principalId;
    public PolicyDocument policyDocument;
    public Map<String, String> context;

    public AuthoriserResponse(String principalId, String effect, String resource, Map<String, String> context) {
        var statement = new Statement(effect, resource);
        var policyDocument = new PolicyDocument(statement);
        this.principalId = principalId;
        this.policyDocument = policyDocument;
        this.context = context;
    }
}
