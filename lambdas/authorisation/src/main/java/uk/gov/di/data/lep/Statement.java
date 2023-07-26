package uk.gov.di.data.lep;

public class Statement {
    public final String Action = "execute-api:Invoke";
    public String Effect;
    public String Resource;

    public Statement(String effect, String resource) {
        this.Effect = effect;
        this.Resource = resource;
    }
}
