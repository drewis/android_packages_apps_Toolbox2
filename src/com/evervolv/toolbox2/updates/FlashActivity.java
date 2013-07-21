package com.evervolv.toolbox2.updates;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.evervolv.toolbox2.R;
import com.evervolv.toolbox2.misc.Constants;
import com.evervolv.toolbox2.updates.misc.afiledialog.FileChooserDialog;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FlashActivity extends Activity {

    private TextView mZipText;
    private ListView mGappsListView;
    private ZipAdapter mAdapter;
    private File[] mGappsList;
    private List<Zip> mZipItems = new ArrayList<Zip>();
    private String mFileName;
    private String mBuildType;
    private int mWhichGapps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_dialog);

        mFileName = getIntent().getStringExtra(Constants.EXTRA_FLASH_ZIP_NAME);
        mBuildType = getIntent().getStringExtra(Constants.EXTRA_FLASH_BUILD_TYPE);


/*
        mGappsList = Utils.getFilesInDir(UpdatesFragment.BASE_STORAGE_LOCATION + "/gapps/", ".zip");
        mZipItems = new String[mGappsList.length + 1];
        mZipItems[0] = "None"; // Hack

        int i = 1;
        for (File zip : mGappsList) {
            mZipItems[i] = zip.getName();
            i++;
        }
*/
        mZipItems.add(new Zip(mFileName, mBuildType));
        mAdapter = new ZipAdapter(this, mZipItems);

        mGappsListView = (ListView) findViewById(R.id.gapps_list);
        SwipeDismissListViewTouchListener swipeDismissTouchListener =
                new SwipeDismissListViewTouchListener(
                        mGappsListView,
                        new SwipeDismissListViewTouchListener.OnDismissCallback() {
                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    mAdapter.remove(mAdapter.getItem(position));
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        });
        swipeDismissTouchListener.setEnabled(true);
        mGappsListView.setOnScrollListener(swipeDismissTouchListener.makeScrollListener());
        mGappsListView.setOnTouchListener(swipeDismissTouchListener);
        mGappsListView.setItemsCanFocus(true);
        mGappsListView.setAdapter(mAdapter);
        mGappsListView.performItemClick(null, 0, mGappsListView.getFirstVisiblePosition());

        Button cancelButton = (Button) findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button rebootButton = (Button) findViewById(R.id.button_reboot);
        rebootButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tempTwrpDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.flash_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                FileChooserDialog dialog = new FileChooserDialog(this);
                dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
                    public void onFileSelected(Dialog source, File file) {
                        mZipItems.add(new Zip(file.getName(), Constants.BUILD_TYPE_GAPPS)); // No way to actually know need to just pass the file object
                        mAdapter.notifyDataSetChanged();
                        source.dismiss();
                    }
                    public void onFileSelected(Dialog source, File folder, String name) {
                        //Pass, called when file is created, we should disable that
                    }
                });
                dialog.setFilter(".*zip");
                dialog.loadFolder(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/" + Constants.DOWNLOAD_DIRECTORY + Constants.BUILD_TYPE_GAPPS);
                dialog.show();
                return true;
        }
        return false;
    }

    /* TODO: Temporary dialog warning for TWRP support only,
     * remove when necessary
     */
    private void tempTwrpDialog() {
        AlertDialog.Builder twrpDialog = new AlertDialog.Builder(this);
        twrpDialog.setTitle(R.string.alert_dialag_warning_title);
        twrpDialog.setMessage(R.string.alert_dialag_warning_message);

        twrpDialog.setPositiveButton(R.string.okay,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                buildOpenRecoveryScript(mZipItems);
                dialog.dismiss();
            }
        });
        twrpDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        twrpDialog.show();
    }

    /*
     * TODO: We should create an OpenRecoveryScript class to house all or most of this.
     */
    private void buildOpenRecoveryScript(List<Zip> items) {
        try {
            Process p = Runtime.getRuntime().exec("sh");
            OutputStream o = p.getOutputStream();
            o.write("mkdir -p /cache/recovery/\n".getBytes());
            o.write("echo -n > /cache/recovery/openrecoveryscript\n".getBytes());
            if (false) { //TODO prompt
                o.write("echo 'backup SDBO' >> /cache/recovery/openrecoveryscript\n".getBytes());
            }
            if (!items.isEmpty()) {
                for (Zip i: items) {
                    /* Using local path should prevent fuckups from different recovery mount points */
                    o.write(String.format("echo 'install %s' >> %s\n",
                            Constants.DOWNLOAD_DIRECTORY + i.getBuildType() + "/"
                                    + i.getFileName(), "/cache/recovery/openrecoveryscript").getBytes());
                }
            } else {
                //pass we should never be here
            }
            /* TODO FEATURE:
             * Add cache / dalvik cache wiping options 
             */
            o.flush();
            //PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            //pm.reboot("recovery");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class ZipAdapter extends ArrayAdapter<Zip>{
        private List<Zip> mZipList = new ArrayList<Zip>();
        private Context mContext;

        public ZipAdapter(Context context, List<Zip> items) {
            super(context, R.layout.listview_flash_item, items);
            mContext = context;
            mZipList = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listview_flash_item, parent, false);
            }

            TextView zipName = (TextView) convertView.findViewById(R.id.zip_name);
            zipName.setText(mZipList.get(position).getFileName());

            String buildType = mZipList.get(position).getBuildType();
            int iconResource = -1;
            if (buildType.equals(Constants.BUILD_TYPE_NIGHTLIES)) {
                iconResource = R.drawable.ic_pref_nightly_build;
            } else if (buildType.equals(Constants.BUILD_TYPE_RELEASE)) {
                iconResource = R.drawable.ic_pref_release_build;
            } else if (buildType.equals(Constants.BUILD_TYPE_TESTING)) {
                iconResource = R.drawable.ic_pref_testing_build;
            } else if (buildType.equals(Constants.BUILD_TYPE_GAPPS)) {
                iconResource = R.drawable.ic_pref_google_apps;
            }
            ImageView zipIcon = (ImageView) convertView.findViewById(R.id.zip_icon);
            zipIcon.setImageResource(iconResource);

            return convertView;
        }
    }

    class Zip {
        private String mFileName;
        private String mBuildType;

        public Zip(String filename, String type) {
            mFileName = filename;
            mBuildType = type;
        }

        public String getFileName() {
            return mFileName;
        }

        public String getBuildType() {
            return mBuildType;
        }
    }

}
