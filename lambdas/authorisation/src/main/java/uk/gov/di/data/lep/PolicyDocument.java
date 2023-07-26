package uk.gov.di.data.lep;

import java.util.Collections;
import java.util.List;

public class PolicyDocument {
    public String Version = "2012-10-17";
    public List<Statement> Statement;

    public PolicyDocument(Statement statement) {
        this.Statement = Collections.singletonList(statement);
    }
}
