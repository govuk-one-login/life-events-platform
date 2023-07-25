package uk.gov.di.data.lep;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Statement.Builder.class)
public class Statement {

    public final String Action = "execute-api:Invoke";

    public String Effect;
    public String Resource;

    private Statement(Builder builder) {
        this.Effect = builder.effect;
        this.Resource = builder.resource;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String effect;
        private String resource;

        private Builder() { }

        public Builder effect(String effect) {
            this.effect = effect;
            return this;
        }

        public Builder resource(String resource) {
            this.resource = resource;
            return this;
        }

        public Statement build() {
            return new Statement(this);
        }
    }
}
