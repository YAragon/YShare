/*
 * MIT License
 *
 * Copyright (c) 2016 YAragon

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.bigger.share.config;

import android.content.Context;

public class ShareConfig {
    private IQQShareConfig mQqShareConfig;
    private IWXShareConfig mWXShareConfig;
    private Context mApplication;

    private ShareConfig() {
    }

    private void setAppContext(Context context){
        this.mApplication = context;
    }

    private void setQQShareConfig(IQQShareConfig config) {
        this.mQqShareConfig = config;
    }

    private void setWXShareConfig(IWXShareConfig config) {
        this.mWXShareConfig = config;
    }

    public Context appContext(){
        return mApplication;
    }

    public IQQShareConfig getQqShareConfig(){
        return mQqShareConfig;
    }

    public IWXShareConfig getWXShareConfig(){
        return mWXShareConfig;
    }

    public static class Builder {
        private IQQShareConfig qqShareConfig;
        private IWXShareConfig wxShareConfig;
        private Context app;

        private Builder(){}

        public static Builder create(){
            return new Builder();
        }

        public Builder setQQShareConfig(IQQShareConfig config) {
            this.qqShareConfig = config;
            return this;
        }

        public Builder setWXShareConfig(IWXShareConfig config) {
            this.wxShareConfig = config;
            return this;
        }

        /**
         * 设置Application级别的Context更为合适
         */
        public Builder setAppContext(Context context){
            this.app = context;
            return this;
        }

        public ShareConfig build() {
            ShareConfig shareConfig = new ShareConfig();
            shareConfig.setAppContext(app);
            shareConfig.setQQShareConfig(qqShareConfig);
            shareConfig.setWXShareConfig(wxShareConfig);
            return shareConfig;
        }
    }
}
