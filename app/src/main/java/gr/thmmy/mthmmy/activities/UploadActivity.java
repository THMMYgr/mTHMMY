package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.UploadCategory;
import gr.thmmy.mthmmy.utils.AppCompatSpinnerWithoutDefault;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseTask;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class UploadActivity extends BaseActivity {
    /**
     * The key to use when putting upload's category String to {@link UploadActivity}'s Bundle.
     */
    public static final String BUNDLE_UPLOAD_CATEGORY = "UPLOAD_CATEGORY";
    private static final String uploadIndexUrl = "https://www.thmmy.gr/smf/index.php?action=tpmod;dl=upload";
    private static final int REQUEST_CODE_CHOOSE_FILE = 8;

    private static ArrayList<UploadCategory> uploadRootCategories = new ArrayList<>();
    //private String currentUploadCategory = "";
    private ParseUploadPageTask parseUploadPageTask;
    private String categorySelected = "-1";
    private String uploaderProfileIndex = "1";
    private Uri fileUri;

    //UI elements
    private MaterialProgressBar progressBar;
    private LinearLayout categoriesSpinners;
    private AppCompatSpinnerWithoutDefault rootCategorySpinner;
    private EditText uploadTitle;
    private EditText uploadDescription;
    //private static AppCompatButton titleDescriptionBuilderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        /*Bundle extras = getIntent().getExtras();
        if (extras != null) {
            //TODO auto fill category from bundle
            currentUploadCategory = extras.getString(BUNDLE_UPLOAD_CATEGORY);
            if (currentUploadCategory != null && !Objects.equals(currentUploadCategory, "")) {
            }
        }*/

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

        /*titleDescriptionBuilderButton = findViewById(R.id.upload_title_description_builder);
        titleDescriptionBuilderButton.setEnabled(false);
        titleDescriptionBuilderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO some dialog?
            }
        });*/

        uploadTitle = findViewById(R.id.upload_title);
        uploadDescription = findViewById(R.id.upload_description);

        findViewById(R.id.upload_select_file_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] mimeTypes = {"image/jpeg", "text/html", "image/png", "image/jpg", "image/gif",
                        "application/pdf", "application/rar", "application/x-tar", "application/zip",
                        "application/msword", "image/vnd.djvu", "application/gz", "application/tar.gz"};

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                        //.setType("*/*")
                        .setType("image/jpeg")
                        .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                startActivityForResult(intent, REQUEST_CODE_CHOOSE_FILE);
            }
        });

        findViewById(R.id.upload_upload_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uploadTitleText = uploadTitle.getText().toString();
                String uploadDescriptionText = uploadDescription.getText().toString();

                if (uploadTitleText.equals("")) {
                    uploadTitle.setError("Required");
                }
                if (uploadDescriptionText.equals("")) {
                    uploadDescription.setError("Required");
                }
                /*if (categorySelected.equals("-1")){
                    //TODO set error
                    //rootCategorySpinner
                }*/

                if (categorySelected.equals("-1") || uploadTitleText.equals("") ||
                        fileUri == null || uploadDescriptionText.equals("")) {
                    return;
                }

                try {
                    String uploadId = new MultipartUploadRequest(v.getContext(), uploadIndexUrl)
                            .setUtf8Charset()
                            .addParameter("tp-dluploadtitle", uploadTitleText)
                            .addParameter("tp-dluploadcat", categorySelected)
                            .addParameter("tp-dluploadtext", uploadDescriptionText)
                            .addFileToUpload(fileUri.toString(), "tp-dluploadfile")
                            .addParameter("tp_dluploadicon", "blank.gif") //TODO auto-select this
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

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    BaseApplication.getInstance().getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, tmpSpinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rootCategorySpinner.setAdapter(spinnerArrayAdapter);
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
            //TODO upload the correct file
            //Check this https://stackoverflow.com/questions/5568874/how-to-extract-the-file-name-from-uri-returned-from-intent-action-get-content/25005243
            fileUri = data.getData();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {
        private ArrayList<UploadCategory> parentCategories, childCategories;

        private CustomOnItemSelectedListener() {
            //Disable default constructor
        }

        CustomOnItemSelectedListener(ArrayList<UploadCategory> parentCategories) {
            this.parentCategories = parentCategories;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //Removes old, unneeded sub categories spinner(s)
            int viewIndex = categoriesSpinners.indexOfChild((AppCompatSpinnerWithoutDefault) view.getParent());

            if (viewIndex + 1 != categoriesSpinners.getChildCount()) { //Makes sure this is not the last child
                categoriesSpinners.removeViews(viewIndex + 1, categoriesSpinners.getChildCount() - viewIndex - 1);
            }

            categorySelected = parentCategories.get(position).getValue();

            //Adds new sub categories spinner
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
                subSpinner.setPromptId(R.string.upload_spinners_prompt);
                subSpinner.setPopupBackgroundResource(R.color.primary);
                subSpinner.setAdapter(spinnerArrayAdapter);
                subSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(childCategories));

                categoriesSpinners.addView(subSpinner);
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

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(
                    BaseApplication.getInstance().getApplicationContext(),
                    android.R.layout.simple_spinner_dropdown_item, tmpSpinnerArray);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            rootCategorySpinner.setAdapter(spinnerArrayAdapter);
            //titleDescriptionBuilderButton.setEnabled(true);

            progressBar.setVisibility(ProgressBar.GONE);
        }
    }
}