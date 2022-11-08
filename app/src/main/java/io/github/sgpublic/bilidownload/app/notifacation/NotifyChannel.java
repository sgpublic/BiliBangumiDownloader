package io.github.sgpublic.bilidownload.app.notifacation;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.LocusIdCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.graphics.drawable.IconCompat;

import io.github.sgpublic.bilidownload.R;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * 通知频道封装，利用枚举更优雅的实现通知管理，若需添加新的通知频道，在此添加即可。
 * @author Madray Haven
 * @date 2022/10/10 11:44
 */
@AllArgsConstructor
@RequiredArgsConstructor
public enum NotifyChannel {
    DownloadingTask(R.string.title_chanel_download, R.string.text_chanel_download);

    /**
     * 构造器参数 1，频道名称，请传入字符串资源
     * @see NotificationChannelCompat.Builder#setName(CharSequence)
     */
    @StringRes
    private final int channelName;
    /**
     * 构造器参数 2，频道介绍，请传入字符串资源
     * @see NotificationChannelCompat.Builder#setDescription(String)
     */
    @StringRes
    private final int channelDesc;

    /**
     * 构造器参数 3（可选），优先级
     * @see NotificationChannelCompat.Builder#setImportance(int)
     */
    private int importance = NotificationManager.IMPORTANCE_DEFAULT;

    /**
     * 构造器参数 4（可选），是否显示通知灯
     * @see NotificationChannelCompat.Builder#setLightsEnabled(boolean)
     */
    private boolean enableLights = false;

    /**
     * 构造器参数 5（可选），是否显示桌面图标角标
     * @see NotificationChannelCompat.Builder#setShowBadge(boolean)
     */
    private boolean showBadge = false;

    /**
     * 在当前通知频道配置下创建一个通知构建器
     * @param context 上下文
     * @return 通知构建器
     */
    public Builder newBuilder(@NonNull Context context) {
        return new Builder(context, name());
    }

