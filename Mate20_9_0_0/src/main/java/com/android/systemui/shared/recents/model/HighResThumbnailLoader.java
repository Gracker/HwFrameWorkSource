package com.android.systemui.shared.recents.model;

import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.SystemClock;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.shared.recents.model.Task.TaskCallbacks;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class HighResThumbnailLoader implements TaskCallbacks {
    private final ActivityManagerWrapper mActivityManager;
    private boolean mFlingingFast;
    private final boolean mIsLowRamDevice;
    @GuardedBy("mLoadQueue")
    private final ArrayDeque<Task> mLoadQueue = new ArrayDeque();
    private final Thread mLoadThread;
    private final Runnable mLoader = new Runnable() {
        public void run() {
            Process.setThreadPriority(11);
            while (true) {
                Task next = null;
                synchronized (HighResThumbnailLoader.this.mLoadQueue) {
                    if (!HighResThumbnailLoader.this.mLoading || HighResThumbnailLoader.this.mLoadQueue.isEmpty()) {
                        try {
                            HighResThumbnailLoader.this.mLoaderIdling = true;
                            HighResThumbnailLoader.this.mLoadQueue.wait();
                            HighResThumbnailLoader.this.mLoaderIdling = false;
                        } catch (InterruptedException e) {
                        }
                    } else {
                        next = (Task) HighResThumbnailLoader.this.mLoadQueue.poll();
                        if (next != null) {
                            HighResThumbnailLoader.this.mLoadingTasks.add(next);
                        }
                    }
                }
                if (next != null) {
                    loadTask(next);
                }
            }
            while (true) {
            }
        }

        private void loadTask(Task t) {
            HighResThumbnailLoader.this.mMainThreadHandler.post(new -$$Lambda$HighResThumbnailLoader$1$s-1KK0EnA0WJuK_oehEz11H5MbU(this, t, HighResThumbnailLoader.this.mActivityManager.getTaskThumbnail(t.key.id, false)));
        }

        public static /* synthetic */ void lambda$loadTask$0(AnonymousClass1 anonymousClass1, Task t, ThumbnailData thumbnail) {
            synchronized (HighResThumbnailLoader.this.mLoadQueue) {
                HighResThumbnailLoader.this.mLoadingTasks.remove(t);
            }
            if (HighResThumbnailLoader.this.mVisibleTasks.contains(t)) {
                t.notifyTaskDataLoaded(thumbnail, t.icon);
            }
        }
    };
    @GuardedBy("mLoadQueue")
    private boolean mLoaderIdling;
    private boolean mLoading;
    @GuardedBy("mLoadQueue")
    private final ArraySet<Task> mLoadingTasks = new ArraySet();
    private final Handler mMainThreadHandler;
    private boolean mTaskLoadQueueIdle;
    private boolean mVisible;
    private final ArrayList<Task> mVisibleTasks = new ArrayList();

    public HighResThumbnailLoader(ActivityManagerWrapper activityManager, Looper looper, boolean isLowRamDevice) {
        this.mActivityManager = activityManager;
        this.mMainThreadHandler = new Handler(looper);
        this.mLoadThread = new Thread(this.mLoader, "Recents-HighResThumbnailLoader");
        this.mLoadThread.start();
        this.mIsLowRamDevice = isLowRamDevice;
    }

    public void setVisible(boolean visible) {
        if (!this.mIsLowRamDevice) {
            this.mVisible = visible;
            updateLoading();
        }
    }

    public void setFlingingFast(boolean flingingFast) {
        if (this.mFlingingFast != flingingFast && !this.mIsLowRamDevice) {
            this.mFlingingFast = flingingFast;
            updateLoading();
        }
    }

    public void setTaskLoadQueueIdle(boolean idle) {
        if (!this.mIsLowRamDevice) {
            this.mTaskLoadQueueIdle = idle;
            updateLoading();
        }
    }

    @VisibleForTesting
    boolean isLoading() {
        return this.mLoading;
    }

    private void updateLoading() {
        boolean z = this.mVisible && !this.mFlingingFast && this.mTaskLoadQueueIdle;
        setLoading(z);
    }

    private void setLoading(boolean loading) {
        if (loading != this.mLoading) {
            synchronized (this.mLoadQueue) {
                this.mLoading = loading;
                if (loading) {
                    startLoading();
                } else {
                    stopLoading();
                }
            }
        }
    }

    @GuardedBy("mLoadQueue")
    private void startLoading() {
        for (int i = this.mVisibleTasks.size() - 1; i >= 0; i--) {
            Task t = (Task) this.mVisibleTasks.get(i);
            if (!((t.thumbnail != null && !t.thumbnail.reducedResolution) || this.mLoadQueue.contains(t) || this.mLoadingTasks.contains(t))) {
                this.mLoadQueue.add(t);
            }
        }
        this.mLoadQueue.notifyAll();
    }

    @GuardedBy("mLoadQueue")
    private void stopLoading() {
        this.mLoadQueue.clear();
        this.mLoadQueue.notifyAll();
    }

    public void onTaskVisible(Task t) {
        t.addCallback(this);
        this.mVisibleTasks.add(t);
        if ((t.thumbnail == null || t.thumbnail.reducedResolution) && this.mLoading) {
            synchronized (this.mLoadQueue) {
                this.mLoadQueue.add(t);
                this.mLoadQueue.notifyAll();
            }
        }
    }

    public void onTaskInvisible(Task t) {
        t.removeCallback(this);
        this.mVisibleTasks.remove(t);
        synchronized (this.mLoadQueue) {
            this.mLoadQueue.remove(t);
        }
    }

    @VisibleForTesting
    void waitForLoaderIdle() {
        while (true) {
            synchronized (this.mLoadQueue) {
                if (this.mLoadQueue.isEmpty() && this.mLoaderIdling) {
                    return;
                }
            }
            SystemClock.sleep(100);
        }
        while (true) {
        }
    }

    public void onTaskDataLoaded(Task task, ThumbnailData thumbnailData) {
        if (thumbnailData != null && !thumbnailData.reducedResolution) {
            synchronized (this.mLoadQueue) {
                this.mLoadQueue.remove(task);
            }
        }
    }

    public void onTaskDataUnloaded() {
    }

    public void onTaskWindowingModeChanged() {
    }
}
