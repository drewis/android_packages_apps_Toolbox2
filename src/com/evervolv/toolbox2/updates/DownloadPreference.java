package com.evervolv.toolbox2.updates;

import java.io.File;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.evervolv.toolbox2.R;
import com.evervolv.toolbox2.updates.db.ManifestEntry;
import com.evervolv.toolbox2.misc.Constants;
import com.evervolv.toolbox2.misc.MD5;
import com.evervolv.toolbox2.misc.Utils;


public class DownloadPreference extends Preference implements OnClickListener {

    private static final String TAG = Constants.TAG;

    public static final int STATE_NOTHING     = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DOWNLOADED  = 2;

    private Context mContext;
    private UpdatesFragment mParent;
    private LinearLayout mDownloadPref;
    private ProgressBar mProgress;
    private TextView mSummary;
    private ImageView mDownloadIcon;
    private ImageView mExpandArrow;

    private ManifestEntry mEntry;
    private String mDate;
    private String mFileName;
    private String mMd5Sum;
    private String mMessage;
    private String mBuildType;
    private int    mSize;

    private int mDownloadStatus;
    private long mDownloadId = -1;
    private String mStorageDir;
    private boolean mInstalled;
    private boolean mNew;

    private File[] mGappsList;
    private int mWhichGapps;
    private CharSequence[] mZipItems;

    private TableLayout mSlidingInfo;
    private boolean mIsDrawerOpen = false;

    private ActionMode mActionMode;
    private TextView mMd5SumLocal;
    private TextView mMd5SumServer;

    public DownloadPreference(Context context, UpdatesFragment parent, ManifestEntry entry) {
        super(context);
        mContext = context;
        mParent = parent;

        mEntry = entry;
        mDate = entry.getDate();
        mFileName = entry.getName();
        mMd5Sum = entry.getMd5sum();
        mMessage = entry.getMessage();
        mBuildType = entry.getType();
        mSize = entry.getSize();

        mStorageDir = UpdatesFragment.BASE_STORAGE_LOCATION + mBuildType + "/";
        mInstalled = Utils.getInstalledVersion().equals(mFileName.replace(".zip", ""));
        mNew = Utils.isNewerThanInstalled(mDate);

        setLayoutResource(R.layout.update_download);
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        mProgress = (ProgressBar) view.findViewById(R.id.download_progress_bar);
        mSummary = (TextView) view.findViewById(android.R.id.summary);
        mDownloadIcon = (ImageView) view.findViewById(R.id.updates_icon);
        mDownloadIcon.setOnClickListener(this);
        if  (mBuildType.equals(Constants.BUILD_TYPE_NIGHTLIES)) {
            mDownloadIcon.setImageResource(R.drawable.ic_pref_nightly_build);
        } else if  (mBuildType.equals(Constants.BUILD_TYPE_RELEASE)) {
            mDownloadIcon.setImageResource(R.drawable.ic_pref_release_build);
        } else if  (mBuildType.equals(Constants.BUILD_TYPE_TESTING)) {
            mDownloadIcon.setImageResource(R.drawable.ic_pref_testing_build);
        } else if (mBuildType.equals(Constants.BUILD_TYPE_GAPPS)) {
            mDownloadIcon.setImageResource(R.drawable.ic_pref_google_apps);
        }

        //TODO: Need to create a "collapse" icon, currently using expand, renamed.
        mExpandArrow = (ImageView) view.findViewById(R.id.updates_arrow);
        mExpandArrow.setImageResource(R.drawable.ic_pref_expand);
        mDownloadPref = (LinearLayout) view.findViewById(R.id.updates_pref);
        mDownloadPref.setOnClickListener(this);

        mSlidingInfo = (TableLayout) view.findViewById(R.id.tab_info);

        File nightly = new File(mStorageDir + mFileName);
        File nightlyPartial = new File(mStorageDir + mFileName + ".partial");

        if (nightly.exists()) {
            updateDownloadUI(STATE_DOWNLOADED);
        } else if (nightlyPartial.exists()) {
            mDownloadId = mParent.checkDownload(mMd5Sum);
            if ( mDownloadId > 0 ) {
                mParent.startDownloadService(mDownloadId);
                updateDownloadUI(STATE_DOWNLOADING);
            } else {
                /* File exists but not being tracked
                   just delete it and start over */
                nightlyPartial.delete();
                updateDownloadUI(STATE_NOTHING);
            }
        } else {
            updateDownloadUI(STATE_NOTHING);
        }

        TextView txtDate = (TextView) view.findViewById(R.id.text_date);
        txtDate.setText(mEntry.getDate());

        TextView txtSize = (TextView) view.findViewById(R.id.text_size);
        txtSize.setText(mEntry.getFriendlySize());

        TextView txtFilename = (TextView) view.findViewById(R.id.text_filename);
        txtFilename.setText(mEntry.getName());

        mMd5SumServer = (TextView) view.findViewById(R.id.text_md5sum_server);
        mMd5SumServer.setText(mEntry.getMd5sum());

        mMd5SumLocal = (TextView) view.findViewById(R.id.text_md5sum_local);
    }

