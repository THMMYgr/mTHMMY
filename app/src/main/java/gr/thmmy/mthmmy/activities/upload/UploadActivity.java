package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.UploadCategory;
import gr.thmmy.mthmmy.utils.AppCompatSpinnerWithoutDefault;
import gr.thmmy.mthmmy.utils.TakePhoto;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseTask;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.UPLOADING_APP_SIGNATURE_ENABLE_KEY;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.BUNDLE_UPLOAD_FIELD_BUILDER_COURSE;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.RESULT_DESCRIPTION;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.RESULT_FILENAME;
import static gr.thmmy.mthmmy.activities.upload.UploadFieldsBuilderActivity.RESULT_TITLE;

public class UploadActivity extends BaseActivity {
    /**
     * The key to use when putting upload's category String to {@link UploadActivity}'s Bundle.
     */
    public static final String BUNDLE_UPLOAD_CATEGORY = "UPLOAD_CATEGORY";
    private static final String uploadIndexUrl = "https://www.thmmy.gr/smf/index.php?action=tpmod;dl=upload";
    private static final String uploadedFromThmmyPromptHtml = "<br /><div style=\"text-align: right;\"><span style=\"font-style: italic;\">uploaded from <a href=\"https://play.google.com/store/apps/details?id=gr.thmmy.mthmmy\">mTHMMY</a></span>";
    /**
     * Request codes used in activities for result (AFR) calls
     */
    private static final int AFR_REQUEST_CODE_CHOOSE_FILE = 8;  //Arbitrary, application specific
    private static final int AFR_REQUEST_CODE_CAMERA = 4;     //Arbitrary, application specific
    private static final int AFR_REQUEST_CODE_FIELDS_BUILDER = 74;    //Arbitrary, application specific

    /**
     * Request code to gain read/write permission
     */
    private static final int UPLOAD_REQUEST_CODE = 42;    //Arbitrary, application specific

    private static final int NO_UPLOAD_METHOD_SELECTED = 0;
    private static final int SELECT_FILE_METHOD_SELECTED = 1;
    private static final int TAKE_PHOTO_METHOD_SELECTED = 2;

    private static final int MAX_FILE_SIZE_SUPPORTED = 45000000;

    private ArrayList<UploadCategory> uploadRootCategories = new ArrayList<>();
    private ParseUploadPageTask parseUploadPageTask;
    private ArrayList<String> bundleCategory;
    private String categorySelected = "-1";
    private String uploaderProfileIndex = "1";

    private int uploadMethodSelected = NO_UPLOAD_METHOD_SELECTED;
    private Uri fileUri;
    private File photoFileSelected = null;
    private File photoFileCreated = null;
    private String fileIcon;

