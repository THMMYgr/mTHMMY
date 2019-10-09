package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Calendar;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import timber.log.Timber;

public class UploadFieldsBuilderActivity extends BaseActivity {

    static final String BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_NAME = "BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_NAME";
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_MINIFIED_NAME = "BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_MINIFIED_NAME";
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_GREEKLISH_NAME = "BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_GREEKLISH_NAME";
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER = "UPLOAD_FIELD_BUILDER_SEMESTER";

    static final String RESULT_FILENAME = "RESULT_FILENAME";
    static final String RESULT_TITLE = "RESULT_TITLE";
    static final String RESULT_DESCRIPTION = "RESULT_DESCRIPTION";

    private String courseName, courseMinifiedName, courseGreeklishName, semester;
    private boolean isValidYear;

    private LinearLayout semesterChooserLinear;
    private RadioGroup typeRadio, semesterRadio;
    private EditText year;

    private TextWatcher customYearWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String working = s.toString();

            if (working.length() == 4) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int inputYear = Integer.parseInt(working);

                isValidYear = inputYear <= currentYear && inputYear > 1980;
            } else
                isValidYear = false;

            if (!isValidYear)
                year.setError("Please enter a valid year");
            else
                year.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_fields_builder);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            courseName = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_NAME);
            courseMinifiedName = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_MINIFIED_NAME);
            courseGreeklishName = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE_GREEKLISH_NAME);
            semester = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER);
            if (courseName == null || courseName.equals("") || semester == null || semester.equals("")) {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                Timber.e("Bundle came empty in %s", UploadFieldsBuilderActivity.class.getSimpleName());

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        }

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.upload_fields_builder_toolbar_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(UPLOAD_ID, false);

        semesterChooserLinear = findViewById(R.id.upload_fields_builder_choose_semester);
        semesterRadio = findViewById(R.id.upload_fields_builder_semester_radio_group);
        semesterRadio.check(Integer.parseInt(semester) % 2 == 0
                ? R.id.upload_fields_builder_radio_button_jun
                : R.id.upload_fields_builder_radio_button_feb);

        year = findViewById(R.id.upload_fields_builder_year);
        year.addTextChangedListener(customYearWatcher);

        typeRadio = findViewById(R.id.upload_fields_builder_type_radio_group);
        typeRadio.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.upload_fields_builder_radio_button_notes) {
                semesterChooserLinear.setVisibility(View.GONE);
            } else {
                semesterChooserLinear.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.upload_fields_builder_submit).setOnClickListener(view -> {
            int typeId = typeRadio.getCheckedRadioButtonId(),
                    semesterId = semesterRadio.getCheckedRadioButtonId();
            if (typeId == -1) {
                Toast.makeText(view.getContext(), "Please choose a type for the upload", Toast.LENGTH_SHORT).show();
                return;
            } else if (semesterChooserLinear.getVisibility() == View.VISIBLE && semesterId == -1) {
                Toast.makeText(view.getContext(), "Please choose a semester for the upload", Toast.LENGTH_SHORT).show();
                return;
            } else if (year.getText().toString().isEmpty() || !isValidYear) {
                Toast.makeText(view.getContext(), "Please choose a valid year for the upload", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra(RESULT_FILENAME, buildFilename());
            returnIntent.putExtra(RESULT_TITLE, buildTitle());
            returnIntent.putExtra(RESULT_DESCRIPTION, buildDescription());
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        });
    }

    @Nullable
    private String buildFilename() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return courseGreeklishName + "_" + getGreeklishPeriod() + "_" + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return courseGreeklishName + "_" + getGreeklishPeriod() + "_" + year.getText().toString() + "_Lyseis";
            case R.id.upload_fields_builder_radio_button_notes:
                return courseGreeklishName + "_" + year.getText().toString() + "_Simeioseis";
            default:
                return null;
        }
    }

    @Nullable
    private String buildTitle() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return "[" + courseMinifiedName + "] " + "Θέματα εξετάσεων " + getPeriod() + " " + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return "[" + courseMinifiedName + "] " + "Λύσεις θεμάτων " + getPeriod() + " " + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_notes:
                return "[" + courseMinifiedName + "] " + "Σημειώσεις παραδόσεων " + year.getText().toString();
            default:
                return null;
        }
    }

    private String buildDescription() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return "Θέματα εξετάσεων " + getPeriod() + " " + year.getText().toString() + " του μαθήματος \"" + courseName + "\"";
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return "Λύσεις των θεμάτων των εξετάσεων " + getPeriod() + " " + year.getText().toString() + " του μαθήματος \"" + courseName + "\"";
            case R.id.upload_fields_builder_radio_button_notes:
                return "Σημειώσεις των παραδόσεων του μαθήματος \"" + courseName + "\" από το " + year.getText().toString();
            default:
                return null;
        }
    }

    private String getGreeklishPeriod() {
        switch (semesterRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_feb:
                return "FEB";
            case R.id.upload_fields_builder_radio_button_jun:
                return "IOY";
            case R.id.upload_fields_builder_radio_button_sept:
                return "SEP";
            default:
                return null;
        }
    }

    private String getPeriod() {
        switch (semesterRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_feb:
                return "Φεβρουαρίου";
            case R.id.upload_fields_builder_radio_button_jun:
                return "Ιουνίου";
            case R.id.upload_fields_builder_radio_button_sept:
                return "Σεπτεμβρίου";
            default:
                return null;
        }
    }
}