    @Override
    public void onClick(View v) {
        if (v == mDownloadPref) {
            Log.d(TAG, "DrawerOpen: " + mIsDrawerOpen);
            if (!mIsDrawerOpen) {
                // If another preference is expanded, collapse it.
                int prefCount = mParent.getAvailableCategory().getPreferenceCount();
                for (int i = 0; i < prefCount; i++) {
                    DownloadPreference pref = (DownloadPreference) 
                            mParent.getAvailableCategory().getPreference(i);
                    if (pref.mActionMode != null) {
                        pref.mActionMode.finish();
                    }
                }
                animateView(mSlidingInfo, ExpandCollapseAnimation.EXPAND);
                mActionMode = mParent.getActivity().startActionMode(mActionModeCallback);
                mIsDrawerOpen = true;
            } else {
                animateView(mSlidingInfo, ExpandCollapseAnimation.COLLAPSE);
                if (mActionMode != null) {
                    mActionMode.finish();
                }
                mIsDrawerOpen = false;
            }
        }
    }

    private void animateView(final View target, final int type) {
        Animation anim = new ExpandCollapseAnimation(target, type);
        anim.setDuration(350);
        target.startAnimation(anim);
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            int actionMenu = -1;
                switch (mDownloadStatus) {
                    case STATE_NOTHING:
                        actionMenu = R.menu.action_menu_nothing;
                        break;
                    case STATE_DOWNLOADING:
                        actionMenu = R.menu.action_menu_downloading;
                        break;
                    case STATE_DOWNLOADED:
                        actionMenu = R.menu.action_menu_downloaded;
                        Log.d(TAG, "buildType: " + mBuildType);

                        break;
                }
            inflater.inflate(actionMenu, menu);
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // Remove the flash button for gapps
            if (mBuildType.equals(Constants.BUILD_TYPE_GAPPS) &&
                    mDownloadStatus == STATE_DOWNLOADED) {
                menu.removeItem(R.id.menu_flash);
            }

            //Create our local md5sum
            File file = new File(mStorageDir + mEntry.getName());
            if (file.exists()) {
                new CalcMd5Sum().execute(mStorageDir + mEntry.getName());
            } else {
                mMd5SumLocal.setText(R.string.changelog_info_dialog_tab_info_md5sum_local_not_exist);
            }

            mExpandArrow.setImageResource(R.drawable.ic_pref_collapse);
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_download:
                    String url = Constants.FETCH_URL + mFileName;
                    String fullFilename = "file://" + mStorageDir + mFileName + ".partial";
                    File downloadDir = new File(mStorageDir);
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }
                    mDownloadId = mParent.downloadUpdate(url, fullFilename, mMd5Sum);
                    updateDownloadUI(STATE_DOWNLOADING);
                    mode.finish();
                    break;
                case R.id.menu_cancel:
                    mParent.showDialog(UpdatesFragment.DIALOG_CONFIRM_CANCEL, DownloadPreference.this);
                    mode.finish();
                    break;
                case R.id.menu_delete:
                    mParent.showDialog(UpdatesFragment.DIALOG_CONFIRM_DELETE, DownloadPreference.this);
                    mode.finish();
                    break;
                case R.id.menu_changelog:
                    ChangelogDialog dlg = new ChangelogDialog(mEntry, mParent.getActivity());
                    dlg.show(mParent.getChildFragmentManager(), mMd5Sum);
                    break;
                case R.id.menu_flash:
                    Intent intent = new Intent(mContext, FlashActivity.class);
                    intent.putExtra(Constants.EXTRA_FLASH_ZIP_NAME, mFileName);
                    intent.putExtra(Constants.EXTRA_FLASH_BUILD_TYPE, mBuildType);
                    mParent.startActivity(intent);
                    mode.finish();
                    break;
                default:
                    return false;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            animateView(mSlidingInfo, ExpandCollapseAnimation.COLLAPSE);
            mIsDrawerOpen = false;
            mActionMode = null;
            mExpandArrow.setImageResource(R.drawable.ic_pref_expand);
        }
    };

    private class CalcMd5Sum extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mMd5SumLocal.setText(R.string.changelog_info_dialog_tab_info_md5sum_calculate);
        }

        @Override
        protected String doInBackground(String... param) {

            File file = new File(param[0]);
            String md5 = "";
            try {
                md5 = MD5.calculateMD5(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return md5;
        }

        @Override
        protected void onPostExecute(String md5) {
            super.onPostExecute(md5);
            if (md5.equals(mEntry.getMd5sum())) {
                mMd5SumLocal.setTextColor(Color.GREEN);
            } else {
                mMd5SumLocal.setTextColor(Color.RED);
            }
            mMd5SumLocal.setText(md5);
        }
    }

    public void updateDownloadUI(int state) {
        mDownloadStatus = state;
        switch (state) {
            case STATE_NOTHING:
                mProgress.setVisibility(View.GONE);
                if (mInstalled) {
                    mSummary.setText(R.string.status_installed);
                    //mSummary.setTextColor(Color.WHITE);
                    mSummary.setVisibility(View.VISIBLE);
                } else if (mNew) {
                    mSummary.setText(R.string.status_new);
                    mSummary.setVisibility(View.VISIBLE);
                } else {
                    mSummary.setVisibility(View.GONE);
                }
                break;
            case STATE_DOWNLOADING:
                mProgress.setVisibility(View.VISIBLE);
                mSummary.setVisibility(View.GONE);
                break;
            case STATE_DOWNLOADED:
                mProgress.setVisibility(View.GONE);
                if (mInstalled) {
                    mSummary.setText(R.string.status_downloaded_and_installed);
                } else {
                    mSummary.setText(R.string.status_downloaded);
                }
                //mSummary.setTextColor(Color.GREEN);
                mSummary.setVisibility(View.VISIBLE);
                break;
        }
    }

    /* TODO FEATURE:
     * Turn this into a dialog or activity to get a build "ready" to flash, including other zips to flash
     * and options while flashing ( wiping, etc ).
     */
    public void getReadyToFlash() {
        int pickedId;
        mGappsList = Utils.getFilesInDir(UpdatesFragment.BASE_STORAGE_LOCATION + "/gapps/", ".zip");
        mZipItems = new CharSequence[mGappsList.length + 1];
        mZipItems[0] = "None"; // Hack
        int i = 1;
        for (File zip : mGappsList) {
            mZipItems[i] = zip.getName();
            i++;
        }
        Resources res = mContext.getResources();
        AlertDialog.Builder flashDialog = new AlertDialog.Builder(mParent.getActivity());
        flashDialog.setTitle(R.string.alert_dialag_gapps_title);
        flashDialog.setSingleChoiceItems(mZipItems, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mWhichGapps = which;
            }
        });

        flashDialog.setPositiveButton(R.string.reboot,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //buildRecoveryScript(zipItems[mWhichGapps].toString());
                //tempTwrpDialog(); //TODO: Temporary dialog warning for TWRP support only
                dialog.dismiss();
            }
        });
        flashDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        flashDialog.show();
    }


    /* TODO FEATURE:
     * Use mSummary to show download percentage
     */
    public void setState(int state, int progress) {
        switch (state) {
            case DownloadManager.STATUS_RUNNING:
                if (mDownloadStatus != STATE_DOWNLOADING) {
                    updateDownloadUI(STATE_DOWNLOADING);
                }
                mProgress.setIndeterminate(progress <= 0);
                mProgress.setProgress(progress);
                break;
            case DownloadManager.STATUS_PENDING:
            case DownloadManager.STATUS_PAUSED:
                // Blocked in service, leaving for possible future implementation
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                if (mDownloadStatus != STATE_DOWNLOADED) {
                    updateDownloadUI(STATE_DOWNLOADED);
                }
                mDownloadId = -1;
                break;
            case DownloadManager.STATUS_FAILED:
            default:
                if (mDownloadStatus != STATE_NOTHING) {
                    updateDownloadUI(STATE_NOTHING);
                }
                mDownloadId = -1;
                break;
        }
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getDate() {
        return mDate;
    }

    public void setFileName(String name) {
        mFileName = name;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setMd5Sum(String sum) {
        mMd5Sum = sum;
    }

    public String getMd5Sum() {
        return mMd5Sum;
    }

    public void setMessage(String msg) {
        mMessage = msg;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setBuildType(String type) {
        mBuildType = type;
    }

    public String getBuildType() {
        return mBuildType;
    }

    public void setSize(int size) {
        mSize = size;
    }

    public int getSize() {
        return mSize;
    }

    public void setDownloadId(long id) {
        mDownloadId = id;
    }

    public long getDownloadId() {
        return mDownloadId;
    }

    public String getStorageLocation() {
        return mStorageDir;
    }

}