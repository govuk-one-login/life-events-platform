package uk.gov.di.data.lep;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
@JsonDeserialize(builder = PolicyDocument.Builder.class)
public class PolicyDocument {
    public final String Version = "2012-10-17";
    public List<Statement> Statement;
    private PolicyDocument(Builder builder) {
        this.Statement = builder.statements;
    }
    public static Builder builder(){
        return new Builder();
    }
    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private List<Statement> statements;
        private Builder() {
            statements = new ArrayList<Statement>();
        }
        public Builder statements(List<Statement> statements) {
            this.statements = statements;
            return this;
        }
        public PolicyDocument build() {
            return new PolicyDocument(this);
        }
    }
}
