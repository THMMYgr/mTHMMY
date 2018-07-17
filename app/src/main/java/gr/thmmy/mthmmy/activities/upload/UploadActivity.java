package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.UploadCategory;
import gr.thmmy.mthmmy.utils.AppCompatSpinnerWithoutDefault;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseTask;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.UPLOADING_APP_SIGNATURE_ENABLE_KEY;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.BUNDLE_UPLOAD_FIELD_BUILDER_COURSE;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.RESULT_DESCRIPTION;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.RESULT_TITLE;

public class UploadActivity extends BaseActivity {
    /**
     * The key to use when putting upload's category String to {@link UploadActivity}'s Bundle.
     */
    public static final String BUNDLE_UPLOAD_CATEGORY = "UPLOAD_CATEGORY";
    private static final String uploadIndexUrl = "https://www.thmmy.gr/smf/index.php?action=tpmod;dl=upload";
    private static final String uploadedFrommThmmyPromptHtml = "<br /><div style=\"text-align: right;\"><span style=\"font-style: italic;\">uploaded from <a href=\"https://play.google.com/store/apps/details?id=gr.thmmy.mthmmy\">mTHMMY</a></span>";
    private static final int REQUEST_CODE_CHOOSE_FILE = 8;
    private static final int REQUEST_CODE_CAMERA = 4;
    private static final int REQUEST_CODE_FIELDS_BUILDER = 74;

    private static ArrayList<UploadCategory> uploadRootCategories = new ArrayList<>();
    private ParseUploadPageTask parseUploadPageTask;
    private ArrayList<String> bundleCategory;
    private String categorySelected = "-1";
    private String uploaderProfileIndex = "1";
    private Uri fileUri;
    private String fileIcon;

