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

package com.bigger.share.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.bigger.share.ShareManager;
import com.bigger.share.entity.ShareEntity;
import com.bigger.share.utils.ShareUtil;

public abstract class BaseShareView extends FrameLayout {

    protected enum TargetApp {
        WX("com.tencent.mm", "微信"), QQ("com.tencent.mobileqq", "QQ");

        private String targetAppPkgName;
        private String targetAppName;

        TargetApp(String pkgName, String name) {
            this.targetAppPkgName = pkgName;
            this.targetAppName = name;
        }

        public String targetAppPkgName() {
            return targetAppPkgName;
        }

        public String targetAppName() {
            return targetAppName;
        }
    }

    protected interface IShareIconClickInterceptor {
        /**
         * 分享点击事件的中断器，可在点击后而未进入最终分享流程时，执行一些必要的操作（比如检测APP是否安装等等）
         *
         * @return true：本次点击分享中断； false：本次点击分享继续执行（进入最终分享流程）
         */
        boolean shareClickIntercept();
    }

    private View mShareIconView;

    private ShareEntity mShareEntity;
    private TargetApp mTargetApp;
    private IShareIconClickInterceptor mShareIconClickInterceptor;

    /**
     * 若子类有其他的View的点击事件也是进行分享操作（即：与mShareIconView 有相同的点击行为），则可以 setOnClickListener(mShareClickListener)
     */
    protected OnClickListener mShareClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            if (null == mTargetApp) {
                throw new RuntimeException("TargetApp 不能为空");
            }

            if (null != mShareIconClickInterceptor && mShareIconClickInterceptor.shareClickIntercept()) {
                return;
            }

            ShareManager.getInstance().share(ShareUtil.getOriginalActivity(getContext()), mShareEntity);
        }
    };

    public BaseShareView(Context context) {
        this(context, null);
    }

    public BaseShareView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseShareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, layoutId(), this);
        this.mTargetApp = targetApp();
        this.mShareIconView = shareIconView();
        setShareIconClickListener();
    }

    private void setShareIconClickListener() {
        if (null != mShareIconView) {
            mShareIconView.setOnClickListener(mShareClickListener);
        }
    }

    public void setShareEntity(ShareEntity shareEntity) {
        this.mShareEntity = shareEntity;
    }

    protected void setShareIconClickInterceptor(IShareIconClickInterceptor interceptor) {
        this.mShareIconClickInterceptor = interceptor;
    }

    public abstract int layoutId();

    public abstract View shareIconView();

    public abstract TargetApp targetApp();
}