    //UI elements
    private MaterialProgressBar progressBar;
    private LinearLayout categoriesSpinners;
    private AppCompatSpinnerWithoutDefault rootCategorySpinner;
    private EditText uploadTitle;
    private EditText uploadFilename;
    private EditText uploadDescription;
    private AppCompatButton titleDescriptionBuilderButton;

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
        titleDescriptionBuilderButton.setOnClickListener(view -> {
            if (categorySelected.equals("-1")) {
                Toast.makeText(view.getContext(), "Please choose a category first", Toast.LENGTH_SHORT).show();
                return;
            }

            int numberOfSpinners = categoriesSpinners.getChildCount();

            if (numberOfSpinners < 3) {
                Toast.makeText(view.getContext(), "Please choose a course category", Toast.LENGTH_SHORT).show();
                return;
            }

            String maybeSemester = "", maybeCourse = "";

            if (numberOfSpinners == 5) {
                if (((AppCompatSpinnerWithoutDefault) categoriesSpinners.getChildAt(numberOfSpinners - 1)).
                        getSelectedItemPosition() == -1) {
                    maybeSemester = (String) ((AppCompatSpinnerWithoutDefault)
                            categoriesSpinners.getChildAt(numberOfSpinners - 4)).getSelectedItem();
                    maybeCourse = (String) ((AppCompatSpinnerWithoutDefault)
                            categoriesSpinners.getChildAt(numberOfSpinners - 2)).getSelectedItem();
                } else {
                    Toast.makeText(view.getContext(), "Please choose a course category", Toast.LENGTH_SHORT).show();
                }
            } else if (numberOfSpinners == 4) {
                maybeSemester = (String) ((AppCompatSpinnerWithoutDefault)
                        categoriesSpinners.getChildAt(numberOfSpinners - 3)).getSelectedItem();
                maybeCourse = (String) ((AppCompatSpinnerWithoutDefault)
                        categoriesSpinners.getChildAt(numberOfSpinners - 1)).getSelectedItem();
            } else {
                maybeSemester = (String) ((AppCompatSpinnerWithoutDefault)
                        categoriesSpinners.getChildAt(numberOfSpinners - 2)).getSelectedItem();
                maybeCourse = (String) ((AppCompatSpinnerWithoutDefault)
                        categoriesSpinners.getChildAt(numberOfSpinners - 1)).getSelectedItem();
            }

            if (!maybeSemester.contains("εξάμηνο") && !maybeSemester.contains("Εξάμηνο")) {
                Toast.makeText(view.getContext(), "Please choose a course category", Toast.LENGTH_SHORT).show();
                return;
            }
            if (maybeCourse == null) {
                Toast.makeText(view.getContext(), "Please choose a course", Toast.LENGTH_SHORT).show();
                return;
            }

            //Fixes course and semester
            String course = maybeCourse.replaceAll("-", "").replace("(ΝΠΣ)", "").trim();
            String semester = maybeSemester.replaceAll("-", "").trim().substring(0, 1);

            Intent intent = new Intent(UploadActivity.this, UploadFieldsBuilderActivity.class);
            Bundle builderExtras = new Bundle();
            builderExtras.putString(BUNDLE_UPLOAD_FIELD_BUILDER_COURSE, course);
            builderExtras.putString(BUNDLE_UPLOAD_FIELD_BUILDER_SEMESTER, semester);
            intent.putExtras(builderExtras);
            startActivityForResult(intent, AFR_REQUEST_CODE_FIELDS_BUILDER);
        });
        titleDescriptionBuilderButton.setEnabled(false);

        uploadTitle = findViewById(R.id.upload_title);
        uploadDescription = findViewById(R.id.upload_description);

        AppCompatImageButton uploadFilenameInfo = findViewById(R.id.upload_filename_info);
        uploadFilenameInfo.setOnClickListener(view -> {
            //Inflates the popup menu content
            LayoutInflater layoutInflater = (LayoutInflater) view.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (layoutInflater == null) {
                return;
            }
            View popUpContent = layoutInflater.inflate(R.layout.activity_upload_filename_info_popup, null);

            //Creates the PopupWindow
            PopupWindow popUp = new PopupWindow(view.getContext());
            popUp.setContentView(popUpContent);
            popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setFocusable(true);

            //((TextView) popUpContent.findViewById(R.id.upload_filename_info_text)).setTe
            ((TextView) popUpContent.findViewById(R.id.upload_filename_info_text)).
                    setMovementMethod(LinkMovementMethod.getInstance());
            //Displays the popup
            popUp.showAsDropDown(view);
        });

