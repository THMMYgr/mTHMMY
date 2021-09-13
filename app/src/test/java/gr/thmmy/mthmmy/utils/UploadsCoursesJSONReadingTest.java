package gr.thmmy.mthmmy.utils;

import net.lachlanmckee.timberjunit.TimberTestRule;

import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.HashMap;


import gr.thmmy.mthmmy.activities.upload.UploadsCourse;
import gr.thmmy.mthmmy.utils.io.ResourceUtils;

import static gr.thmmy.mthmmy.activities.upload.UploadsCourse.generateCoursesFromJSON;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@PrepareForTest(JSONObject.class)
public class UploadsCoursesJSONReadingTest {
    private final String filePath = "/raw/uploads_courses.json";

    @Rule
    public TimberTestRule logAllAlwaysRule = TimberTestRule.logAllAlways();

    @Test
    public void uploadsCoursesRetrievedCorrectly() throws Exception {
        InputStream is = this.getClass().getResourceAsStream(filePath);
        assertNotNull(is);
        String uploadsCoursesJSON = ResourceUtils.readJSONResourceToString(is);
        assertNotNull(uploadsCoursesJSON);;
        JSONObject jsonObject = new JSONObject(uploadsCoursesJSON);
        assertTrue(jsonObject.has("categories"));
        HashMap<Integer, UploadsCourse> coursesHashMap = generateCoursesFromJSON(jsonObject);
        assertEquals(coursesHashMap.size(), 216);
    }
}
