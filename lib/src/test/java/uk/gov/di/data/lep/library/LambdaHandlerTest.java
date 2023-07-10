package uk.gov.di.data.lep.library;


import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class LambdaHandlerTest {
    @Mock
    private Context context;
    
    private LambdaHandler underTest = new TestLambda();

    @Test
    public void fds(){
        underTest.handleRequest("htdfdh", context);
    }

    class TestLambda extends LambdaHandler<String, String> {
        @Override
        public String process(String input){
            return input;
        }
    }
}
