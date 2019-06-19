package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            } else {
                isValidYear = false;
            }

            if (!isValidYear) {
                year.setError("Please enter a valid year");
            } else {
                year.setError(null);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }
    };

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

    @Nullable
    private String getGreeklishCourseName() {
        return getGreeklishOrMinifiedCourseName(true);
    }

    @Nullable
    private String getMinifiedCourseName() {
        return getGreeklishOrMinifiedCourseName(false);
    }

    private String normalizeLatinNumbers(String stringWithLatinNumbers) {
        String greekLatinOne = "Ι", englishLatinOne = "I";
        String normalisedString;

        //Separates the latin number suffix from the course name
        final String regex = "(.+)\\ ([IΙ]+)";
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(stringWithLatinNumbers);

        if (matcher.matches() && matcher.groupCount() == 2) {
            normalisedString = matcher.group(1) + " " + matcher.group(2).replaceAll(greekLatinOne, englishLatinOne);
        } else {
            normalisedString = stringWithLatinNumbers;
        }

        return normalisedString;
    }

    @Nullable
    private String getGreeklishOrMinifiedCourseName(boolean greeklish) {
        String normalisedCourse = normalizeLatinNumbers(course);

        if (normalisedCourse.contains(("Ψηφιακή Επεξεργασία Σήματος"))) {
            return greeklish ? "PSES" : "ΨΕΣ";
        } else if (normalisedCourse.contains(("Ψηφιακή Επεξεργασία Εικόνας"))) {
            return greeklish ? "psee" : "ΨΕΕ";
        } else if (normalisedCourse.contains(("Ψηφιακές Τηλεπικοινωνίες II"))) {
            return greeklish ? "pshf_thlep_II" : "Ψηφιακές Τηλεπ. 2";
        } else if (normalisedCourse.contains(("Ψηφιακές Τηλεπικοινωνίες I"))) {
            return greeklish ? "pshf_thlep_I" : "Ψηφιακές Τηλεπ. 1";
        } else if (normalisedCourse.contains(("Ψηφιακά Φίλτρα"))) {
            return greeklish ? "filtra" : "Φίλτρα";
        } else if (normalisedCourse.contains(("Ψηφιακά Συστήματα III"))) {
            return greeklish ? "pshfiaka_III" : "Ψηφιακά 3";
        } else if (normalisedCourse.contains(("Ψηφιακά Συστήματα II"))) {
            return greeklish ? "pshfiaka_II" : "Ψηφιακά 2";
        } else if (normalisedCourse.contains(("Ψηφιακά Συστήματα I"))) {
            return greeklish ? "pshfiaka_I" : "Ψηφιακά 1";
        } else if (normalisedCourse.contains(("Φωτονική Τεχνολογία"))) {
            return greeklish ? "fwtonikh" : "Φωτονική";
        } else if (normalisedCourse.contains(("Φυσική I"))) {
            return greeklish ? "fysikh_I" : "Φυσική 1";
        } else if (normalisedCourse.contains(("Υψηλές Τάσεις III"))) {
            return greeklish ? "ypshles_III" : "Υψηλές 3";
        } else if (normalisedCourse.contains(("Υψηλές Τάσεις II"))) {
            return greeklish ? "ypshles_II" : "Υψηλές 2";
        } else if (normalisedCourse.contains(("Υψηλές Τάσεις I"))) {
            return greeklish ? "ypshles_I" : "Υψηλές 1";
        } else if (normalisedCourse.contains(("Υψηλές Τάσεις 4"))) {
            return greeklish ? "ypshles_IV" : "Υψηλές 4";
        } else if (normalisedCourse.contains(("Υπολογιστικός Ηλεκτρομαγνητισμός"))) {
            return greeklish ? "ypologistikos_HM" : "Υπολογιστικός Η/Μ";
        } else if (normalisedCourse.contains(("Υπολογιστικές Μέθοδοι στα Ενεργειακά Συστήματα"))) {
            return greeklish ? "ymes" : "ΥΜΕΣ";
        } else if (normalisedCourse.contains(("Τηλεπικοινωνιακή Ηλεκτρονική"))) {
            return greeklish ? "tilep_ilektr" : "Τηλεπ. Ηλεκτρ.";
        } else if (normalisedCourse.contains(("Τηλεοπτικά Συστήματα"))) {
            return greeklish ? "tileoptika" : "Τηλεοπτικά";
        } else if (normalisedCourse.contains(("Τεχνολογία Λογισμικού"))) {
            return greeklish ? "SE" : "Τεχνολογία Λογισμικού";
        } else if (normalisedCourse.contains(("Τεχνολογία Ηλεκτροτεχνικών Υλικών"))) {
            return greeklish ? "Hlektrotexnika_Ylika" : "Ηλεκτροτεχνικά Υλικά";
        } else if (normalisedCourse.contains(("Τεχνολογία Ήχου και Εικόνας"))) {
            return greeklish ? "texn_hxoy_eikonas" : "Τεχνολογία Ήχου και Εικόνας";
        } else if (normalisedCourse.contains(("Τεχνική Μηχανική"))) {
            return greeklish ? "texn_mhxan" : "Τεχν. Μηχαν.";
        } else if (normalisedCourse.contains(("Τεχνικές μη Καταστρεπτικών Δοκιμών"))) {
            return greeklish ? "non_destructive_tests" : "Μη Καταστρεπτικές Δοκιμές";
        } else if (normalisedCourse.contains(("Τεχνικές Σχεδίασης με Η/Υ"))) {
            return greeklish ? "sxedio" : "Σχέδιο";
        } else if (normalisedCourse.contains(("Τεχνικές Κωδικοποίησης"))) {
            return greeklish ? "texn_kwdikopoihshs" : "Τεχνικές Κωδικοποίησης";
        } else if (normalisedCourse.contains(("Τεχνικές Βελτιστοποίησης"))) {
            return greeklish ? "veltistopoihsh" : "Βελτιστοποίηση";
        } else if (normalisedCourse.contains(("Σύνθεση Τηλεπικοινωνιακών Διατάξεων"))) {
            return greeklish ? "synth_thlep_diataksewn" : "Σύνθεση Τηλεπ. Διατάξεων";
        } else if (normalisedCourse.contains(("Σύνθεση Ενεργών και Παθητικών Κυκλωμάτων"))) {
            return greeklish ? "synthesh" : "Σύνθεση";
        } else if (normalisedCourse.contains(("Σχεδίαση Συστημάτων VLSI"))) {
            return greeklish ? "VLSI" : "VLSI";
        } else if (normalisedCourse.contains(("Συστήματα Υπολογιστών (Υπολογιστικά Συστήματα)"))) {
            return greeklish ? "sys_ypologistwn" : "Συσ. Υπολογιστών";
        } else if (normalisedCourse.contains(("Συστήματα Πολυμέσων και Εικονική Πραγματικότητα"))) {
            return greeklish ? "polymesa" : "Πολυμέσα";
        } else if (normalisedCourse.contains(("Συστήματα Μικροϋπολογιστών"))) {
            return greeklish ? "mikro_I" : "Μίκρο 1";
        } else if (normalisedCourse.contains(("Συστήματα Ηλεκτροκίνησης"))) {
            return greeklish ? "hlektrokinhsh" : "Ηλεκτροκίνηση";
        } else if (normalisedCourse.contains(("Συστήματα Ηλεκτρικής Ενέργειας III"))) {
            return greeklish ? "SHE_III" : "ΣΗΕ 3";
        } else if (normalisedCourse.contains(("Συστήματα Ηλεκτρικής Ενέργειας II"))) {
            return greeklish ? "SHE_II" : "ΣΗΕ 2";
        } else if (normalisedCourse.contains(("Συστήματα Ηλεκτρικής Ενέργειας I"))) {
            return greeklish ? "SHE_I" : "ΣΗΕ 1";
        } else if (normalisedCourse.contains(("Συστήματα Αυτομάτου Ελέγχου III"))) {
            return greeklish ? "SAE_III" : "ΣΑΕ 3";
        } else if (normalisedCourse.contains(("Συστήματα Αυτομάτου Ελέγχου II"))) {
            return greeklish ? "SAE_II" : "ΣΑΕ 2";
        } else if (normalisedCourse.contains(("Συστήματα Αυτομάτου Ελέγχου I"))) {
            return greeklish ? "SAE_1" : "ΣΑΕ 1";
        } else if (normalisedCourse.contains(("Στοχαστικό Σήμα"))) {
            return greeklish ? "stox_shma" : "Στοχ. Σήμα";
        } else if (normalisedCourse.contains(("Σταθμοί Παραγωγής Ηλεκτρικής Ενέργειας"))) {
            return greeklish ? "SPHE" : "ΣΠΗΕ";
        } else if (normalisedCourse.contains(("Σερβοκινητήρια Συστήματα"))) {
            return greeklish ? "servo" : "Σέρβο";
        } else if (normalisedCourse.contains(("Σήματα και Συστήματα"))) {
            return greeklish ? "analog_shma" : "Σύματα & Συστήματα";
        } else if (normalisedCourse.contains(("Ρομποτική"))) {
            return greeklish ? "rompotikh" : "Ρομποτική";
        } else if (normalisedCourse.contains(("Προσομοίωση και Μοντελοποίηση Συστημάτων"))) {
            return greeklish ? "montelopoihsh" : "Μοντελοποίηση";
        } else if (normalisedCourse.contains(("Προηγμένες Τεχνικές Επεξεργασίας Σήματος"))) {
            return greeklish ? "ptes" : "ΠΤΕΣ";
        } else if (normalisedCourse.contains(("Προγραμματιστικές Τεχνικές"))) {
            return greeklish ? "cpp" : "Προγραμματ. Τεχν.";
        } else if (normalisedCourse.contains(("Προγραμματιζόμενα Κυκλώματα ASIC"))) {
            return greeklish ? "asic" : "ASIC";
        } else if (normalisedCourse.contains(("Παράλληλα και Κατανεμημένα Συστήματα"))) {
            return greeklish ? "parallhla" : "Παράλληλα";
        } else if (normalisedCourse.contains(("Οργάνωση και Διοίκηση Εργοστασίων"))) {
            return greeklish ? "organ_dioik_ergostasiwn" : "Οργάνωση και Διοίκηση Εργοστασίων";
        } else if (normalisedCourse.contains(("Οργάνωση Υπολογιστών"))) {
            return greeklish ? "org_ypol" : "Οργάνωση Υπολ.";
        } else if (normalisedCourse.contains(("Οπτική II"))) {
            return greeklish ? "optikh_II" : "Οπτική 2";
        } else if (normalisedCourse.contains(("Οπτική I"))) {
            return greeklish ? "optikh_I" : "Οπτική 1";
        } else if (normalisedCourse.contains(("Οπτικές Επικοινωνίες"))) {
            return greeklish ? "optikes_thlep" : "Οπτικές Τηλεπ.";
        } else if (normalisedCourse.contains(("Μικροκύματα II"))) {
            return greeklish ? "mikrokymata_II" : "Μικροκύματα 2";
        } else if (normalisedCourse.contains(("Μικροκύματα I"))) {
            return greeklish ? "mikrokymata_I" : "Μικροκύματα 1";
        } else if (normalisedCourse.contains(("Μικροκυματική Τηλεπισκόπηση"))) {
            return greeklish ? "thlepiskophsh" : "Τηλεπισκόπηση";
        } else if (normalisedCourse.contains(("Μικροεπεξεργαστές και Περιφερειακά"))) {
            return greeklish ? "mikro_II" : "Μίκρο 2";
        } else if (normalisedCourse.contains(("Μετάδοση Θερμότητας"))) {
            return greeklish ? "metadosi_therm" : "Μετάδοση Θερμ.";
        } else if (normalisedCourse.contains(("Λογισμός II"))) {
            return greeklish ? "logismos_II" : "Λογισμός 2";
        } else if (normalisedCourse.contains(("Λογισμός I"))) {
            return greeklish ? "logismos_I" : "Λογισμός 1";
        } else if (normalisedCourse.contains(("Λογική Σχεδίαση"))) {
            return greeklish ? "logiki_sxediash" : "Λογική Σχεδίαση";
        } else if (normalisedCourse.contains(("Λειτουργικά Συστήματα"))) {
            return greeklish ? "OS" : "Λειτουργικά";
        } else if (normalisedCourse.contains(("Κινητές και Δορυφορικές Επικοινωνίες"))) {
            return greeklish ? "kinhtes_doryforikes_epik" : "Κινητές & Δορυφορικές Επικοινωνίες";
        } else if (normalisedCourse.contains(("Κβαντική Φυσική"))) {
            return greeklish ? "kvantikh" : "Κβαντική";
        } else if (normalisedCourse.contains(("Θεωρία και Τεχνολογία Πυρηνικών Αντιδραστήρων"))) {
            return greeklish ? "texn_antidrasthrwn" : "Τεχνολογία Αντιδραστήρων";
        } else if (normalisedCourse.contains(("Θεωρία Υπολογισμών και Αλγορίθμων"))) {
            return greeklish ? "thya" : "ΘΥΑ";
        } else if (normalisedCourse.contains(("Θεωρία Σκέδασης"))) {
            return greeklish ? "skedash" : "Σκέδαση";
        } else if (normalisedCourse.contains(("Θεωρία Σημάτων και Γραμμικών Συστημάτων"))) {
            return greeklish ? "analog_shma" : "Σύματα & Συστήματα";
        } else if (normalisedCourse.contains(("Θεωρία Πληροφοριών"))) {
            return greeklish ? "theoria_plir" : "Θεωρία Πληρ.";
        } else if (normalisedCourse.contains(("Θεωρία Πιθανοτήτων και Στατιστική"))) {
            return greeklish ? "pithanothtes" : "Πιθανότητες";
        } else if (normalisedCourse.contains(("Ημιαγωγά Υλικά: Θεωρία-Διατάξεις"))) {
            return greeklish ? "Hmiagwga_Ylika" : "Ημιαγωγά Υλικά";
        } else if (normalisedCourse.contains(("Ηλεκτρονική III"))) {
            return greeklish ? "hlektronikh_III" : "Ηλεκτρονική 3";
        } else if (normalisedCourse.contains(("Ηλεκτρονική II"))) {
            return greeklish ? "hlektronikh_2" : "Ηλεκτρονική 2";
        } else if (normalisedCourse.contains(("Ηλεκτρονική I"))) {
            return greeklish ? "hlektronikh_1" : "Ηλεκτρονική 1";
        } else if (normalisedCourse.contains(("Ηλεκτρονικές Διατάξεις και Μετρήσεις"))) {
            return greeklish ? "hlektron_diatakseis_metrhseis" : "Ηλεκτρονικές Διατάξεις και Μετρήσεις";
        } else if (normalisedCourse.contains(("Ηλεκτρονικά Ισχύος II"))) {
            return greeklish ? "isxyos_II" : "Ισχύος 2";
        } else if (normalisedCourse.contains(("Ηλεκτρονικά Ισχύος I"))) {
            return greeklish ? "isxyos_I" : "Ισχύος 1";
        } else if (normalisedCourse.contains(("Ηλεκτρομαγνητικό Πεδίο II"))) {
            return greeklish ? "pedio_II" : "Πεδίο 2";
        } else if (normalisedCourse.contains(("Ηλεκτρομαγνητικό Πεδίο I"))) {
            return greeklish ? "pedio_I" : "Πεδίο 1";
        } else if (normalisedCourse.contains(("Ηλεκτρομαγνητική Συμβατότητα"))) {
            return greeklish ? "HM_symvatothta" : "H/M Συμβατότητα";
        } else if (normalisedCourse.contains(("Ηλεκτρολογικά Υλικά"))) {
            return greeklish ? "ylika" : "Ηλεκτρ. Υλικά";
        } else if (normalisedCourse.contains(("Ηλεκτρική Οικονομία"))) {
            return greeklish ? "hlektr_oikonomia" : "Ηλεκτρική Οικονομία";
        } else if (normalisedCourse.contains(("Ηλεκτρικές Μηχανές Γ'"))) {
            return greeklish ? "mhxanes_C" : "Μηχανές Γ";
        } else if (normalisedCourse.contains(("Ηλεκτρικές Μηχανές Β'"))) {
            return greeklish ? "mhxanes_B" : "Μηχανές Β";
        } else if (normalisedCourse.contains(("Ηλεκτρικές Μηχανές Α'"))) {
            return greeklish ? "mhxanes_A" : "Μηχανές Α";
        } else if (normalisedCourse.contains(("Ηλεκτρικές Μετρήσεις II"))) {
            return greeklish ? "metrhseis_II" : "Μετρήσεις 2";
        } else if (normalisedCourse.contains(("Ηλεκτρικές Μετρήσεις I"))) {
            return greeklish ? "metrhseis_1" : "Μετρήσεις 1";
        } else if (normalisedCourse.contains(("Ηλεκτρικά Κυκλώματα III"))) {
            return greeklish ? "kyklwmata_I" : "Κυκλώματα 3";
        } else if (normalisedCourse.contains(("Ηλεκτρικά Κυκλώματα II"))) {
            return greeklish ? "kyklwmata_II" : "Κυκλώματα 2";
        } else if (normalisedCourse.contains(("Ηλεκτρικά Κυκλώματα I"))) {
            return greeklish ? "kyklwmata_I" : "Κυκλώματα 1";
        } else if (normalisedCourse.contains(("Ηλεκτρακουστική II"))) {
            return greeklish ? "hlektroakoystikh_II" : "Ηλεκτροακουστική 2";
        } else if (normalisedCourse.contains(("Ηλεκτρακουστική I"))) {
            return greeklish ? "hlektroakoystikh_I" : "Ηλεκτροακουστική 1";
        } else if (normalisedCourse.contains(("Εφαρμοσμένη Θερμοδυναμική"))) {
            return greeklish ? "thermodynamikh" : "Θερμοδυναμική";
        } else if (normalisedCourse.contains(("Εφαρμοσμένα Μαθηματικά II"))) {
            return greeklish ? "efarmosmena_math_II" : "Εφαρμοσμένα 2";
        } else if (normalisedCourse.contains(("Εφαρμοσμένα Μαθηματικά I"))) {
            return greeklish ? "efarmosmena_math_I" : "Εφαρμοσμένα 1";
        } else if (normalisedCourse.contains(("Εφαρμογές Τηλεπικοινωνιακών Διατάξεων"))) {
            return greeklish ? "efarm_thlep_diataksewn" : "Εφαρμογές Τηλεπ. Διατάξεων";
        } else if (normalisedCourse.contains(("Ευφυή Συστήματα Ρομπότ"))) {
            return greeklish ? "eufuh" : "Ευφυή";
        } else if (normalisedCourse.contains(("Ευρυζωνικά Δίκτυα"))) {
            return greeklish ? "eyryzwnika" : "Ευρυζωνικά";
        } else if (normalisedCourse.contains(("Επιχειρησιακή Έρευνα"))) {
            return greeklish ? "epixeirisiaki" : "Επιχειρησιακή Έρευνα";
        } else if (normalisedCourse.contains(("Ενσωματωμένα Συστήματα Πραγματικού Χρόνου"))) {
            return greeklish ? "enswmatwmena" : "Ενσωματωμένα";
        } else if (normalisedCourse.contains(("Εισαγωγή στις εφαρμογές Πυρηνικής Τεχνολογίας"))) {
            return greeklish ? "Intro_Purhnikh_Texn" : "Εισ. Πυρηνικη Τεχν.";
        } else if (normalisedCourse.contains(("Εισαγωγή στην Πολιτική Οικονομία"))) {
            return greeklish ? "polit_oik" : "Πολιτική Οικονομία";
        } else if (normalisedCourse.contains(("Εισαγωγή στην Ενεργειακή Τεχνολογία II"))) {
            return greeklish ? "EET_2" : "ΕΕΤ2";
        } else if (normalisedCourse.contains(("Εισαγωγή στην Ενεργειακή Τεχνολογία I"))) {
            return greeklish ? "EET_I" : "ΕΕΤ 1";
        } else if (normalisedCourse.contains(("Ειδικές Κεραίες, Σύνθεση Κεραιών"))) {
            return greeklish ? "eidikes_keraies" : "Ειδικές Κεραίες, Σύνθεση Κεραιών";
        } else if (normalisedCourse.contains(("Ειδικές Αρχιτεκτονικές Υπολογιστών"))) {
            return greeklish ? "eidikes_arx_ypolog" : "Ειδικές Αρχιτεκτονικές Υπολογιστών";
        } else if (normalisedCourse.contains(("Ειδικά Κεφάλαια Συστημάτων Ηλεκτρικής Ενέργειας"))) {
            return greeklish ? "ekshe" : "ΕΚΣΗΕ";
        } else if (normalisedCourse.contains(("Ειδικά Κεφάλαια Ηλεκτρομαγνητικού Πεδίου I"))) {
            return greeklish ? "eidika_kef_HM_pedioy_I" : "Ειδικά Κεφάλαια Ηλεκτρομαγνητικού Πεδίου I";
        } else if (normalisedCourse.contains(("Ειδικά Κεφάλαια Διαφορικών Εξισώσεων"))) {
            return greeklish ? "eidika_kef_diaf_eksis" : "Ειδικά Κεφάλαια Διαφορικών Εξισώσεων";
        } else if (normalisedCourse.contains(("Δομημένος Προγραμματισμός"))) {
            return greeklish ? "C" : "Δομ. Προγραμμ.";
        } else if (normalisedCourse.contains(("Δομές Δεδομένων"))) {
            return greeklish ? "dom_dedomenwn" : "Δομ. Δεδομ.";
        } else if (normalisedCourse.contains(("Διαχείριση Συστημάτων Ηλεκτρικής Ενέργειας"))) {
            return greeklish ? "dshe" : "ΔΣΗΕ";
        } else if (normalisedCourse.contains(("Διαφορικές Εξισώσεις"))) {
            return greeklish ? "diaforikes" : "Διαφορικές";
        } else if (normalisedCourse.contains(("Διανεμημένη Παραγωγή"))) {
            return greeklish ? "dian_paragwgh" : "Διανεμημένη Παραγωγή";
        } else if (normalisedCourse.contains(("Διακριτά μαθηματικά"))) {
            return greeklish ? "diakrita" : "Διακριτά Μαθηματικά";
        } else if (normalisedCourse.contains(("Διακριτά Μαθηματικά"))) {
            return greeklish ? "diakrita" : "Διακριτά Μαθηματικά";
        } else if (normalisedCourse.contains(("Διάδοση Ηλεκτρομαγνητικού Κύματος I (πρώην Πεδίο III)"))) {
            return greeklish ? "diadosi_1" : "Διάδοση 1";
        } else if (normalisedCourse.contains(("Διάδοση Η/Μ Κύματος II"))) {
            return greeklish ? "diadosi_II" : "Διάδοση 2";
        } else if (normalisedCourse.contains(("Δίκτυα Υπολογιστών II"))) {
            return greeklish ? "diktya_II" : "Δίκτυα 2";
        } else if (normalisedCourse.contains(("Δίκτυα Υπολογιστών I"))) {
            return greeklish ? "diktya_I" : "Δίκτυα 1";
        } else if (normalisedCourse.contains(("Δίκτυα Τηλεπικοινωνιών"))) {
            return greeklish ? "diktya_thlep" : "Δίκτυα Τηλέπ.";
        } else if (normalisedCourse.contains(("Γραφική με Υπολογιστές"))) {
            return greeklish ? "grafikh" : "Γραφική";
        } else if (normalisedCourse.contains(("Γραμμική Άλγεβρα"))) {
            return greeklish ? "grammikh_algebra" : "Γραμμ. Άλγεβρ.";
        } else if (normalisedCourse.contains(("Γεωηλεκτρομαγνητισμός"))) {
            return greeklish ? "geohlektromagnitismos" : "Γεωηλεκτρομαγνητισμός";
        } else if (normalisedCourse.contains(("Βιοϊατρική Τεχνολογία"))) {
            return greeklish ? "vioiatriki" : "Βιοιατρική";
        } else if (normalisedCourse.contains(("Βιομηχανική Πληροφορική"))) {
            return greeklish ? "viomix_plir" : "Βιομηχανική Πληρ";
        } else if (normalisedCourse.contains(("Βιομηχανικά Ηλεκτρονικά"))) {
            return greeklish ? "bhomix_hlektronika" : "Βιομηχανικά Ηλεκτρονικά";
        } else if (normalisedCourse.contains(("Βάσεις Δεδομένων"))) {
            return greeklish ? "vaseis" : "Βάσεις";
        } else if (normalisedCourse.contains(("Ασύρματος Τηλεπικοινωνία II"))) {
            return greeklish ? "asyrmatos_II" : "Ασύρματος 2";
        } else if (normalisedCourse.contains(("Ασύρματος Τηλεπικοινωνία I"))) {
            return greeklish ? "asyrmatos_I" : "Ασύρματος 1";
        } else if (normalisedCourse.contains(("Ασφάλεια Πληροφοριακών Συστημάτων"))) {
            return greeklish ? "asfaleia" : "Ασφάλεια";
        } else if (normalisedCourse.contains(("Ασαφή Συστήματα"))) {
            return greeklish ? "asafh" : "Ασαφή";
        } else if (normalisedCourse.contains(("Αρχιτεκτονική Υπολογιστών"))) {
            return greeklish ? "arx_ypologistwn" : "Αρχ. Υπολογιστών";
        } else if (normalisedCourse.contains(("Αρχές Παράλληλης Επεξεργασίας"))) {
            return greeklish ? "arxes_parall_epeksergasias" : "Αρχές Παράλληλης Επεξεργασίας";
        } else if (normalisedCourse.contains(("Αρχές Οικονομίας"))) {
            return greeklish ? "arx_oikonomias" : "Αρχές Οικονομίας";
        } else if (normalisedCourse.contains(("Αριθμητική Ανάλυση"))) {
            return greeklish ? "arith_anal" : "Αριθμ. Ανάλυση";
        } else if (normalisedCourse.contains(("Αξιοπιστία Συστημάτων"))) {
            return greeklish ? "aksiopistia_systhmatwn" : "Αξιοπιστία Συστημάτων";
        } else if (normalisedCourse.contains(("Αντικειμενοστραφής Προγραμματισμός"))) {
            return greeklish ? "OOP" : "Αντικειμενοστραφής";
        } else if (normalisedCourse.contains(("Αναλογικές Τηλεπικοινωνίες (πρώην Τηλεπικοινωνιακά Συστήματα I)"))) {
            return greeklish ? "anal_thlep" : "Αναλογικές Τηλεπ.";
        } else if (normalisedCourse.contains(("Αναγνώριση Προτύπων"))) {
            return greeklish ? "protipa" : "Αναγνώριση Προτύπων";
        } else if (normalisedCourse.contains(("Ανάλυση και Σχεδίαση Αλγορίθμων"))) {
            return greeklish ? "algorithms" : "Αλγόριθμοι";
        } else if (normalisedCourse.contains(("Ανάλυση Χρονοσειρών"))) {
            return greeklish ? "xronoseires" : "Χρονοσειρές";
        } else if (normalisedCourse.contains(("Ανάλυση Συστημάτων Ηλεκτρικής Ενέργειας"))) {
            return greeklish ? "ASHE" : "ΑΣΗΕ";
        } else if (normalisedCourse.contains(("Ανάλυση Ηλεκτρικών Κυκλωμάτων με Υπολογιστή"))) {
            return greeklish ? "analysh_hlektr_kykl" : "Ανάλυση Ηλεκτρικ. Κυκλ. με Υπολογιστή";
        } else if (normalisedCourse.contains(("Ακουστική II"))) {
            return greeklish ? "akoystikh_II" : "Ακουστική 2";
        } else if (normalisedCourse.contains(("Ακουστική I"))) {
            return greeklish ? "akoystikh_I" : "Ακουστική 1";
        } else {
            Timber.wtf("Unrecognised course came in the upload fields generator! Course string = %s", course);
            return null;
        }
    }
}