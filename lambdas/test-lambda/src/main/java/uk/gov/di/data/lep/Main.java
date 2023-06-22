package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class Main implements RequestHandler<String, String> {
    @Override
    public String handleRequest(String input, Context context) {
        return "hello";
    }
}
