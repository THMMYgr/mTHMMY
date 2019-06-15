package gr.thmmy.mthmmy.activities.upload;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageButton;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.ContextThemeWrapper;
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
import net.gotev.uploadservice.UploadNotificationAction;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.UploadCategory;
import gr.thmmy.mthmmy.model.UploadFile;
import gr.thmmy.mthmmy.services.UploadsReceiver;
import gr.thmmy.mthmmy.utils.AppCompatSpinnerWithoutDefault;
import gr.thmmy.mthmmy.utils.FileUtils;
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
import static gr.thmmy.mthmmy.utils.FileUtils.faIconFromFilename;

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

    private static final int MAX_FILE_SIZE_SUPPORTED = 45000000;

    //private UploadsReceiver uploadsReceiver = new UploadsReceiver();
    private ArrayList<UploadCategory> uploadRootCategories = new ArrayList<>();
    private ParseUploadPageTask parseUploadPageTask;
    private ArrayList<String> bundleCategory;
    private String categorySelected = "-1";
    private String uploaderProfileIndex = "1";

    private ArrayList<UploadFile> filesList = new ArrayList<>();
    private File photoFileCreated = null;
    private String fileIcon;
    private AppCompatImageButton uploadFilenameInfo;
    private CustomTextWatcher textWatcher;
    private boolean hasModifiedFilename = false;

    //UI elements
    private MaterialProgressBar progressBar;
    private LinearLayout categoriesSpinners;
    private AppCompatSpinnerWithoutDefault rootCategorySpinner;
    private EditText uploadTitle;
    private EditText uploadFilename;
    private EditText uploadDescription;
    private AppCompatButton titleDescriptionBuilderButton;
    private LinearLayout filesListView;

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

        uploadFilenameInfo = findViewById(R.id.upload_filename_info);
        uploadFilenameInfo.setOnClickListener(view -> {
            //Inflates the popup menu content
            LayoutInflater layoutInflater = (LayoutInflater) view.getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (layoutInflater == null) {
                return;
            }

            Context wrapper = new ContextThemeWrapper(this, R.style.PopupWindow);
            View popUpContent = layoutInflater.inflate(R.layout.activity_upload_filename_info_popup, null);

            //Creates the PopupWindow
            PopupWindow popUp = new PopupWindow(wrapper);
            popUp.setContentView(popUpContent);
            popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setFocusable(true);

            ((TextView) popUpContent.findViewById(R.id.upload_filename_info_text)).
                    setMovementMethod(LinkMovementMethod.getInstance());
            //Displays the popup
            popUp.showAsDropDown(view);
        });

        uploadFilename = findViewById(R.id.upload_filename);
        textWatcher = new CustomTextWatcher();
        uploadFilename.addTextChangedListener(textWatcher);

        filesListView = findViewById(R.id.upload_files_list);

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
                    .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                    .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

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
            String editTextFilename = uploadFilename.getText().toString();
            final String[] uploadDescriptionText = {uploadDescription.getText().toString()};

            //Checks if all required fields are filled
            {
                boolean shouldReturn = false;
                if (uploadTitleText.equals("")) {
                    uploadTitle.setError("Required");
                    shouldReturn = true;
                }
                if (filesList.isEmpty()) {
                    Toast.makeText(view.getContext(), "Please choose a file to upload or take a photo", Toast.LENGTH_LONG).show();
                    shouldReturn = true;
                }
                if (categorySelected.equals("-1")) {
                    Toast.makeText(view.getContext(), "Please choose category first", Toast.LENGTH_SHORT).show();
                    shouldReturn = true;
                }
                if (!filesList.isEmpty()) {
                    long totalFilesSize = 0;
                    for (UploadFile file : filesList) {
                        totalFilesSize += FileUtils.sizeFromUri(this, file.getFileUri());
                    }

                    if (totalFilesSize > MAX_FILE_SIZE_SUPPORTED) {
                        Toast.makeText(view.getContext(), "Your files are too powerful for thmmy. Reduce size or split!", Toast.LENGTH_LONG).show();
                        shouldReturn = true;
                    }
                }
                if (!editTextFilename.matches("(.+\\.)+.+") ||
                        !FileUtils.getFilenameWithoutExtension(editTextFilename).
                                matches("[0-9a-zA-Zα-ωΑ-Ω~!@#$%^&()_+=\\-`\\[\\]{};',.]+")) {
                    uploadFilename.setError("Invalid filename");
                    shouldReturn = true;
                }
                if (shouldReturn) {
                    progressBar.setVisibility(View.GONE);
                    return;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Upload to thmmy");
            builder.setMessage("Are you sure?");
            builder.setPositiveButton("YES, FIRE AWAY", (dialog, which) -> {
                //Checks settings and possibly adds "Uploaded from mTHMMY" string to description
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                if (sharedPrefs.getBoolean(UPLOADING_APP_SIGNATURE_ENABLE_KEY, true)) {
                    uploadDescriptionText[0] += uploadedFromThmmyPromptHtml;
                }

                for (UploadFile file : filesList) {
                    if (file.isCameraPhoto()) {
                        TakePhoto.galleryAddPic(this, file.getPhotoFile());
                    }
                }

                Uri tempFileUri = null;
                if (filesList.size() == 1) {
                    //Checks if the file needs renaming
                    UploadFile uploadFile = filesList.get(0);
                    String selectedFileFilename = FileUtils.filenameFromUri(this, uploadFile.getFileUri());

                    if (!editTextFilename.equals(selectedFileFilename)) {
                        //File should be uploaded with a different name
                        if (!uploadFile.isCameraPhoto()) {
                            //Temporarily copies the file to a another location and renames it
                            tempFileUri = UploadsHelper.createTempFile(this, storage,
                                    uploadFile.getFileUri(),
                                    FileUtils.getFilenameWithoutExtension(editTextFilename));
                        } else {
                            //Renames the photo taken
                            String photoPath = uploadFile.getPhotoFile().getPath();
                            photoPath = photoPath.substring(0, photoPath.lastIndexOf(File.separator));
                            String destinationFilename = photoPath + File.separator +
                                    FileUtils.getFilenameWithoutExtension(editTextFilename) + ".jpg";

                            if (!storage.rename(uploadFile.getPhotoFile().getAbsolutePath(), destinationFilename)) {
                                //Something went wrong, abort
                                Toast.makeText(this, "Could not create temporary file for renaming", Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                return;
                            }

                            //Points photoFile and fileUri to the new copied and renamed file
                            uploadFile.setPhotoFile(storage.getFile(destinationFilename));
                            uploadFile.setFileUri(FileProvider.getUriForFile(this, getPackageName() +
                                    ".provider", uploadFile.getPhotoFile()));
                        }
                    }
                } else {
                    Uri[] filesListArray = new Uri[filesList.size()];
                    for (int i = 0; i < filesList.size(); ++i) {
                        filesListArray[i] = filesList.get(i).getFileUri();
                    }

                    new ZipTask(this, editTextFilename, categorySelected,
                            uploadTitleText, uploadDescriptionText[0], fileIcon,
                            uploaderProfileIndex).execute(filesListArray);
                    finish();
                    return;
                }

                String uploadID = UUID.randomUUID().toString();
                if (uploadFile(this, uploadID, getConfigForUpload(this, uploadID,
                        editTextFilename, categorySelected, uploadTitleText, uploadDescriptionText[0],
                        fileIcon, uploaderProfileIndex, tempFileUri == null
                                ? filesList.get(0).getFileUri()
                                : tempFileUri),
                        categorySelected, uploadTitleText,
                        uploadDescriptionText[0], fileIcon, uploaderProfileIndex,
                        tempFileUri == null
                                ? filesList.get(0).getFileUri()
                                : tempFileUri)) {
                    finish();
                } else {
                    Toast.makeText(this, "Couldn't initiate upload.", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("NOPE", (dialog, which) -> {
                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
            });

            AlertDialog alert = builder.create();
            alert.setOnCancelListener(dialog -> {
                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
            });
            alert.show();
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (parseUploadPageTask != null && parseUploadPageTask.getStatus() != AsyncTask.Status.RUNNING) {
            parseUploadPageTask.cancel(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AFR_REQUEST_CODE_CHOOSE_FILE) {
            if (resultCode == Activity.RESULT_CANCELED || data == null) {
                return;
            }

            if (data.getClipData() != null) {
                fileIcon = "archive.gif";
                textWatcher.setFileExtension(".zip");

                if (!hasModifiedFilename) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
                    String zipFilename = "mThmmy_" + timeStamp + ".zip";
                    uploadFilename.setText(zipFilename);
                    hasModifiedFilename = false;
                }

                for (int fileIndex = 0; fileIndex < data.getClipData().getItemCount(); ++fileIndex) {
                    Uri newFileUri = data.getClipData().getItemAt(fileIndex).getUri();
                    String filename = FileUtils.filenameFromUri(this, newFileUri);
                    addFileViewToList(filename);
                    filesList.add(new UploadFile(false, newFileUri, null));
                }
            } else {
                Uri newFileUri = data.getData();
                if (newFileUri != null) {
                    String filename = FileUtils.filenameFromUri(this, newFileUri);

                    if (filesList.isEmpty()) {
                        textWatcher.setFileExtension(FileUtils.getFileExtension(filename));

                        if (!hasModifiedFilename) {
                            uploadFilename.setText(filename);
                            hasModifiedFilename = false;
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
                    } else {
                        fileIcon = "archive.gif";
                        textWatcher.setFileExtension(".zip");

                        if (!hasModifiedFilename) {
                            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
                            String zipFilename = "mThmmy_" + timeStamp + ".zip";
                            uploadFilename.setText(zipFilename);
                            hasModifiedFilename = false;
                        }
                    }

                    addFileViewToList(filename);
                    filesList.add(new UploadFile(false, newFileUri, null));
                }
            }
        } else if (requestCode == AFR_REQUEST_CODE_CAMERA) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Deletes image file
                storage.deleteFile(photoFileCreated.getAbsolutePath());
                return;
            }

            if (filesList.isEmpty()) {
                textWatcher.setFileExtension(FileUtils.getFileExtension(photoFileCreated.getName()));

                if (!hasModifiedFilename) {
                    uploadFilename.setText(photoFileCreated.getName());
                    hasModifiedFilename = false;
                }

                fileIcon = "jpg_image.gif";
            } else {
                fileIcon = "archive.gif";
                textWatcher.setFileExtension(".zip");

                if (!hasModifiedFilename) {
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
                    String zipFilename = "mThmmy_" + timeStamp + ".zip";
                    uploadFilename.setText(zipFilename);
                    hasModifiedFilename = false;
                }
            }

            UploadFile newFile = new UploadFile(true, TakePhoto.processResult(this,
                    photoFileCreated), photoFileCreated);
            addFileViewToList(FileUtils.getFilenameWithoutExtension(FileUtils.
                    filenameFromUri(this, newFile.getFileUri())));
            filesList.add(newFile);
        } else if (requestCode == AFR_REQUEST_CODE_FIELDS_BUILDER) {
            if (resultCode == Activity.RESULT_CANCELED) {
                return;
            }

            String previousName = uploadFilename.getText().toString();
            if (previousName.isEmpty()) {
                uploadFilename.setText(data.getStringExtra(RESULT_FILENAME));
            } else {
                String filenameWithExtension = data.getStringExtra(RESULT_FILENAME) +
                        FileUtils.getFileExtension(previousName);
                uploadFilename.setText(filenameWithExtension);
            }
            hasModifiedFilename = true;

            uploadTitle.setText(data.getStringExtra(RESULT_TITLE));
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

    private void addFileViewToList(String filename) {
        LayoutInflater layoutInflater = getLayoutInflater();
        LinearLayout newFileRow = (LinearLayout) layoutInflater.
                inflate(R.layout.activity_upload_file_list_row, null);

        TextView itemText = newFileRow.findViewById(R.id.upload_file_item_text);
        itemText.setTypeface(Typeface.createFromAsset(this.getAssets()
                , "fonts/fontawesome-webfont.ttf"));
        String filenameWithIcon = faIconFromFilename(this, filename) + " " + filename;
        itemText.setText(filenameWithIcon);

        newFileRow.findViewById(R.id.upload_file_item_remove).setOnClickListener(view -> {
            int fileIndex = filesListView.indexOfChild(newFileRow);
            filesListView.removeViewAt(fileIndex);

            if (filesList.get(fileIndex).isCameraPhoto()) {
                storage.deleteFile(filesList.get(fileIndex).getPhotoFile().getAbsolutePath());
            }
            filesList.remove(fileIndex);
            if (filesList.isEmpty()) {
                filesListView.setVisibility(View.GONE);
            } else if (filesList.size() == 1) {
                textWatcher.setFileExtension(FileUtils.getFileExtension(FileUtils.
                        filenameFromUri(this, filesList.get(0).getFileUri())));
            }
        });

        filesListView.addView(newFileRow);
        filesListView.setVisibility(View.VISIBLE);
    }

    public static UploadNotificationConfig getConfigForUpload(Context context, String uploadID,
                                                              String filename, String retryCategory,
                                                              String retryTitleText, String retryDescription,
                                                              String retryIcon, String retryUploaderProfile,
                                                              Uri retryFileUri) {
        UploadNotificationConfig uploadNotificationConfig = new UploadNotificationConfig();
        uploadNotificationConfig.setIconForAllStatuses(android.R.drawable.stat_sys_upload);
        uploadNotificationConfig.setTitleForAllStatuses("Uploading " + filename);

        uploadNotificationConfig.getProgress().iconResourceID = android.R.drawable.stat_sys_upload;
        uploadNotificationConfig.getCompleted().iconResourceID = android.R.drawable.stat_sys_upload_done;
        uploadNotificationConfig.getError().iconResourceID = android.R.drawable.stat_sys_upload_done;
        uploadNotificationConfig.getError().iconColorResourceID = R.color.error_red;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uploadNotificationConfig.getError().message = "Error during upload. Click for options";
        }
        uploadNotificationConfig.getCancelled().iconColorResourceID = android.R.drawable.stat_sys_upload_done;
        uploadNotificationConfig.getCancelled().autoClear = true;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Intent combinedActionsIntent = new Intent(UploadsReceiver.ACTION_COMBINED_UPLOAD);
            combinedActionsIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_ID_KEY, uploadID);

            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_FILENAME, filename);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_CATEGORY, retryCategory);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_TITLE, retryTitleText);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_DESCRIPTION, retryDescription);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_ICON, retryIcon);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_UPLOADER, retryUploaderProfile);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_RETRY_FILE_URI, retryFileUri);

            uploadNotificationConfig.setClickIntentForAllStatuses(PendingIntent.getBroadcast(context,
                    1, combinedActionsIntent, PendingIntent.FLAG_UPDATE_CURRENT));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent retryIntent = new Intent(context, UploadsReceiver.class);
            retryIntent.setAction(UploadsReceiver.ACTION_RETRY_UPLOAD);
            retryIntent.putExtra(UploadsReceiver.UPLOAD_ID_KEY, uploadID);

            Intent cancelIntent = new Intent(context, UploadsReceiver.class);
            cancelIntent.setAction(UploadsReceiver.ACTION_CANCEL_UPLOAD);
            cancelIntent.putExtra(UploadsReceiver.UPLOAD_ID_KEY, uploadID);

            uploadNotificationConfig.getProgress().actions.add(new UploadNotificationAction(
                    R.drawable.ic_cancel_accent_24dp,
                    context.getString(R.string.upload_notification_cancel),
                    PendingIntent.getBroadcast(context, 0, cancelIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)
            ));
            uploadNotificationConfig.getError().actions.add(new UploadNotificationAction(
                    R.drawable.ic_notification,
                    context.getString(R.string.upload_notification_retry),
                    PendingIntent.getBroadcast(context, 0, retryIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT)
            ));
        }

        return uploadNotificationConfig;
    }

    public static boolean uploadFile(Context context, String uploadID,
                                     UploadNotificationConfig uploadNotificationConfig,
                                     String categorySelected, String uploadTitleText,
                                     String uploadDescriptionText, String fileIcon,
                                     String uploaderProfileIndex, Uri fileUri) {
        try {
            new MultipartUploadRequest(context, uploadID, uploadIndexUrl)
                    .setUtf8Charset()
                    .setNotificationConfig(uploadNotificationConfig)
                    .addParameter("tp-dluploadtitle", uploadTitleText)
                    .addParameter("tp-dluploadcat", categorySelected)
                    .addParameter("tp-dluploadtext", uploadDescriptionText)
                    .addFileToUpload(fileUri.toString()
                            , "tp-dluploadfile")
                    .addParameter("tp_dluploadicon", fileIcon)
                    .addParameter("tp-uploaduser", uploaderProfileIndex)
                    .setNotificationConfig(uploadNotificationConfig)
                    .setMaxRetries(2)
                    .startUpload();

            Toast.makeText(context, "Uploading files in the background.", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception exception) {
            Timber.e(exception, "AndroidUploadService: %s", exception.getMessage());
            return false;
        }
    }

    private class CustomTextWatcher implements TextWatcher {
        String oldFilename, fileExtension;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            //Saves an instance of the filename before changing
            oldFilename = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            //Warns user for bad filenames
            String filenameWithoutExtension = FileUtils.getFilenameWithoutExtension(s.toString());
            if (filenameWithoutExtension != null && !filenameWithoutExtension.isEmpty() &&
                    !filenameWithoutExtension.matches("[0-9a-zA-Z~!@#$%^&()_+=\\-`\\[\\]{};',.]+")) {
                uploadFilenameInfo.setImageResource(R.drawable.ic_info_outline_warning_24dp);
            } else {
                uploadFilenameInfo.setImageResource(R.drawable.ic_info_outline_white_24dp);
            }

            if (fileExtension == null) {
                hasModifiedFilename = !s.toString().isEmpty();
                return;
            }

            if (!s.toString().endsWith(fileExtension)) {
                //User tried to alter the extension
                //Prevents the change
                uploadFilename.setText(oldFilename);
                return;
            }

            //User has modified the filename
            hasModifiedFilename = true;
            if (s.toString().isEmpty() || (filesList.size() == 1 && s.toString().equals(FileUtils.
                    filenameFromUri(getApplicationContext(), filesList.get(0).getFileUri())))) {
                //After modification the filename falls back to the original
                hasModifiedFilename = false;
            }

            //Adds the grey colored span to the extension
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.secondary_text)),
                    s.length() - fileExtension.length(), s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        void setFileExtension(String extension) {
            boolean oldHasModifiedFilename = hasModifiedFilename;
            oldFilename = uploadFilename.getText().toString();
            fileExtension = extension;
            String newFilename;

            if (!oldFilename.isEmpty()) {
                newFilename = FileUtils.getFilenameWithoutExtension(oldFilename) + extension;
            } else {
                newFilename = extension;
            }

            uploadFilename.setText(newFilename);
            hasModifiedFilename = oldHasModifiedFilename;
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

    public static class ZipTask extends AsyncTask<Uri, Void, Boolean> {
        // Weak references will still allow the Activity to be garbage-collected
        private final WeakReference<Activity> weakActivity;
        final String zipFilename, categorySelected, uploadTitleText, uploadDescriptionText,
                fileIcon, uploaderProfileIndex;
        Uri zipFileUri;

        // Suppresses default constructor
        @SuppressWarnings("unused")
        private ZipTask() {
            weakActivity = null;
            this.zipFilename = null;
            this.categorySelected = null;
            this.uploadTitleText = null;
            this.uploadDescriptionText = null;
            this.fileIcon = null;
            this.uploaderProfileIndex = null;
        }

        ZipTask(Activity uploadsActivity, @NonNull String zipFilename,
                @NonNull String categorySelected, @NonNull String uploadTitleText,
                @NonNull String uploadDescriptionText, @NonNull String fileIcon,
                @NonNull String uploaderProfileIndex) {
            weakActivity = new WeakReference<>(uploadsActivity);
            this.zipFilename = zipFilename;
            this.categorySelected = categorySelected;
            this.uploadTitleText = uploadTitleText;
            this.uploadDescriptionText = uploadDescriptionText;
            this.fileIcon = fileIcon;
            this.uploaderProfileIndex = uploaderProfileIndex;
        }

        @Override
        protected void onPreExecute() {
            assert weakActivity != null;
            Toast.makeText(weakActivity.get(), "Zipping files", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(Uri... filesToZip) {
            if (weakActivity == null || zipFilename == null) {
                return false;
            }
            File zipFile = UploadsHelper.createZipFile(zipFilename);

            if (zipFile == null) {
                return false;
            }
            zipFileUri = FileProvider.getUriForFile(weakActivity.get(),
                    weakActivity.get().getPackageName() +
                            ".provider", zipFile);

            UploadsHelper.zip(weakActivity.get(), filesToZip, zipFileUri);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (weakActivity == null) {
                return;
            }

            if (!result) {
                Toast.makeText(weakActivity.get(), "Couldn't create zip!", Toast.LENGTH_SHORT).show();
                return;
            }

            String uploadID = UUID.randomUUID().toString();
            if (!uploadFile(weakActivity.get(), uploadID,
                    getConfigForUpload(weakActivity.get(), uploadID, zipFilename, categorySelected,
                            uploadTitleText, uploadDescriptionText, fileIcon, uploaderProfileIndex,
                            zipFileUri), categorySelected,
                    uploadTitleText, uploadDescriptionText, fileIcon, uploaderProfileIndex,
                    zipFileUri)) {
                Toast.makeText(weakActivity.get(), "Couldn't initiate upload.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}