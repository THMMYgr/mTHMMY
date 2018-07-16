package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import timber.log.Timber;

public class UploadFieldsBuilderActivity extends BaseActivity {
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_COURSE = "UPLOAD_FIELD_BUILDER_COURSE";
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER = "UPLOAD_FIELD_BUILDER_SEMESTER";

    static final String RESULT_FILENAME = "RESULT_FILENAME";
    static final String RESULT_TITLE = "RESULT_TITLE";
    static final String RESULT_DESCRIPTION = "RESULT_DESCRIPTION";

    private String course, semester;

    //UI elements
    private LinearLayout semesterChooserLinear;
    private RadioGroup typeRadio, semesterRadio;
    private EditText year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_fields_builder);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            course = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE);
            semester = extras.getString(BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER);
            if (course == null || course.equals("") || semester == null || semester.equals("")) {
                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                Timber.e("Bundle came empty in %s", UploadFieldsBuilderActivity.class.getSimpleName());

                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        }

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Upload fields builder");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(UPLOAD_ID);

        semesterChooserLinear = findViewById(R.id.upload_fields_builder_choose_semester);
        semesterRadio = findViewById(R.id.upload_fields_builder_semester_radio_group);
        year = findViewById(R.id.upload_fields_builder_year);

        typeRadio = findViewById(R.id.upload_fields_builder_type_radio_group);
        typeRadio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.upload_fields_builder_radio_button_notes) {
                    semesterChooserLinear.setVisibility(View.GONE);
                } else {
                    semesterChooserLinear.setVisibility(View.VISIBLE);
                }
            }
        });

        findViewById(R.id.upload_fields_builder_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int typeId = typeRadio.getCheckedRadioButtonId(),
                        semesterId = semesterRadio.getCheckedRadioButtonId();
                if (typeId == -1) {
                    Toast.makeText(view.getContext(), "Please choose a type for the upload", Toast.LENGTH_SHORT).show();
                    return;
                } else if (semesterChooserLinear.getVisibility() == View.VISIBLE && semesterId == -1) {
                    Toast.makeText(view.getContext(), "Please choose a semester for the upload", Toast.LENGTH_SHORT).show();
                    return;
                } else if (year.getText().toString().isEmpty()) {
                    Toast.makeText(view.getContext(), "Please choose a year for the upload", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent returnIntent = new Intent();
                returnIntent.putExtra(RESULT_FILENAME, buildFilename());
                returnIntent.putExtra(RESULT_TITLE, buildTitle());
                returnIntent.putExtra(RESULT_DESCRIPTION, buildDescription());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
    }

    @Nullable
    private String buildFilename() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return getGreeklishCourseName() + "_" + getGreeklishPeriod() + "_" + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return getGreeklishCourseName() + "_" + getGreeklishPeriod() + "_" + year.getText().toString() + "_Lyseis";
            case R.id.upload_fields_builder_radio_button_notes:
                return getGreeklishCourseName() + "_" + year.getText().toString() + "_Shmeiwseis";
            default:
                return null;
        }
    }

    @Nullable
    private String buildTitle() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return getMinifiedCourseName() + " - " + "Θέματα εξετάσεων " + getPeriod() + " " + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return getMinifiedCourseName() + " - " + "Λύσεις θεμάτων " + getPeriod() + " " + year.getText().toString();
            case R.id.upload_fields_builder_radio_button_notes:
                return getMinifiedCourseName() + " - " + "Σημειώσεις παραδόσεων " + year.getText().toString();
            default:
                return null;
        }
    }

    private String buildDescription() {
        switch (typeRadio.getCheckedRadioButtonId()) {
            case R.id.upload_fields_builder_radio_button_exams:
                return "Θέματα εξετάσεων " + getPeriod() + " " + year.getText().toString() + " του μαθήματος \"" + course + "\"";
            case R.id.upload_fields_builder_radio_button_exam_solutions:
                return "Λύσεις των θεμάτων των εξετάσεων " + getPeriod() + " " + year.getText().toString() + " του μαθήματος \"" + course + "\"";
            case R.id.upload_fields_builder_radio_button_notes:
                return "Σημειώσεις των παραδόσεων του μαθήματος \"" + course + "\" από το " + year.getText().toString();
            default:
                return null;
        }
    }

    private String getGreeklishCourseName() {
        //TODO
        return "";
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

    private String getMinifiedCourseName() {
        //TODO
        return "";
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