    /**
     * 初始化通知频道，系统语言更改时自动重新初始化，Android 8.0 以下没有通知频道功能不需要初始化
     * @param application 上下文
     */
    public static void init(@NonNull final Application application) {
        realInit(application);
        application.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                realInit(application);
            }
        }, new IntentFilter(Intent.ACTION_LOCALE_CHANGED));
    }

    private static void realInit(@NonNull Application application) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(application);
        for (NotifyChannel value : NotifyChannel.values()) {
            NotificationChannelCompat.Builder channel =
                    new NotificationChannelCompat.Builder(value.name(), value.importance);
            channel.setLightsEnabled(value.enableLights);
            channel.setShowBadge(value.showBadge);
            channel.setName(application.getString(value.channelName));
            channel.setDescription(application.getString(value.channelDesc));
            manager.createNotificationChannel(channel.build());
        }
    }

    /**
     * Notification 封装，支持仅传入 id 发送通知，需通过 NotifyChannel.Builder 创建
     * @see NotifyChannel.Builder
     * @see android.app.Notification
     */
    public static class Notification extends android.app.Notification {
        private final Context context;
        private final android.app.Notification notification;
        private Notification(Context context, android.app.Notification notification) {
            this.context = context;
            this.notification = notification;
        }

        /**
         * 发送通知，使用已有 id 会更新现有通知
         * @param id 通知 id
         */
        public void send(int id) {
            NotificationManagerCompat.from(context).notify(id, notification);
        }

        /**
         * 发送通知以启动前台服务，使用已有 id 会更新现有通知
         * @param id 通知 id
         */
        public void startForeground(int id) {
            if (context instanceof Service) {
                ((Service) context).startForeground(id, notification);
            } else {
                throw new IllegalStateException("Current context is not a Service, cannot call startForeground()!");
            }
        }
    }

    /**
     * 通知构建器封装，构建时返回封装后的 Notification
     * @see NotifyChannel#newBuilder(Context)
     */
    public static class Builder extends NotificationCompat.Builder {
        private final Context context;
        private Builder(Context context, String name) {
            super(context, name);
            this.context = context;
        }

        @NonNull
        @Override
        public Notification build() {
            return new Notification(context, super.build());
        }

        @NonNull
        @Override
        public Builder setAllowSystemGeneratedContextualActions(boolean allowed) {
            super.setAllowSystemGeneratedContextualActions(allowed);
            return this;
        }

        @NonNull
        @Override
        public Builder setAutoCancel(boolean autoCancel) {
            super.setAutoCancel(autoCancel);
            return this;
        }

        @NonNull
        @Override
        public Builder setBadgeIconType(int icon) {
            super.setBadgeIconType(icon);
            return this;
        }

        @NonNull
        @Override
        public Builder setBubbleMetadata(@Nullable NotificationCompat.BubbleMetadata data) {
            super.setBubbleMetadata(data);
            return this;
        }

        @NonNull
        @Override
        public Builder setCategory(@Nullable String category) {
            super.setCategory(category);
            return this;
        }

        @NonNull
        @Override
        public Builder setChannelId(@NonNull String channelId) {
            super.setChannelId(channelId);
            return this;
        }

        @NonNull
        @Override
        public Builder setChronometerCountDown(boolean countsDown) {
            super.setChronometerCountDown(countsDown);
            return this;
        }

        @NonNull
        @Override
        public Builder setColor(int argb) {
            super.setColor(argb);
            return this;
        }

        @NonNull
        @Override
        public Builder setColorized(boolean colorize) {
            super.setColorized(colorize);
            return this;
        }

        @NonNull
        @Override
        public Builder setContent(@Nullable RemoteViews views) {
            super.setContent(views);
            return this;
        }

        @NonNull
        @Override
        public Builder setContentInfo(@Nullable CharSequence info) {
            super.setContentInfo(info);
            return this;
        }

        @NonNull
        @Override
        public Builder setContentIntent(@Nullable PendingIntent intent) {
            super.setContentIntent(intent);
            return this;
        }

        @NonNull
        @Override
        public Builder setContentText(@Nullable CharSequence text) {
            super.setContentText(text);
            return this;
        }

        @NonNull
        @Override
        public Builder setContentTitle(@Nullable CharSequence title) {
            super.setContentTitle(title);
            return this;
        }

        @NonNull
        @Override
        public Builder setCustomBigContentView(@Nullable RemoteViews contentView) {
            super.setCustomBigContentView(contentView);
            return this;
        }

        @NonNull
        @Override
        public Builder setCustomContentView(@Nullable RemoteViews contentView) {
            super.setCustomContentView(contentView);
            return this;
        }

        @NonNull
        @Override
        public Builder setCustomHeadsUpContentView(@Nullable RemoteViews contentView) {
            super.setCustomHeadsUpContentView(contentView);
            return this;
        }

        @NonNull
        @Override
        public Builder setDefaults(int defaults) {
            super.setDefaults(defaults);
            return this;
        }

        @NonNull
        @Override
        public Builder setDeleteIntent(@Nullable PendingIntent intent) {
            super.setDeleteIntent(intent);
            return this;
        }

        @NonNull
        @Override
        public Builder setExtras(@Nullable Bundle extras) {
            super.setExtras(extras);
            return this;
        }

        @NonNull
        @Override
        public Builder setForegroundServiceBehavior(int behavior) {
            super.setForegroundServiceBehavior(behavior);
            return this;
        }

        @NonNull
        @Override
        public Builder setFullScreenIntent(@Nullable PendingIntent intent, boolean highPriority) {
            super.setFullScreenIntent(intent, highPriority);
            return this;
        }

        @NonNull
        @Override
        public Builder setGroup(@Nullable String groupKey) {
            super.setGroup(groupKey);
            return this;
        }

        @NonNull
        @Override
        public Builder setGroupAlertBehavior(int groupAlertBehavior) {
            super.setGroupAlertBehavior(groupAlertBehavior);
            return this;
        }

        @NonNull
        @Override
        public Builder setGroupSummary(boolean isGroupSummary) {
            super.setGroupSummary(isGroupSummary);
            return this;
        }

        @NonNull
        @Override
        public Builder setLargeIcon(@Nullable Bitmap icon) {
            super.setLargeIcon(icon);
            return this;
        }

        @NonNull
        @Override
        public Builder setLights(int argb, int onMs, int offMs) {
            super.setLights(argb, onMs, offMs);
            return this;
        }

        @NonNull
        @Override
        public Builder setLocalOnly(boolean b) {
            super.setLocalOnly(b);
            return this;
        }

        @NonNull
        @Override
        public Builder setLocusId(@Nullable LocusIdCompat locusId) {
            super.setLocusId(locusId);
            return this;
        }

        @NonNull
        @Override
        public Builder setNumber(int number) {
            super.setNumber(number);
            return this;
        }

        @NonNull
        @Override
        public Builder setOngoing(boolean ongoing) {
            super.setOngoing(ongoing);
            return this;
        }

        @NonNull
        @Override
        public Builder setOnlyAlertOnce(boolean onlyAlertOnce) {
            super.setOnlyAlertOnce(onlyAlertOnce);
            return this;
        }

        @NonNull
        @Override
        public Builder setPriority(int pri) {
            super.setPriority(pri);
            return this;
        }

        @NonNull
        @Override
        public Builder setProgress(int max, int progress, boolean indeterminate) {
            super.setProgress(max, progress, indeterminate);
            return this;
        }

        @NonNull
        @Override
        public Builder setPublicVersion(@Nullable android.app.Notification n) {
            super.setPublicVersion(n);
            return this;
        }

        @NonNull
        @Override
        public Builder setRemoteInputHistory(@Nullable CharSequence[] text) {
            super.setRemoteInputHistory(text);
            return this;
        }

        @NonNull
        @Override
        public Builder setSettingsText(@Nullable CharSequence text) {
            super.setSettingsText(text);
            return this;
        }

        @NonNull
        @Override
        public Builder setShortcutId(@Nullable String shortcutId) {
            super.setShortcutId(shortcutId);
            return this;
        }

        @NonNull
        @Override
        public Builder setShortcutInfo(@Nullable ShortcutInfoCompat shortcutInfo) {
            super.setShortcutInfo(shortcutInfo);
            return this;
        }

        @NonNull
        @Override
        public Builder setShowWhen(boolean show) {
            super.setShowWhen(show);
            return this;
        }

        @NonNull
        @Override
        public Builder setSilent(boolean silent) {
            super.setSilent(silent);
            return this;
        }

        @NonNull
        @Override
        public Builder setSmallIcon(int icon) {
            super.setSmallIcon(icon);
            return this;
        }

        @NonNull
        @Override
        public Builder setSmallIcon(@NonNull IconCompat icon) {
            super.setSmallIcon(icon);
            return this;
        }

        @NonNull
        @Override
        public Builder setSmallIcon(int icon, int level) {
            super.setSmallIcon(icon, level);
            return this;
        }

        @NonNull
        @Override
        public Builder setSortKey(@Nullable String sortKey) {
            super.setSortKey(sortKey);
            return this;
        }

        @NonNull
        @Override
        public Builder setSound(@Nullable Uri sound) {
            super.setSound(sound);
            return this;
        }

        @NonNull
        @Override
        public Builder setSound(@Nullable Uri sound, int streamType) {
            super.setSound(sound, streamType);
            return this;
        }

        @NonNull
        @Override
        public Builder setStyle(@Nullable NotificationCompat.Style style) {
            super.setStyle(style);
            return this;
        }

        @NonNull
        @Override
        public Builder setSubText(@Nullable CharSequence text) {
            super.setSubText(text);
            return this;
        }

        @NonNull
        @Override
        public Builder setTicker(@Nullable CharSequence tickerText) {
            super.setTicker(tickerText);
            return this;
        }

        @NonNull
        @Override
        public Builder setTimeoutAfter(long durationMs) {
            super.setTimeoutAfter(durationMs);
            return this;
        }

        @NonNull
        @Override
        public Builder setUsesChronometer(boolean b) {
            super.setUsesChronometer(b);
            return this;
        }

        @NonNull
        @Override
        public Builder setVibrate(@Nullable long[] pattern) {
            super.setVibrate(pattern);
            return this;
        }

        @NonNull
        @Override
        public Builder setVisibility(int visibility) {
            super.setVisibility(visibility);
            return this;
        }

        @NonNull
        @Override
        public Builder setWhen(long when) {
            super.setWhen(when);
            return this;
        }

        @NonNull
        @Override
        public Builder setNotificationSilent() {
            super.setNotificationSilent();
            return this;
        }

        @NonNull
        @Override
        public Builder setTicker(@Nullable CharSequence tickerText, @Nullable RemoteViews views) {
            super.setTicker(tickerText, views);
            return this;
        }
    }
}
