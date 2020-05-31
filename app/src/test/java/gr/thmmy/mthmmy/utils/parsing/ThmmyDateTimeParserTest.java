package gr.thmmy.mthmmy.utils.parsing;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static gr.thmmy.mthmmy.utils.parsing.ThmmyDateTimeParser.convertToTimestamp;
import static gr.thmmy.mthmmy.utils.parsing.ThmmyDateTimeParser.purifyTodayDateTime;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.support.membermodification.MemberMatcher.method;
import static org.powermock.api.support.membermodification.MemberModifier.stub;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ThmmyDateTimeParser.class)
public class ThmmyDateTimeParserTest {
    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();

    private static final String TIME_ZONE ="Europe/Athens";
    private static final String GET_DTZ ="getDtz";

    private final String [] expTimestamps={"1569245936000","1569191627000","1570050809000"};
    private final String [][] dateTimes = {
        {
            "Σεπτεμβρίου 23, 2019, 16:38:56",
            "Σεπτεμβρίου 23, 2019, 16:38:56 pm",
            "Σεπτεμβρίου 23, 2019, 04:38:56 pm",
            "23 Σεπτεμβρίου 2019, 16:38:56",
            "2019-09-23, 16:38:56",
            "23-09-2019, 16:38:56"
        },
        {
            "Σεπτεμβρίου 23, 2019, 01:33:47",
            "Σεπτεμβρίου 23, 2019, 01:33:47 am",
            "23 Σεπτεμβρίου 2019, 01:33:47",
            "23-09-2019, 01:33:47",
            "2019-09-23, 01:33:47"
        },
        {
            "Οκτωβρίου 03, 2019, 12:13:29 am",
            "Οκτωβρίου 03, 2019, 00:13:29 am"
        }
    };

    @Test
    public void dateTimesAreConvertedCorrectly() {
        stub(method(ThmmyDateTimeParser.class, GET_DTZ)).toReturn(DateTimeZone.forID(TIME_ZONE));

        String[][] expectedTimeStamps = new String[dateTimes.length][];
        String[][] timeStamps = new String[dateTimes.length][];

        for(int i=0; i<dateTimes.length; i++){
            timeStamps[i] = new String[dateTimes[i].length];
            expectedTimeStamps[i] = new String[dateTimes[i].length];
            for(int j=0; j<dateTimes[i].length; j++){
                expectedTimeStamps[i][j]=expTimestamps[i];
                timeStamps[i][j]=convertToTimestamp(dateTimes[i][j]);
            }
        }

        assertArrayEquals(expectedTimeStamps, timeStamps);
    }

    private final String [] todayDateTimes = {
        "10:10:10",
        "00:58:07",
        "23:23:23",
        "09:09:09 am",
        "09:09:09 pm",
        "Today at 12:16:48",
        "Σήμερα στις 12:16:48",
        "Today at 12:16:48 am"
    };

    @Test
    public void todayDateTimesConvertToNonNull() {
        stub(method(ThmmyDateTimeParser.class, GET_DTZ)).toReturn(DateTimeZone.forID(TIME_ZONE));

        for (String todayDateTime : todayDateTimes)
            assertNotNull(convertToTimestamp(todayDateTime));
    }

    private final String [] dateTimesToBePurified = {
            "12:16:48",
            "12:16:48 am",
            "Today at 12:16:48",
            "Σήμερα στις 12:16:48",
            "Today at 12:16:48 am"
    };

    private final String [] expectedPurifiedDateTimes = {
            "12:16:48",
            "12:16:48 am",
            "12:16:48",
            "12:16:48",
            "12:16:48 am"
    };

    @Test
    public void todayDateTimesArePurifiedCorrectly(){
        String[] purifiedTodayDateTimes = new String[dateTimesToBePurified.length];

        for(int i = 0; i< dateTimesToBePurified.length; i++)
            purifiedTodayDateTimes[i] = purifyTodayDateTime(dateTimesToBePurified[i]);

        assertArrayEquals(purifiedTodayDateTimes, expectedPurifiedDateTimes);

        // Those below should remain unaltered
        String[][] purifiedDateTimes = new String[dateTimes.length][];

        for(int i=0; i<dateTimes.length; i++){
            purifiedDateTimes[i] = new String[dateTimes[i].length];
            for(int j=0; j<dateTimes[i].length; j++)
                purifiedDateTimes[i][j]=purifyTodayDateTime(dateTimes[i][j]);
        }

        assertArrayEquals(dateTimes, purifiedDateTimes);
    }
}
