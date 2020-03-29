package gr.thmmy.mthmmy.utils.parsing;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StringUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void extractUserCodeFromUrl() {
        String url = "https://www.thmmy.gr/smf/index.php?action=profile;u=14670";
        assertEquals(StringUtils.extractUserCodeFromUrl(url), 14670);
    }
}