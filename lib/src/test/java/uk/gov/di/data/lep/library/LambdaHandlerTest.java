package uk.gov.di.data.lep.library;

import org.junit.jupiter.api.Test;
import uk.gov.di.data.lep.library.dto.GroDeathEventEnrichedData;

public class LambdaHandlerTest {
    private final LambdaHandler<GroDeathEventEnrichedData> underTest = new TestLambda();

    @Test
    public void publishPublishesMessageToQueue(){
        var output = new GroDeathEventEnrichedData();

        underTest.publish(output);
    }

    static class TestLambda extends LambdaHandler<GroDeathEventEnrichedData> {
    }
}