        uploadFilename = findViewById(R.id.upload_filename);
        uploadFilename.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty() &&
                        !s.toString().matches("[0-9a-zA-Z~!@#$%^&()_+=\\-`\\[\\]{};',.]+")) {
                    uploadFilenameInfo.setImageResource(R.drawable.ic_info_outline_warning_24dp);
                } else {
                    uploadFilenameInfo.setImageResource(R.drawable.ic_info_outline_white_24dp);
                }
            }
        });

        AppCompatButton selectFileButton = findViewById(R.id.upload_select_file_button);
        Drawable selectStartDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_insert_drive_file_white_24dp);
        selectFileButton.setCompoundDrawablesRelativeWithIntrinsicBounds(selectStartDrawable, null, null, null);
        selectFileButton.setOnClickListener(v -> {
            String[] mimeTypes = {"image/jpeg", "text/html", "image/png", "image/jpg", "image/gif",
                    "application/pdf", "application/rar", "application/x-tar", "application/zip",
                    "application/msword", "image/vnd.djvu", "application/gz", "application/tar.gz"};

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                    //.setType("*/*")
                    .setType("image/jpeg")
                    .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            startActivityForResult(intent, AFR_REQUEST_CODE_CHOOSE_FILE);
        });

        AppCompatButton takePhotoButton = findViewById(R.id.upload_take_photo_button);
        Drawable takePhotoDrawable = AppCompatResources.getDrawable(this, R.drawable.ic_photo_camera_white_24dp);
        takePhotoButton.setCompoundDrawablesRelativeWithIntrinsicBounds(takePhotoDrawable, null, null, null);
        takePhotoButton.setOnClickListener(v -> {
            if (checkPerms())
                takePhoto();
            else
                requestPerms(UPLOAD_REQUEST_CODE);
        });

        FloatingActionButton uploadFAB = findViewById(R.id.upload_fab);
        uploadFAB.setOnClickListener(view -> {
            //Attempts upload
            progressBar.setVisibility(View.VISIBLE);

            String uploadTitleText = uploadTitle.getText().toString();
            String uploadDescriptionText = uploadDescription.getText().toString();

            //Checks if all required fields are filled
            {
                boolean shouldReturn = false;
                if (uploadTitleText.equals("")) {
                    uploadTitle.setError("Required");
                    shouldReturn = true;
                }
                if (fileUri == null) {
                    Toast.makeText(view.getContext(), "Please choose a file to upload or take a photo", Toast.LENGTH_LONG).show();
                    shouldReturn = true;
                }
                if (categorySelected.equals("-1")) {
                    Toast.makeText(view.getContext(), "Please choose category first", Toast.LENGTH_SHORT).show();
                    shouldReturn = true;
                }
                if (fileUri != null && UploadsHelper.sizeFromUri(this, fileUri) > MAX_FILE_SIZE_SUPPORTED) {
                    Toast.makeText(view.getContext(), "Your files are too powerful for thmmy. Reduce size or split!", Toast.LENGTH_LONG).show();
                    shouldReturn = true;
                }
                if (shouldReturn) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }
            }

            //Checks settings and adds "Uploaded from mTHMMY" string to description
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
            if (sharedPrefs.getBoolean(UPLOADING_APP_SIGNATURE_ENABLE_KEY, true)) {
                uploadDescriptionText += uploadedFromThmmyPromptHtml;
            }

            //Checks if a file renaming is necessary
            {
                String editTextFilename = uploadFilename.getText().toString();
                String selectedFileFilename = UploadsHelper.filenameFromUri(this, fileUri);
                selectedFileFilename = selectedFileFilename.substring(0, selectedFileFilename.indexOf("."));

                if (!editTextFilename.equals(selectedFileFilename)) {
                    //File should be uploaded with a different name
                    switch (uploadMethodSelected) {
                        case SELECT_FILE_METHOD_SELECTED:
                            //Temporarily copies the file to a another location and renames it
                            fileUri = UploadsHelper.createTempFile(this, storage, fileUri,
                                    editTextFilename);
                            break;
                        case TAKE_PHOTO_METHOD_SELECTED:
                            //Renames the photo taken
                            String photoPath = photoFileSelected.getPath();
                            photoPath = photoPath.substring(0, photoPath.lastIndexOf(File.separator));
                            String destinationFilename = photoPath + File.separator + editTextFilename + ".jpg";

                            if (!storage.rename(photoFileSelected.getAbsolutePath(), destinationFilename)) {
                                //Something went wrong, abort
                                Toast.makeText(this, "Could not create temporary file for renaming", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                return;
                            }

                            //Points photoFile and fileUri to the new copied and renamed file
                            photoFileSelected = storage.getFile(destinationFilename);
                            fileUri = FileProvider.getUriForFile(this, getPackageName() +
                                    ".provider", photoFileSelected);
                            break;
                        default:
                            break;
                    }
                }
            }

            try {
                new MultipartUploadRequest(view.getContext(), uploadIndexUrl)
                        .setUtf8Charset()
                        .addParameter("tp-dluploadtitle", uploadTitleText)
                        .addParameter("tp-dluploadcat", categorySelected)
                        .addParameter("tp-dluploadtext", uploadDescriptionText)
                        .addFileToUpload(fileUri.toString(), "tp-dluploadfile")
                        .addParameter("tp_dluploadicon", fileIcon)
                        .addParameter("tp-uploaduser", uploaderProfileIndex)
                        .setNotificationConfig(new UploadNotificationConfig())
                        .setMaxRetries(2)
                        .setDelegate(new UploadStatusDelegate() {
                            @Override
                            public void onProgress(Context context, UploadInfo uploadInfo) {
                            }

                            @Override
                            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                                                Exception exception) {
                                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                                UploadsHelper.deleteTempFiles();
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                                Toast.makeText(context, "Upload completed successfully", Toast.LENGTH_SHORT).show();
                                UploadsHelper.deleteTempFiles();
                                BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);

                                if (uploadMethodSelected == TAKE_PHOTO_METHOD_SELECTED) {
                                    TakePhoto.galleryAddPic(context, photoFileSelected);
                                }
                                uploadTitle.setText(null);
                                uploadFilename.setText(null);
                                uploadDescription.setText(null);
                                fileUri = null;
                                photoFileCreated = null;
                                photoFileSelected = null;
                                progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onCancelled(Context context, UploadInfo uploadInfo) {
                                Toast.makeText(context, "Upload canceled", Toast.LENGTH_SHORT).show();

                                UploadsHelper.deleteTempFiles();
                                progressBar.setVisibility(View.GONE);
                            }
                        })
                        .startUpload();
            } catch (Exception exception) {
                Timber.e(exception, "AndroidUploadService: %s", exception.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

        if (uploadRootCategories.isEmpty()) {
            //Parses the uploads page
            parseUploadPageTask = new ParseUploadPageTask();
            parseUploadPageTask.execute(uploadIndexUrl);
        } else {
            //Renders the already parsed data
            updateUIElements();
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

        //Deletes any photo file previously created, as it is not going to be used
        if (photoFileCreated != null) {
            storage.deleteFile(photoFileCreated.getAbsolutePath());
        }
        if (photoFileSelected != null) {
            storage.deleteFile(photoFileSelected.getAbsolutePath());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AFR_REQUEST_CODE_CHOOSE_FILE) {
            if (resultCode == Activity.RESULT_CANCELED || data == null) {
                return;
            }

            //Keeps the filename between different file selections if it has been modified
            boolean hasCustomFilename = false;
            {
                String editTextFilename = uploadFilename.getText().toString();
                String previousFilename = "";
                if (fileUri != null) {
                    previousFilename = UploadsHelper.filenameFromUri(this, fileUri);
                    previousFilename = previousFilename.substring(0, previousFilename.indexOf("."));
                }

                if (!editTextFilename.isEmpty() && !editTextFilename.equals(previousFilename)) {
                    hasCustomFilename = true;
                }
            }

            //Deletes any photo file previously created, as it is not going to be used
            if (photoFileCreated != null) {
                storage.deleteFile(photoFileCreated.getAbsolutePath());
            }
            if (photoFileSelected != null) {
                storage.deleteFile(photoFileSelected.getAbsolutePath());
            }
            uploadMethodSelected = SELECT_FILE_METHOD_SELECTED;

            fileUri = data.getData();
            if (fileUri != null) {
                String filename = UploadsHelper.filenameFromUri(this, fileUri);
                if (!hasCustomFilename) {
                    uploadFilename.setText(filename.substring(0, filename.indexOf(".")));
                }

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
        } else if (requestCode == AFR_REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Deletes image file
                storage.deleteFile(photoFileCreated.getAbsolutePath());
                return;
            }
            uploadMethodSelected = TAKE_PHOTO_METHOD_SELECTED;

            //Keeps the filename between different file selections if it has been modified
            boolean hasCustomFilename = false;
            {
                String editTextFilename = uploadFilename.getText().toString();
                String previousFilename = "";
                if (fileUri != null) {
                    previousFilename = UploadsHelper.filenameFromUri(this, fileUri);
                    previousFilename = previousFilename.substring(0, previousFilename.indexOf("."));
                }

                if (!editTextFilename.isEmpty() && !editTextFilename.equals(previousFilename)) {
                    hasCustomFilename = true;
                }
            }

            if (photoFileSelected != null) {
                storage.deleteFile(photoFileSelected.getAbsolutePath());
            }
            photoFileSelected = photoFileCreated;

            fileUri = TakePhoto.processResult(this, photoFileSelected);

            if (!hasCustomFilename) {
                uploadFilename.setText(photoFileSelected.getName().
                        substring(0, photoFileSelected.getName().indexOf(".")));
            }
            fileIcon = "jpg_image.gif";
        } else if (requestCode == AFR_REQUEST_CODE_FIELDS_BUILDER) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }

            uploadTitle.setText(data.getStringExtra(RESULT_TITLE));
            uploadFilename.setText(data.getStringExtra(RESULT_FILENAME));
            uploadDescription.setText(data.getStringExtra(RESULT_DESCRIPTION));
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        switch (permsRequestCode) {
            case UPLOAD_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    takePhoto();
                break;
        }
    }

    // Should only be called after making sure permissions are granted
    private void takePhoto() {
        // Create the File where the photo should go
        photoFileCreated = TakePhoto.createImageFile(this);

        // Continue only if the File was successfully created
        if (photoFileCreated != null) {
            startActivityForResult(TakePhoto.getIntent(this, photoFileCreated),
                    AFR_REQUEST_CODE_CAMERA);
        }
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
                        R.layout.spinner_item, tmpSpinnerArray);
                spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

                AppCompatSpinnerWithoutDefault subSpinner = new AppCompatSpinnerWithoutDefault(categoriesSpinners.getContext());
                subSpinner.setPromptId(R.string.upload_spinners_hint);
                subSpinner.setPopupBackgroundResource(R.color.primary);
                subSpinner.setAdapter(spinnerArrayAdapter);
                subSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener(childCategories));
                categoriesSpinners.addView(subSpinner);

                //Sets bundle selection
                if (bundleCategory != null && !bundleCategory.isEmpty()) {
                    int bundleSelectionIndex = -1, currentIndex = 0;

                    for (UploadCategory category : childCategories) {
                        if (bundleCategory.get(0).contains(category.getCategoryTitle()
                                .replace("-", "").trim())) {
                            bundleSelectionIndex = currentIndex;
                            break;
                        }
                        ++currentIndex;
                    }

                    if (bundleSelectionIndex != -1) {
                        subSpinner.setSelection(bundleSelectionIndex, true);
                        bundleCategory.remove(0);
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
                    //This is a level four subcategory
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
            updateUIElements();
            titleDescriptionBuilderButton.setEnabled(true);
            progressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void updateUIElements() {
        String[] tmpSpinnerArray = new String[uploadRootCategories.size()];
        for (int i = 0; i < uploadRootCategories.size(); ++i) {
            tmpSpinnerArray[i] = uploadRootCategories.get(i).getCategoryTitle();
        }

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(BaseApplication.getInstance().getApplicationContext(),
                R.layout.spinner_item, tmpSpinnerArray);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        rootCategorySpinner.setAdapter(spinnerArrayAdapter);

        //Sets bundle selection
        if (bundleCategory != null) {
            int bundleSelectionIndex = -1, currentIndex = 0;

            for (UploadCategory category : uploadRootCategories) {
                if (bundleCategory.get(0).contains(category.getCategoryTitle())) {
                    bundleSelectionIndex = currentIndex;
                    break;
                }
                ++currentIndex;
            }

            if (bundleSelectionIndex != -1) {
                rootCategorySpinner.setSelection(bundleSelectionIndex, true);
                bundleCategory.remove(0);
            }
        }
    }
}