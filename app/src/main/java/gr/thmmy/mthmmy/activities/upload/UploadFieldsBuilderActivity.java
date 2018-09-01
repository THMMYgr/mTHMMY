package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;

import gr.thmmy.mthmmy.R;
import timber.log.Timber;

public class UploadFieldsBuilderActivity extends AppCompatActivity {
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_COURSE = "UPLOAD_FIELD_BUILDER_COURSE";
    static final String BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER = "UPLOAD_FIELD_BUILDER_SEMESTER";

    static final String RESULT_FILENAME = "RESULT_FILENAME";
    static final String RESULT_TITLE = "RESULT_TITLE";
    static final String RESULT_DESCRIPTION = "RESULT_DESCRIPTION";

    private String course, semester;

    private LinearLayout semesterChooserLinear;
    private RadioGroup typeRadio, semesterRadio;
    private EditText year;

    private TextWatcher customYearWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String working = s.toString();
            boolean isValid;

            if (working.length() == 4) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int inputYear = Integer.parseInt(working);

                isValid = inputYear <= currentYear && inputYear > 2000;
            } else {
                isValid = false;
            }

            if (!isValid) {
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.upload_fields_builder_toolbar_title);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

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


    @Nullable
    private String getGreeklishOrMinifiedCourseName(boolean greeklish) {
        //TODO fill missing values
        if (course.contains("Συστήματα Υπολογιστών (Υπολογιστικά Συστήματα)")) {
            return greeklish ? "sys_ypologistwn" : "Συσ. Υπολογιστών";
        } else if (course.contains("Τεχνική Μηχανική")) {
            return greeklish ? "texn_mhxan" : "Τεχν. Μηχαν.";
        } else if (course.contains("Διαφορικές Εξισώσεις")) {
            return greeklish ? "diaforikes" : "Διαφορικές";
        } else if (course.contains("Θεωρία Πιθανοτήτων και Στατιστική")) {
            return greeklish ? "pithanothtes" : "Πιθανότητες";
        } else if (course.contains("Εφαρμοσμένα Μαθηματικά Ι")) {
            return greeklish ? "efarmosmena_math_I" : "Εφαρμοσμένα 1";
        } else if (course.contains("Ηλεκτρικά Κυκλώματα ΙΙ")) {
            return greeklish ? "kyklwmata_II" : "Κυκλώματα 2";
        } else if (course.contains("Ηλεκτρολογικά Υλικά")) {
            return greeklish ? "ylika" : "Ηλεκτρ. Υλικά";
        } else if (course.contains("Ηλεκτρομαγνητικό Πεδίο Ι")) {
            return greeklish ? "pedio_I" : "Πεδίο 1";
        } else if (course.contains("Θεωρία Σημάτων και Γραμμικών Συστημάτων")) {
            return greeklish ? "analog_shma" : "Σύματα & Συστήματα";
        } else if (course.contains("Προγραμματιστικές Τεχνικές")) {
            return greeklish ? "cpp" : "Προγραμματ. Τεχν.";
        } else if (course.contains("Αριθμητική Ανάλυση")) {
            return greeklish ? "arith_anal" : "Αριθμ. Ανάλυση";
        } else if (course.contains("Αρχιτεκτονική Υπολογιστών")) {
            return greeklish ? "arx_ypologistwn" : "Αρχ. Υπολογιστών";
        } else if (course.contains("Εισαγωγή στην Ενεργειακή Τεχνολογία Ι")) {
            return greeklish ? "EET_I" : "ΕΕΤ 1";
        } else if (course.contains("Ηλεκτρικά Κυκλώματα ΙΙΙ")) {
            return greeklish ? "kyklwmata_I" : "Κυκλώματα 3";
        } else if (course.contains("Ηλεκτρομαγνητικό Πεδίο ΙΙ")) {
            return greeklish ? "pedio_II" : "Πεδίο 2";
        } else if (course.contains("Στοχαστικό Σήμα")) {
            return greeklish ? "stox_shma" : "Στοχ. Σήμα";
        } else if (course.contains("Ψηφιακά Συστήματα Ι")) {
            return greeklish ? "pshfiaka_I" : "Ψηφιακά 1";
        } else if (course.contains("Αναλογικές Τηλεπικοινωνίες (πρώην Τηλεπικοινωνιακά Συστήματα Ι)")) {
            return greeklish ? "anal_thlep" : "Αναλογικές Τηλεπ.";
        } else if (course.contains("Διάδοση Ηλεκτρομαγνητικού Κύματος Ι (πρώην Πεδίο ΙΙΙ)")) {
            return greeklish ? "diadosi_1" : "Διάδοση 1";
        } else if (course.contains("Δομές Δεδομένων")) {
            return greeklish ? "dom_dedomenwn" : "Δομ. Δεδομ.";
        } else if (course.contains("Εισαγωγή στην Ενεργειακή Τεχνολογία ΙΙ")) {
            return greeklish ? "EET_2" : "ΕΕΤ2";
        } else if (course.contains("Ηλεκτρικές Μετρήσεις Ι")) {
            return greeklish ? "metrhseis_1" : "Μετρήσεις 1";
        } else if (course.contains("Ηλεκτρονική ΙΙ")) {
            return greeklish ? "hlektronikh_2" : "Ηλεκτρονική 2";
        } else if (course.contains("Συστήματα Αυτομάτου Ελέγχου Ι")) {
            return greeklish ? "SAE_1" : "ΣΑΕ 1";
        } else if (course.contains("Γραμμική Άλγεβρα")) {
            return greeklish ? "grammikh_algebra" : "Γραμμ. Άλγεβρ.";
        } else if (course.contains("Δομημένος Προγραμματισμός")) {
            return greeklish ? "C" : "Δομ. Προγραμμ.";
        } else if (course.contains("Λογική Σχεδίαση")) {
            return greeklish ? "logiki_sxediash" : "Λογική Σχεδίαση";
        } else if (course.contains("Λογισμός Ι")) {
            return greeklish ? "logismos_I" : "Λογισμός 1";
        } else if (course.contains("Τεχνικές Σχεδίασης με Η/Υ")) {
            return greeklish ? "sxedio" : "Σχέδιο";
        } else if (course.contains("Φυσική Ι")) {
            return greeklish ? "fysikh_I" : "Φυσική 1";
        } else if (course.contains("Αντικειμενοστραφής Προγραμματισμός")) {
            return greeklish ? "OOP" : "Αντικειμενοστραφής";
        } else if (course.contains("Εφαρμοσμένη Θερμοδυναμική")) {
            return greeklish ? "thermodynamikh" : "Θερμοδυναμική";
        } else if (course.contains("Ηλεκτρικά Κυκλώματα Ι")) {
            return greeklish ? "kyklwmata_I" : "Κυκλώματα 1";
        } else if (course.contains("Λογισμός ΙΙ")) {
            return greeklish ? "logismos_II" : "Λογισμός 2";
        } else if (course.contains("Οργάνωση Υπολογιστών")) {
            return greeklish ? "org_ypol" : "Οργάνωση Υπολ.";
        } else if (course.contains("Ηλεκτρονική Ι")) {
            return greeklish ? "hlektronikh_1" : "Ηλεκτρονική 1";
        } else if (course.contains("Διακριτά μαθηματικά")) {
            return greeklish ? "diakrita" : "Διακριτά Μαθηματικά";
        } else if (course.contains("Σήματα και Συστήματα")) {
            return greeklish ? "analog_shma" : "Σύματα & Συστήματα";
        } else if (course.contains("Εισαγωγή στις εφαρμογές Πυρηνικής Τεχνολογίας")) {
            return greeklish ? "Intro_Purhnikh_Texn" : "Εισ. Πυρηνικη Τεχν.";
        } else if (course.contains("Επιχειρησιακή Έρευνα")) {
            return greeklish ? "epixeirisiaki" : "Επιχειρησιακή Έρευνα";
        } else if (course.contains("Ημιαγωγά Υλικά: Θεωρία-Διατάξεις")) {
            return greeklish ? "Hmiagwga_Ylika" : "Ημιαγωγά Υλικά";
        } else if (course.contains("Μετάδοση Θερμότητας")) {
            return greeklish ? "metadosi_therm" : "Μετάδοση Θερμ.";
        } else if (course.contains("Συστήματα Ηλεκτρικής Ενέργειας Ι")) {
            return greeklish ? "SHE_I" : "ΣΗΕ 1";
        } else if (course.contains("Υψηλές Τάσεις Ι")) {
            return greeklish ? "ypshles_I" : "Υψηλές 1";
        } else if (course.contains("Θεωρία και Τεχνολογία Πυρηνικών Αντιδραστήρων")) {
            return greeklish ? "texn_antidrasthrwn" : "Τεχνολογία Αντιδραστήρων";
        } else if (course.contains("Τεχνολογία Ηλεκτροτεχνικών Υλικών")) {
            return greeklish ? "Hlektrotexnika_Ylika" : "Ηλεκτροτεχνικά Υλικά";
        } else if (course.contains("Ηλεκτρικές Μηχανές Α'")) {
            return greeklish ? "mhxanes_A" : "Μηχανές Α";
        } else if (course.contains("Σταθμοί Παραγωγής Ηλεκτρικής Ενέργειας")) {
            return greeklish ? "SPHE" : "ΣΠΗΕ";
        } else if (course.contains("Συστήματα Ηλεκτρικής Ενέργειας ΙΙ")) {
            return greeklish ? "SHE_II" : "ΣΗΕ 2";
        } else if (course.contains("Υψηλές Τάσεις ΙΙ")) {
            return greeklish ? "ypshles_II" : "Υψηλές 2";
        } else if (course.contains("Αρχές Οικονομίας")) {
            return greeklish ? "arx_oikonomias" : "Αρχές Οικονομίας";
        } else if (course.contains("Διανεμημένη Παραγωγή")) {
            return greeklish ? "dian_paragwgh" : "Διανεμημένη Παραγωγή";
        } else if (course.contains("Διαχείριση Συστημάτων Ηλεκτρικής Ενέργειας")) {
            return greeklish ? "dshe" : "ΔΣΗΕ";
        } else if (course.contains("Υψηλές Τάσεις ΙΙΙ")) {
            return greeklish ? "ypshles_III" : "Υψηλές 3";
        } else if (course.contains("Ανάλυση Συστημάτων Ηλεκτρικής Ενέργειας")) {
            return greeklish ? "ASHE" : "ΑΣΗΕ";
        } else if (course.contains("Ηλεκτρικές Μηχανές Β'")) {
            return greeklish ? "mhxanes_B" : "Μηχανές Β";
        } else if (course.contains("Ηλεκτρονικά Ισχύος Ι")) {
            return greeklish ? "isxyos_I" : "Ισχύος 1";
        } else if (course.contains("Συστήματα Ηλεκτρικής Ενέργειας ΙΙΙ")) {
            return greeklish ? "SHE_III" : "ΣΗΕ 3";
        } else if (course.contains("Σερβοκινητήρια Συστήματα")) {
            return greeklish ? "servo" : "Σέρβο";
        } else if (course.contains("Συστήματα Ηλεκτροκίνησης")) {
            return greeklish ? "hlektrokinhsh" : "Ηλεκτροκίνηση";
        } else if (course.contains("Υπολογιστικές Μέθοδοι στα Ενεργειακά Συστήματα")) {
            return greeklish ? "ymes" : "ΥΜΕΣ";
        } else if (course.contains("Υψηλές Τάσεις 4")) {
            return greeklish ? "ypshles_IV" : "Υψηλές 4";
        } else if (course.contains("Ηλεκτρικές Μηχανές Γ'")) {
            return greeklish ? "mhxanes_C" : "Μηχανές Γ";
        } else if (course.contains("Ηλεκτρική Οικονομία")) {
            return greeklish ? "hlektr_oikonomia" : "Ηλεκτρική Οικονομία";
        } else if (course.contains("Ηλεκτρονικά Ισχύος ΙΙ")) {
            return greeklish ? "isxyos_II" : "Ισχύος 2";
        } else if (course.contains("Ανάλυση και Σχεδίαση Αλγορίθμων")) {
            return greeklish ? "algorithms" : "Αλγόριθμοι";
        } else if (course.contains("Διακριτά Μαθηματικά")) {
            return greeklish ? "diakrita" : "Διακριτά Μαθηματικά";
        } else if (course.contains("Κβαντική Φυσική")) {
            return greeklish ? "kvantikh" : "Κβαντική";
        } else if (course.contains("Ρομποτική")) {
            return greeklish ? "rompotikh" : "Ρομποτική";
        } else if (course.contains("Τεχνικές Βελτιστοποίησης")) {
            return greeklish ? "veltistopoihsh" : "Βελτιστοποίηση";
        } else if (course.contains("Ηλεκτρικές Μετρήσεις ΙΙ")) {
            return greeklish ? "metrhseis_II" : "Μετρήσεις 2";
        } else if (course.contains("Ηλεκτρονική ΙΙΙ")) {
            return greeklish ? "hlektronikh_III" : "Ηλεκτρονική 3";
        } else if (course.contains("Συστήματα Αυτομάτου Ελέγχου ΙΙ")) {
            return greeklish ? "SAE_II" : "ΣΑΕ 2";
        } else if (course.contains("Ψηφιακά Συστήματα ΙΙ")) {
            return greeklish ? "pshfiaka_II" : "Ψηφιακά 2";
        } else if (course.contains("Ανάλυση Χρονοσειρών")) {
            return greeklish ? "xronoseires" : "Χρονοσειρές";
        } else if (course.contains("Θεωρία Υπολογισμών και Αλγορίθμων")) {
            return greeklish ? "thya" : "ΘΥΑ";
        } else if (course.contains("Παράλληλα και Κατανεμημένα Συστήματα")) {
            return greeklish ? "parallhla" : "Παράλληλα";
        } else if (course.contains("Προγραμματιζόμενα Κυκλώματα ASIC")) {
            return greeklish ? "asic" : "ASIC";
        } else if (course.contains("Προσομοίωση και Μοντελοποίηση Συστημάτων")) {
            return greeklish ? "montelopoihsh" : "Μοντελοποίηση";
        } else if (course.contains("Συστήματα Αυτομάτου Ελέγχου ΙΙI")) {
            return greeklish ? "SAE_III" : "ΣΑΕ 3";
        } else if (course.contains("Σύνθεση Ενεργών και Παθητικών Κυκλωμάτων")) {
            return greeklish ? "synthesh" : "Σύνθεση";
        } else if (course.contains("Δίκτυα Υπολογιστών Ι")) {
            return greeklish ? "diktya_I" : "Δίκτυα 1";
        } else if (course.contains("Λειτουργικά Συστήματα")) {
            return greeklish ? "OS" : "Λειτουργικά";
        } else if (course.contains("Συστήματα Μικροϋπολογιστών")) {
            return greeklish ? "mikro_I" : "Μίκρο 1";
        } else if (course.contains("Ασαφή Συστήματα")) {
            return greeklish ? "asafh" : "Ασαφή";
        } else if (course.contains("Γραφική με Υπολογιστές")) {
            return greeklish ? "grafikh" : "Γραφική";
        } else if (course.contains("Ενσωματωμένα Συστήματα Πραγματικού Χρόνου")) {
            return greeklish ? "enswmatwmena" : "Ενσωματωμένα";
        } else if (course.contains("Τηλεπικοινωνιακή Ηλεκτρονική")) {
            return greeklish ? "tilep_ilektr" : "Τηλεπ. Ηλεκτρ.";
        } else if (course.contains("Ψηφιακά Συστήματα ΙΙΙ")) {
            return greeklish ? "pshfiaka_III" : "Ψηφιακά 3";
        } else if (course.contains("Ψηφιακή Επεξεργασία Εικόνας")) {
            return greeklish ? "psee" : "ΨΕΕ";
        } else if (course.contains("Δίκτυα Υπολογιστών ΙΙ")) {
            return greeklish ? "diktya_II" : "Δίκτυα 2";
        } else if (course.contains("Μικροεπεξεργαστές και Περιφερειακά")) {
            return greeklish ? "mikro_II" : "Μίκρο 2";
        } else if (course.contains("Τεχνολογία Λογισμικού")) {
            return greeklish ? "SE" : "Τεχνολογία Λογισμικού";
        } else if (course.contains("Ψηφιακά Φίλτρα")) {
            return greeklish ? "filtra" : "Φίλτρα";
        } else if (course.contains("Αναγνώριση Προτύπων")) {
            return greeklish ? "protipa" : "Αναγνώριση Προτύπων";
        } else if (course.contains("Ασφάλεια Πληροφοριακών Συστημάτων")) {
            return greeklish ? "asfaleia" : "Ασφάλεια";
        } else if (course.contains("Βάσεις Δεδομένων")) {
            return greeklish ? "vaseis" : "Βάσεις";
        } else if (course.contains("Βιομηχανική Πληροφορική")) {
            return greeklish ? "viomix_plir" : "Βιομηχανική Πληρ";
        } else if (course.contains("Ευφυή Συστήματα Ρομπότ")) {
            return greeklish ? "eufuh" : "Ευφυή";
        } else if (course.contains("Συστήματα Πολυμέσων και Εικονική Πραγματικότητα")) {
            return greeklish ? "polymesa" : "Πολυμέσα";
        } else if (course.contains("Σχεδίαση Συστημάτων VLSI")) {
            return greeklish ? "VLSI" : "VLSI";
        } else if (course.contains("Ακουστική Ι")) {
            return greeklish ? "akoystikh_I" : "Ακουστική 1";
        } else if (course.contains("Εφαρμοσμένα Μαθηματικά ΙΙ")) {
            return greeklish ? "efarmosmena_math_II" : "Εφαρμοσμένα 2";
        } else if (course.contains("Ηλεκτρακουστική Ι")) {
            return greeklish ? "hlektroakoystikh_I" : "Ηλεκτροακουστική 1";
        } else if (course.contains("Οπτική Ι")) {
            return greeklish ? "optikh_I" : "Οπτική 1";
        } else if (course.contains("Διάδοση Η/Μ Κύματος ΙΙ")) {
            return greeklish ? "diadosi_II" : "Διάδοση 2";
        } else if (course.contains("Ψηφιακές Τηλεπικοινωνίες Ι")) {
            return greeklish ? "pshf_thlep_I" : "Ψηφιακές Τηλεπ. 1";
        } else if (course.contains("Ακουστική ΙΙ")) {
            return greeklish ? "akoystikh_II" : "Ακουστική 2";
        } else if (course.contains("Βιοϊατρική Τεχνολογία")) {
            return greeklish ? "vioiatriki" : "Βιοιατρική";
        } else if (course.contains("Ηλεκτρακουστική ΙΙ")) {
            return greeklish ? "hlektroakoystikh_II" : "Ηλεκτροακουστική 2";
        } else if (course.contains("Οπτική ΙΙ")) {
            return greeklish ? "optikh_II" : "Οπτική 2";
        } else if (course.contains("Ασύρματος Τηλεπικοινωνία Ι")) {
            return greeklish ? "asyrmatos_I" : "Ασύρματος 1";
        } else if (course.contains("Μικροκύματα I")) {
            return greeklish ? "mikrokymata_I" : "Μικροκύματα 1";
        } else if (course.contains("Ψηφιακές Τηλεπικοινωνίες ΙΙ")) {
            return greeklish ? "pshf_thlep_II" : "Ψηφιακές Τηλεπ. 2";
        } else if (course.contains("Ψηφιακή Επεξεργασία Σήματος")) {
            return greeklish ? "PSES" : "ΨΕΣ";
        } else if (course.contains("Εισαγωγή στην Πολιτική Οικονομία")) {
            return greeklish ? "polit_oik" : "Πολιτική Οικονομία";
        } else if (course.contains("Θεωρία Σκέδασης")) {
            return greeklish ? "skedash" : "Σκέδαση";
        } else if (course.contains("Προηγμένες Τεχνικές Επεξεργασίας Σήματος")) {
            return greeklish ? "ptes" : "ΠΤΕΣ";
        } else if (course.contains("Τηλεοπτικά Συστήματα")) {
            return greeklish ? "tileoptika" : "Τηλεοπτικά";
        } else if (course.contains("Ασύρματος Τηλεπικοινωνία ΙΙ")) {
            return greeklish ? "asyrmatos_II" : "Ασύρματος 2";
        } else if (course.contains("Δίκτυα Τηλεπικοινωνιών")) {
            return greeklish ? "diktya_thlep" : "Δίκτυα Τηλέπ.";
        } else if (course.contains("Θεωρία Πληροφοριών")) {
            return greeklish ? "theoria_plir" : "Θεωρία Πληρ.";
        } else if (course.contains("Οπτικές Επικοινωνίες")) {
            return greeklish ? "optikes_thlep" : "Οπτικές Τηλεπ.";
        } else if (course.contains("Ευρυζωνικά Δίκτυα")) {
            return greeklish ? "eyryzwnika" : "Ευρυζωνικά";
        } else if (course.contains("Τεχνικές μη Καταστρεπτικών Δοκιμών")) {
            return greeklish ? "non_destructive_tests" : "Μη Καταστρεπτικές Δοκιμές";
        } else if (course.contains("Φωτονική Τεχνολογία")) {
            return greeklish ? "fwtonikh" : "Φωτονική";
        } else if (course.contains("Μικροκυματική Τηλεπισκόπηση")) {
            return greeklish ? "thlepiskophsh" : "Τηλεπισκόπηση";
        } else if (course.contains("Μικροκύματα II")) {
            return greeklish ? "mikrokymata_II" : "Μικροκύματα 2";
        } else {
            return null;
        }
    }
}