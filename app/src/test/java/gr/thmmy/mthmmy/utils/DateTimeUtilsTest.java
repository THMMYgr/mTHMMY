package gr.thmmy.mthmmy.utils;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static gr.thmmy.mthmmy.utils.DateTimeUtils.getRelativeTimeSpanString;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DateTimeUtils.class)
public class DateTimeUtilsTest {
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();

    private final long NOW = System.currentTimeMillis();
    private final String [] expectedRelativeTimeSpans = {
            "just now",
            "just now",
            "just now",
            "1m",
            "1m",
            "1m",
            "2m",
            "3m",
            "1h",
            "1h 15m",
            "2h",
            "2h 20m",
            "4h",
            "20h",
            "21h",
            "21h",
            "21h",
            "22h",
            "1d",
            "1d",
            "2d",
            "2d",
            "3d",
            "16d",
            "1 month",
            "2 months",
            "1 year",
            "1 year",
            "2 years",
            "a long time ago"
    };

    private final long [] times = {
            NOW,
            newDT().minusSeconds(44).getMillis(),
            newDT().minusSeconds(44).minusMillis(500).getMillis(),
            newDT().minusSeconds(45).getMillis(),
            newDT().minusSeconds(89).getMillis(),
            newDT().minusSeconds(89).minusMillis(500).getMillis(),
            newDT().minusSeconds(90).getMillis(),
            newDT().minusMinutes(3).minusSeconds(10).getMillis(),
            newDT().minusHours(1).minusMinutes(4).getMillis(),
            newDT().minusHours(1).minusMinutes(15).getMillis(),
            newDT().minusHours(2).minusMinutes(4).getMillis(),
            newDT().minusHours(2).minusMinutes(20).getMillis(),
            newDT().minusHours(3).minusMinutes(51).getMillis(),
            newDT().minusHours(20).minusMinutes(10).getMillis(),
            newDT().minusHours(20).minusMinutes(30).getMillis(),
            newDT().minusHours(21).getMillis(),
            newDT().minusHours(21).minusMinutes(29).getMillis(),
            newDT().minusHours(21).minusMinutes(30).getMillis(),
            newDT().minusHours(22).minusMinutes(30).getMillis(),
            newDT().minusHours(34).getMillis(),
            newDT().minusHours(38).getMillis(),
            newDT().minusDays(2).minusHours(10).getMillis(),
            newDT().minusDays(2).minusHours(17).getMillis(),
            newDT().minusDays(16).getMillis(),
            newDT().minusDays(30+12).getMillis(),
            newDT().minusDays(2*30+14).getMillis(),
            newDT().minusDays(14*30).getMillis(),
            newDT().minusMonths(15).getMillis(),
            newDT().minusMonths(22).getMillis(),
            newDT().minusYears(22).getMillis()
    };

    private DateTime newDT(){
        return new DateTime(NOW);
    }

    @Test
    public void relativeTimeSpansAreConvertedCorrectly() {
        PowerMockito.mockStatic(System.class);
        when(System.currentTimeMillis()).thenReturn(NOW);

        String[] timeStrings = new String[times.length];

        for(int i=0; i<times.length; i++)
            timeStrings[i] = getRelativeTimeSpanString(times[i]);

        assertArrayEquals(expectedRelativeTimeSpans,timeStrings);
    }
}
