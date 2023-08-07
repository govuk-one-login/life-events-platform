package uk.gov.di.data.lep;

import com.amazonaws.services.lambda.runtime.Context;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class PullFileTest {
    private static final PullFile underTest = new PullFile();
    private static final Context context = mock(Context.class);

    @Test
    void constructionCallsCorrectInstantiation() {
        new PullFile();
    }
}
