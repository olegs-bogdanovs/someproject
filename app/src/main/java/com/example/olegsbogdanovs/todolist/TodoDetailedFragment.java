package com.example.olegsbogdanovs.todolist;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.olegsbogdanovs.todolist.model.Todo;
import com.example.olegsbogdanovs.todolist.model.TodoListDao;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.UUID;


public class TodoDetailedFragment extends Fragment{

    private Todo mTodo;
    private EditText mTitleField;
    private EditText mDescriptionField;
    private RadioGroup mRadioGroup;
    private ImageView mPhotoView;
    private Button mPhotoButton;
    private Button mSetColorButton;
    private Button mTweetButton;
    private File mPhotoFile;
    private static final int REQUEST_PHOTO = 0;
    private static final String TODO_ID = "todo_id";
    private static final String RED = "red";
    private static final String GREEN = "green";
    private static final String YELLOW = "yellow";


    public TodoDetailedFragment createInstance(UUID todoId){
        Bundle bundle = new Bundle();
        bundle.putSerializable(TODO_ID, todoId);
        TodoDetailedFragment fragment = new TodoDetailedFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID todoId = (UUID)getArguments().getSerializable(TODO_ID);
        mTodo = TodoListDao.get(getActivity()).getTodo(todoId);
        mPhotoFile = TodoListDao.get(getActivity()).getPhotoFile(mTodo);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        // TODO: 4/11/2017 Update in AsyncTask 
        TodoListDao.get(getActivity()).updateTodo(mTodo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detailed_todo, container, false);
        setupUI(view);
        PackageManager packageManager = getActivity().getPackageManager();

        mTitleField = (EditText)view.findViewById(R.id.todo_title);
        mTitleField.setText(mTodo.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTodo.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        mDescriptionField = (EditText)view.findViewById(R.id.todo_description);
        mDescriptionField.setText(mTodo.getDescription());
        mDescriptionField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mTodo.setDescription(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mRadioGroup = (RadioGroup)view.findViewById(R.id.todo_radiogroup);
        switch (mTodo.getColor()){
            case GREEN:
                mRadioGroup.check(R.id.todo_radioButton_green);
                break;
            case RED:
                mRadioGroup.check(R.id.todo_radioButton_red);
                break;
            case YELLOW:
                mRadioGroup.check(R.id.todo_radioButton_yellow);
                break;
            default:
                mRadioGroup.check(R.id.todo_radioButton_yellow);
        }

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.todo_radioButton_green:
                        mTodo.setColor(GREEN);
                        break;
                    case R.id.todo_radioButton_red:
                        mTodo.setColor(RED);
                        break;
                    case R.id.todo_radioButton_yellow:
                        mTodo.setColor(YELLOW);
                        break;
                    default:
                        mTodo.setColor(YELLOW);
                }
            }
        });

        mPhotoButton = (Button) view.findViewById(R.id.todo_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePicture = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePicture);

        if (canTakePicture) {
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });

        mPhotoView = (ImageView) view.findViewById(R.id.todo_photo);
        updatePhotoView();

        mTweetButton = (Button) view.findViewById(R.id.todo_button_tweet);
        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tweetIntent = new Intent(Intent.ACTION_SEND);
                tweetIntent.putExtra(Intent.EXTRA_TEXT, mTodo.getTitle() + " " + mTodo.getDescription());
                tweetIntent.setType("text/plain");

                PackageManager packageManager = getActivity().getPackageManager();
                List<ResolveInfo> resolveInfoList = packageManager
                        .queryIntentActivities(tweetIntent, PackageManager.MATCH_DEFAULT_ONLY);

                boolean resolved = false;
                for(ResolveInfo resolveInfo: resolveInfoList){
                    if(resolveInfo.activityInfo.packageName.startsWith("com.twitter.android")){
                        tweetIntent.setClassName(
                                resolveInfo.activityInfo.packageName,
                                resolveInfo.activityInfo.name );
                        resolved = true;
                        break;
                    }
                }
                if(resolved){
                    startActivity(tweetIntent);
                }else{
                    Toast.makeText(getActivity(), "Twitter app isn't found", Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }



    private void updatePhotoView() {
        Picasso.with(getActivity())
                .load(mPhotoFile)
                .resize(200,200)
                .skipMemoryCache()
                .centerCrop()
                .into(mPhotoView);
    }

    public void setupUI(View view) {
        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(getActivity());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                // TODO: 3/30/2017 Null pointer exception
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PHOTO){
            updatePhotoView();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_todo_detailed_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_delete_todo:
                TodoListDao.get(getActivity()).removeTodo(mTodo);
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