    //UI elements
    private MaterialProgressBar progressBar;
    private LinearLayout categoriesSpinners;
    private AppCompatSpinnerWithoutDefault rootCategorySpinner;
    private EditText uploadTitle;
    private EditText uploadDescription;
    private AppCompatButton titleDescriptionBuilderButton;
    private AppCompatButton selectFileButton;
    //private static AppCompatButton titleDescriptionBuilderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String tmpUploadCategoryNav = extras.getString(BUNDLE_UPLOAD_CATEGORY);
            //something like "THMMY.gr > Downloads > Βασικός Κύκλος > 3ο εξάμηνο > Ηλεκτρικά Κυκλώματα ΙΙ"
            if (tmpUploadCategoryNav != null && !tmpUploadCategoryNav.equals("")) {
                String[] tmpSplitUploadCategoryNav = tmpUploadCategoryNav.split(">");

                for (int i = 0; i < tmpSplitUploadCategoryNav.length; ++i) {
                    tmpSplitUploadCategoryNav[i] = tmpSplitUploadCategoryNav[i].trim();
                }

                if (tmpSplitUploadCategoryNav.length > 2) {
                    bundleCategory = new ArrayList<>(Arrays.asList(tmpSplitUploadCategoryNav).subList(2, tmpSplitUploadCategoryNav.length));
                }
            }
        }

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Upload");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(UPLOAD_ID);

        progressBar = findViewById(R.id.progressBar);

        findViewById(R.id.upload_outer_scrollview).setVerticalScrollBarEnabled(false);
        categoriesSpinners = findViewById(R.id.upload_spinners);
        rootCategorySpinner = findViewById(R.id.upload_spinner_category_root);
        rootCategorySpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(uploadRootCategories));

        titleDescriptionBuilderButton = findViewById(R.id.upload_title_description_builder);
        titleDescriptionBuilderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (categorySelected.equals("-1")) {
                    Toast.makeText(view.getContext(), "Please choose category first", Toast.LENGTH_SHORT).show();
                    return;
                }

                int numberOfSpinners = categoriesSpinners.getChildCount();

                if (numberOfSpinners < 3) {
                    Toast.makeText(view.getContext(), "Please choose a course category", Toast.LENGTH_SHORT).show();
                    return;
                }

                String maybeSemester = (String) ((AppCompatSpinnerWithoutDefault)
                        categoriesSpinners.getChildAt(numberOfSpinners - 2)).getSelectedItem(),
                        maybeCourse = (String) ((AppCompatSpinnerWithoutDefault)
                                categoriesSpinners.getChildAt(numberOfSpinners - 1)).getSelectedItem();

                if (!maybeSemester.contains("εξάμηνο") && !maybeSemester.contains("Εξάμηνο")) {
                    Toast.makeText(view.getContext(), "Please choose a course category", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Fixes course and semester
                maybeCourse = maybeCourse.replaceAll("-", "").replace("(ΝΠΣ)", "").trim();
                maybeSemester = maybeSemester.replaceAll("-", "").trim().
                        substring(0, 1);

                Intent intent = new Intent(UploadActivity.this, UploadFieldsBuilderActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE, maybeCourse);
                extras.putString(BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER, maybeSemester);
                intent.putExtras(extras);
                startActivityForResult(intent, REQUEST_CODE_FIELDS_BUILDER);
            }
        });
        titleDescriptionBuilderButton.setEnabled(false);

        uploadTitle = findViewById(R.id.upload_title);
        uploadDescription = findViewById(R.id.upload_description);

        selectFileButton = findViewById(R.id.upload_select_file_button);
        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Take photo", "Choose file",
                        "Cancel"};
                AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
                builder.setTitle("Upload file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Take photo")) {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, REQUEST_CODE_CAMERA);
                        } else if (options[item].equals("Choose file")) {
                            String[] mimeTypes = {"image/jpeg", "text/html", "image/png", "image/jpg", "image/gif",
                                    "application/pdf", "application/rar", "application/x-tar", "application/zip",
                                    "application/msword", "image/vnd.djvu", "application/gz", "application/tar.gz"};

                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                                    //.setType("*/*")
                                    .setType("image/jpeg")
                                    .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                            startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
                        } else if (options[item].equals("Cancel")) {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });

        findViewById(R.id.upload_upload_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uploadTitleText = uploadTitle.getText().toString();
                String uploadDescriptionText = uploadDescription.getText().toString();

                if (uploadTitleText.equals("")) {
                    uploadTitle.setError("Required");
                }
                if (fileUri == null) {
                    selectFileButton.setError("Required");
                }
                if (categorySelected.equals("-1")) {
                    Toast.makeText(view.getContext(), "Please choose category first", Toast.LENGTH_SHORT).show();
                }

                if (categorySelected.equals("-1") || uploadTitleText.equals("") || fileUri == null) {
                    return;
                }

                String tmpDescriptionText = uploadDescriptionText;

                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                if (sharedPrefs.getBoolean(UPLOADING_APP_SIGNATURE_ENABLE_KEY, true)) {
                    tmpDescriptionText += uploadedFrommThmmyPromptHtml;
                }

                try {
                    new MultipartUploadRequest(view.getContext(), uploadIndexUrl)
                            .setUtf8Charset()
                            .addParameter("tp-dluploadtitle", uploadTitleText)
                            .addParameter("tp-dluploadcat", categorySelected)
                            .addParameter("tp-dluploadtext", tmpDescriptionText)
                            .addFileToUpload(fileUri.toString(), "tp-dluploadfile")
                            .addParameter("tp_dluploadicon", fileIcon)
                            .addParameter("tp-uploaduser", uploaderProfileIndex)
                            .setNotificationConfig(new UploadNotificationConfig())
                            .setMaxRetries(2).startUpload();
                } catch (Exception exception) {
                    Timber.e(exception, "AndroidUploadService: %s", exception.getMessage());
                }
            }
        });

        if (uploadRootCategories.isEmpty()) {
            parseUploadPageTask = new ParseUploadPageTask();
            parseUploadPageTask.execute(uploadIndexUrl);
        } else {
            String[] tmpSpinnerArray = new String[uploadRootCategories.size()];
            for (int i = 0; i < uploadRootCategories.size(); ++i) {
                tmpSpinnerArray[i] = uploadRootCategories.get(i).getCategoryTitle();
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(BaseApplication.getInstance().getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, tmpSpinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rootCategorySpinner.setAdapter(spinnerArrayAdapter);

            //Sets bundle selection
            if (bundleCategory != null) {
                int bundleSelectionIndex = -1, currentIndex = 0;
                for (UploadCategory category : uploadRootCategories) {
                    if (category.getCategoryTitle().contains(bundleCategory.get(0))) { //TODO fix .contains, always false
                        bundleSelectionIndex = currentIndex;
                        break;
                    }
                    ++currentIndex;
                }

                if (bundleSelectionIndex != -1) {
                    rootCategorySpinner.setSelection(bundleSelectionIndex, true);
                }
            }

            titleDescriptionBuilderButton.setEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        drawer.setSelection(UPLOAD_ID);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parseUploadPageTask != null && parseUploadPageTask.getStatus() != AsyncTask.Status.RUNNING)
            parseUploadPageTask.cancel(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_FILE && data != null) {
            fileUri = data.getData();
            if (fileUri != null) {
                String filename = filenameFromUri(fileUri);
                selectFileButton.setText(filename);

                filename = filename.toLowerCase();
                if (filename.endsWith(".jpg")) {
                    fileIcon = "jpg_image.gif";
                } else if (filename.endsWith(".gif")) {
                    fileIcon = "gif_image.gif";
                } else if (filename.endsWith(".png")) {
                    fileIcon = "png_image.gif";
                } else if (filename.endsWith(".html") || filename.endsWith(".htm")) {
                    fileIcon = "html_file.gif";
                } else if (filename.endsWith(".pdf") || filename.endsWith(".doc") ||
                        filename.endsWith("djvu")) {
                    fileIcon = "text_file.gif";
                } else if (filename.endsWith(".zip") || filename.endsWith(".rar") ||
                        filename.endsWith(".tar") || filename.endsWith(".tar.gz") ||
                        filename.endsWith(".gz")) {
                    fileIcon = "archive.gif";
                } else {
                    fileIcon = "blank.gif";
                }
            }
        } else if (requestCode == REQUEST_CODE_CAMERA && data != null) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }
            //TODO
        } else if (requestCode == REQUEST_CODE_FIELDS_BUILDER) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }
            //TODO rename file
            uploadTitle.setText(data.getStringExtra(RESULT_TITLE));
            uploadDescription.setText(data.getStringExtra(RESULT_DESCRIPTION));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @NonNull
    private String filenameFromUri(Uri uri) {
        String filename = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (filename == null) {
            filename = uri.getPath();
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }
        }

        return filename;
    }

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private ArrayList<UploadCategory> parentCategories, childCategories;

        // Suppresses default constructor
        @SuppressWarnings("unused")
        private CustomOnItemSelectedListener() {
        }

        CustomOnItemSelectedListener(ArrayList<UploadCategory> parentCategories) {
            this.parentCategories = parentCategories;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //Removes old, unneeded sub-category spinner(s)
            int viewIndex = categoriesSpinners.indexOfChild((AppCompatSpinnerWithoutDefault) view.getParent());

            if (viewIndex + 1 != categoriesSpinners.getChildCount()) { //Makes sure this is not the last child
                categoriesSpinners.removeViews(viewIndex + 1, categoriesSpinners.getChildCount() - viewIndex - 1);
            }

            categorySelected = parentCategories.get(position).getValue();

            //Adds new sub-category spinner
            if (parentCategories.get(position).hasSubCategories()) {
                childCategories = parentCategories.get(position).getSubCategories();

                String[] tmpSpinnerArray = new String[childCategories.size()];
                for (int i = 0; i < tmpSpinnerArray.length; ++i) {
                    tmpSpinnerArray[i] = childCategories.get(i).getCategoryTitle();
                }

                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, tmpSpinnerArray);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                AppCompatSpinnerWithoutDefault subSpinner = new AppCompatSpinnerWithoutDefault(categoriesSpinners.getContext());
                subSpinner.setPromptId(R.string.upload_spinners_hint);
                subSpinner.setPopupBackgroundResource(R.color.primary);
                subSpinner.setAdapter(spinnerArrayAdapter);
                subSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(childCategories));
                categoriesSpinners.addView(subSpinner);

                //Sets bundle selection
                if (bundleCategory != null && viewIndex < bundleCategory.size()) {
                    int bundleSelectionIndex = -1, currentIndex = 0;
                    for (UploadCategory category : parentCategories) {
                        if (category.getCategoryTitle().contains(bundleCategory.get(viewIndex))) {
                            bundleSelectionIndex = currentIndex;
                            break;
                        }
                        ++currentIndex;
                    }

                    if (bundleSelectionIndex != -1) {
                        subSpinner.setSelection(bundleSelectionIndex, true);
                    }
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    }

    /**
     * An {@link ParseTask} that handles asynchronous fetching of the upload page and parsing the
     * upload categories.
     */
    private class ParseUploadPageTask extends ParseTask {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected void parse(Document uploadPage) throws ParseException {
            Elements categoriesElements;
            Element uploaderProfileIndexElement;

            try {
                categoriesElements = uploadPage.select("select[name='tp-dluploadcat']>option");
                uploaderProfileIndexElement = uploadPage.select("input[name=\"tp-uploaduser\"]").first();
            } catch (Exception e) {
                throw new ParseException("Parsing failed (UploadActivity)");
            }

            uploaderProfileIndex = uploaderProfileIndexElement.attr("value");

            for (Element category : categoriesElements) {
                String categoryValue = category.attr("value");
                String categoryText = category.text();

                if (categoryText.startsWith("- ")) {
                    //This is a level one subcategory
                    uploadRootCategories.get(uploadRootCategories.size() - 1).addSubCategory(categoryValue, categoryText);
                } else if (categoryText.startsWith("-- ")) {
                    //This is a level two subcategory
                    UploadCategory rootLevelCategory = uploadRootCategories.get(uploadRootCategories.size() - 1);
                    UploadCategory firstLevelCategory = rootLevelCategory.getSubCategories().get(rootLevelCategory.getSubCategories().size() - 1);
                    firstLevelCategory.addSubCategory(categoryValue, categoryText);
                } else if (categoryText.startsWith("--- ")) {
                    //This is a level three subcategory
                    UploadCategory rootLevelCategory = uploadRootCategories.get(uploadRootCategories.size() - 1);
                    UploadCategory firstLevelCategory = rootLevelCategory.getSubCategories().get(rootLevelCategory.getSubCategories().size() - 1);
                    UploadCategory secondLevelCategory = firstLevelCategory.getSubCategories().get(firstLevelCategory.getSubCategories().size() - 1);
                    secondLevelCategory.addSubCategory(categoryValue, categoryText);
                } else if (categoryText.startsWith("---- ")) {
                    //This is a level three subcategory
                    UploadCategory rootLevelCategory = uploadRootCategories.get(uploadRootCategories.size() - 1);
                    UploadCategory firstLevelCategory = rootLevelCategory.getSubCategories().get(rootLevelCategory.getSubCategories().size() - 1);
                    UploadCategory secondLevelCategory = firstLevelCategory.getSubCategories().get(firstLevelCategory.getSubCategories().size() - 1);
                    UploadCategory thirdLevelCategory = secondLevelCategory.getSubCategories().get(secondLevelCategory.getSubCategories().size() - 1);
                    thirdLevelCategory.addSubCategory(categoryValue, categoryText);
                } else {
                    //This is a root category
                    uploadRootCategories.add(new UploadCategory(categoryValue, categoryText));
                }
            }
        }

        @Override
        protected void postExecution(ResultCode result) {
            String[] tmpSpinnerArray = new String[uploadRootCategories.size()];
            for (int i = 0; i < uploadRootCategories.size(); ++i) {
                tmpSpinnerArray[i] = uploadRootCategories.get(i).getCategoryTitle();
            }

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(BaseApplication.getInstance().getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, tmpSpinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rootCategorySpinner.setAdapter(spinnerArrayAdapter);

            //Sets bundle selection
            if (bundleCategory != null) {
                int bundleSelectionIndex = -1, currentIndex = 0;
                for (UploadCategory category : uploadRootCategories) {
                    if (category.getCategoryTitle().contains(bundleCategory.get(0))) { //TODO fix .contains, always false
                        bundleSelectionIndex = currentIndex;
                        break;
                    }
                    ++currentIndex;
                }

                if (bundleSelectionIndex != -1) {
                    rootCategorySpinner.setSelection(bundleSelectionIndex, true);
                }
            }

            titleDescriptionBuilderButton.setEnabled(true);
            progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}