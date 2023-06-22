package lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.di.data.lep.Main;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MainTest {
    @Mock
    private Context context;

    private final Main underTest = new Main();

    @Test
    void handleRequestReturnsHello() {
        var result = underTest.handleRequest("h", context);

        assertEquals("hello", result);
    }
}
