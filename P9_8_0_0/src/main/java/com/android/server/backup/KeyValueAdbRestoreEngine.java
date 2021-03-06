package com.android.server.backup;

import android.app.IBackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FullBackup;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Slog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;

class KeyValueAdbRestoreEngine implements Runnable {
    private static final boolean DEBUG = false;
    private static final String TAG = "KeyValueAdbRestoreEngine";
    IBackupAgent mAgent;
    private final BackupManagerService mBackupManagerService;
    private final File mDataDir;
    ParcelFileDescriptor mInFD;
    FileMetadata mInfo;
    PerformAdbRestoreTask mRestoreTask;
    int mToken;

    KeyValueAdbRestoreEngine(BackupManagerService backupManagerService, File dataDir, FileMetadata info, ParcelFileDescriptor inFD, IBackupAgent agent, int token) {
        this.mBackupManagerService = backupManagerService;
        this.mDataDir = dataDir;
        this.mInfo = info;
        this.mInFD = inFD;
        this.mAgent = agent;
        this.mToken = token;
    }

    public void run() {
        try {
            invokeAgentForAdbRestore(this.mAgent, this.mInfo, prepareRestoreData(this.mInfo, this.mInFD), 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File prepareRestoreData(FileMetadata info, ParcelFileDescriptor inFD) throws IOException {
        String pkg = info.packageName;
        File restoreDataName = new File(this.mDataDir, pkg + ".restore");
        File sortedDataName = new File(this.mDataDir, pkg + ".sorted");
        FullBackup.restoreFile(inFD, info.size, info.type, info.mode, info.mtime, restoreDataName);
        sortKeyValueData(restoreDataName, sortedDataName);
        return sortedDataName;
    }

    private void invokeAgentForAdbRestore(IBackupAgent agent, FileMetadata info, File restoreData, int versionCode) throws IOException {
        File newStateName = new File(this.mDataDir, info.packageName + ".new");
        try {
            agent.doRestore(ParcelFileDescriptor.open(restoreData, 268435456), versionCode, ParcelFileDescriptor.open(newStateName, 1006632960), this.mToken, this.mBackupManagerService.mBackupManagerBinder);
        } catch (IOException e) {
            Slog.e(TAG, "Exception opening file. " + e);
        } catch (RemoteException e2) {
            Slog.e(TAG, "Exception calling doRestore on agent: " + e2);
        }
    }

    private void sortKeyValueData(File restoreData, File sortedData) throws IOException {
        FileOutputStream outputStream;
        Throwable th;
        Object inputStream;
        AutoCloseable autoCloseable = null;
        AutoCloseable outputStream2 = null;
        try {
            FileInputStream inputStream2 = new FileInputStream(restoreData);
            try {
                outputStream = new FileOutputStream(sortedData);
            } catch (Throwable th2) {
                th = th2;
                inputStream = inputStream2;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                if (outputStream2 != null) {
                    IoUtils.closeQuietly(outputStream2);
                }
                throw th;
            }
            try {
                copyKeysInLexicalOrder(new BackupDataInput(inputStream2.getFD()), new BackupDataOutput(outputStream.getFD()));
                if (inputStream2 != null) {
                    IoUtils.closeQuietly(inputStream2);
                }
                if (outputStream != null) {
                    IoUtils.closeQuietly(outputStream);
                }
            } catch (Throwable th3) {
                th = th3;
                Object outputStream3 = outputStream;
                inputStream = inputStream2;
                if (autoCloseable != null) {
                    IoUtils.closeQuietly(autoCloseable);
                }
                if (outputStream2 != null) {
                    IoUtils.closeQuietly(outputStream2);
                }
                throw th;
            }
        } catch (Throwable th4) {
            th = th4;
            if (autoCloseable != null) {
                IoUtils.closeQuietly(autoCloseable);
            }
            if (outputStream2 != null) {
                IoUtils.closeQuietly(outputStream2);
            }
            throw th;
        }
    }

    private void copyKeysInLexicalOrder(BackupDataInput in, BackupDataOutput out) throws IOException {
        Map<String, byte[]> data = new HashMap();
        while (in.readNextHeader()) {
            String key = in.getKey();
            int size = in.getDataSize();
            if (size < 0) {
                in.skipEntityData();
            } else {
                byte[] value = new byte[size];
                in.readEntityData(value, 0, size);
                data.put(key, value);
            }
        }
        List<String> keys = new ArrayList(data.keySet());
        Collections.sort(keys);
        for (String key2 : keys) {
            value = (byte[]) data.get(key2);
            out.writeEntityHeader(key2, value.length);
            out.writeEntityData(value, value.length);
        }
    }
}
