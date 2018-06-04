package io.straas.android.media.demo.widget;

public class StraasConfiguration {

    private boolean mEnableDefaultWidget;
    private boolean mEnableDefaultSwitchQuality;
    private boolean mEnableDefaultSwitchSpeed;
    private boolean mEnableDefaultChannelName;
    private boolean mEnableDefaultSummaryViewer;
    private boolean mEnableDefaultLoadingProgressBar;
    private boolean mEnableDefaultContentProgressBar;
    private boolean mEnableDefaultPlay;
    private boolean mEnableDefaultPause;
    private boolean mEnableDefaultErrorMessage;
    private boolean mEnableDefaultBroadcastStateMessage;
    private boolean mEnableDefaultReplay;
    private boolean mEnableDefaultPrevious;
    private boolean mEnableDefaultNext;

    private StraasConfiguration(Builder builder) {
        mEnableDefaultWidget = !builder.mDisableDefaultWidget;
        mEnableDefaultSwitchQuality = !builder.mDisableDefaultSwitchQuality;
        mEnableDefaultSwitchSpeed = !builder.mDisableDefaultSwitchSpeed;
        mEnableDefaultChannelName = !builder.mDisableDefaultChannelName;
        mEnableDefaultSummaryViewer = !builder.mDisableDefaultSummaryViewer;
        mEnableDefaultLoadingProgressBar = !builder.mDisableDefaultLoadingProgressBar;
        mEnableDefaultContentProgressBar = !builder.mDisableDefaultContentProgressBar;
        mEnableDefaultPlay = !builder.mDisableDefaultPlay;
        mEnableDefaultPause = !builder.mDisableDefaultPause;
        mEnableDefaultErrorMessage = !builder.mDisableDefaultErrorMessage;
        mEnableDefaultBroadcastStateMessage = !builder.mDisableDefaultBroadcastStateMessage;
        mEnableDefaultReplay = !builder.mDisableDefaultReplay;
        mEnableDefaultPrevious = !builder.mDisableDefaultPrevious;
        mEnableDefaultNext = !builder.mDisableDefaultNext;
    }

    public boolean isEnableDefaultWidget() {
        return mEnableDefaultWidget;
    }

    public boolean isEnableDefaultSwitchQuality() {
        return mEnableDefaultSwitchQuality;
    }

    public boolean isEnableDefaultSwitchSpeed() {
        return mEnableDefaultSwitchSpeed;
    }

    public boolean isEnableDefaultChannelName() {
        return mEnableDefaultChannelName;
    }

    public boolean isEnableDefaultSummaryViewer() {
        return mEnableDefaultSummaryViewer;
    }

    public boolean isEnableDefaultLoadingProgressBar() {
        return mEnableDefaultLoadingProgressBar;
    }

    public boolean isEnableDefaultContentProgressBar() {
        return mEnableDefaultContentProgressBar;
    }

    public boolean isEnableDefaultPlay() {
        return mEnableDefaultPlay;
    }

    public boolean isEnableDefaultPause() {
        return mEnableDefaultPause;
    }

    public boolean isEnableDefaultReplay() {
        return mEnableDefaultReplay;
    }

    public boolean isEnableDefaultSkipToPrevious() {
        return mEnableDefaultPrevious;
    }

    public boolean isEnableDefaultSkipToNext() {
        return mEnableDefaultNext;
    }

    public boolean isEnableDefaultErrorMessage() {
        return mEnableDefaultErrorMessage;
    }

    public boolean isEnableDefaultBroadcastStateMessage() {
        return mEnableDefaultBroadcastStateMessage;
    }

    public static class Builder {

        private boolean mDisableDefaultWidget;
        private boolean mDisableDefaultSwitchQuality;
        private boolean mDisableDefaultSwitchSpeed;
        private boolean mDisableDefaultChannelName;
        private boolean mDisableDefaultSummaryViewer;
        private boolean mDisableDefaultLoadingProgressBar;
        private boolean mDisableDefaultContentProgressBar;
        private boolean mDisableDefaultPlay;
        private boolean mDisableDefaultPause;
        private boolean mDisableDefaultErrorMessage;
        private boolean mDisableDefaultBroadcastStateMessage;
        private boolean mDisableDefaultReplay;
        private boolean mDisableDefaultPrevious;
        private boolean mDisableDefaultNext;

        public StraasConfiguration build() {
            return new StraasConfiguration(this);
        }

        public Builder disableDefaultWidget() {
            mDisableDefaultWidget = true;
            return this;
        }


        public Builder disableDefaultSwitchQuality() {
            mDisableDefaultSwitchQuality = true;
            return this;
        }

        public Builder disableDefaultSwitchSpeed() {
            mDisableDefaultSwitchSpeed = true;
            return this;
        }

        public Builder disableDefaultChannelName() {
            mDisableDefaultChannelName = true;
            return this;
        }

        public Builder disableDefaultSummaryViewer() {
            mDisableDefaultSummaryViewer = true;
            return this;
        }

        public Builder disableDefaultLoadingProgressBar() {
            mDisableDefaultLoadingProgressBar = true;
            return this;
        }

        public Builder disableDefaultContentProgressBar() {
            mDisableDefaultContentProgressBar = true;
            return this;
        }

        public Builder disableDefaultPlay() {
            mDisableDefaultPlay = true;
            return this;
        }

        public Builder disableDefaultPause() {
            mDisableDefaultPause = true;
            return this;
        }

        public Builder disableDefaultReplay() {
            mDisableDefaultReplay = true;
            return this;
        }

        public Builder disableDefaultSkipToPrevious() {
            mDisableDefaultPrevious = true;
            return this;
        }

        public Builder disableDefaultSkipToNext() {
            mDisableDefaultNext = true;
            return this;
        }

        public Builder disableDefaultErrorMessage() {
            mDisableDefaultErrorMessage = true;
            return this;
        }

        public Builder disableDefaultBroadcastStateMessage() {
            mDisableDefaultBroadcastStateMessage = true;
            return this;
        }
    }
}
