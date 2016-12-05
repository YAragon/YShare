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

package com.bigger.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bigger.share.config.ShareConfig;
import com.bigger.share.entity.ShareEntity;
import com.bigger.share.entity.ShareResult;
import com.bigger.share.performer.QQSharePerformer;
import com.bigger.share.performer.WXSharePerformer;
import com.bigger.share.utils.ShareUtil;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;

public class ShareManager {

    private static ShareManager mInstance;

    private IUiListener mUiListener;
    private IWXAPIEventHandler mWXListener;

    private ShareEntity mCurrShareEntity;
    private ConcurrentHashMap<Integer, WeakReference<ShareUIListener>> mShareListeners;

    private WXSharePerformer mWXSharePerformer;
    private QQSharePerformer mQQSharePerformer;

    private ShareManager() {
        mShareListeners = new ConcurrentHashMap<>();
        createQQShareListener();
        createWXShareListener();
    }

    /**
     * 初始化各个平台的分享，IConfig不能为空
     *
     * @param config config 不能为空
     */
    public void initConfig(ShareConfig config) {
        if (null == config) {
            throw new RuntimeException("IConfig 不能为空！");
        }

        ShareUtil.setContext(config.appContext());
        mQQSharePerformer = QQSharePerformer.newInstance(config.getQqShareConfig());
        mWXSharePerformer = WXSharePerformer.newInstance(config.getWXShareConfig());
    }

    public static ShareManager getInstance() {
        if (null == mInstance) {
            synchronized (ShareManager.class) {
                if (null == mInstance) {
                    mInstance = new ShareManager();
                }
            }
        }
        return mInstance;
    }

    private void createWXShareListener() {
        mWXListener = new IWXAPIEventHandler() {

            @Override
            public void onReq(BaseReq baseReq) {
                // do nothing
            }

            @Override
            public void onResp(BaseResp baseResp) {
                switch (baseResp.errCode) {
                    case BaseResp.ErrCode.ERR_OK: {
                        notifyShareResult(createShareResult(ShareResult.ResultCode.SUCCESS));
                        break;
                    }

                    case BaseResp.ErrCode.ERR_USER_CANCEL: {
                        notifyShareResult(createShareResult(ShareResult.ResultCode.CANCEL));
                        break;
                    }

                    case BaseResp.ErrCode.ERR_SENT_FAILED: {
                        notifyShareResult(createShareResult(ShareResult.ResultCode.FAIL));
                        break;
                    }
                }
            }
        };
    }

    private void createQQShareListener() {
        //qq分享回调
        mUiListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                ShareResult shareResult = createShareResult(ShareResult.ResultCode.SUCCESS);
                notifyShareResult(shareResult);
            }

            @Override
            public void onError(UiError uiError) {
                ShareResult shareResult = createShareResult(ShareResult.ResultCode.FAIL);
                shareResult.setErrorMsg(null == uiError ? "" : uiError.errorMessage);
                notifyShareResult(shareResult);
            }

            @Override
            public void onCancel() {
                ShareResult shareResult = createShareResult(ShareResult.ResultCode.CANCEL);
                notifyShareResult(shareResult);
            }
        };
    }

    private ShareResult createShareResult(ShareResult.ResultCode resultCode) {
        ShareResult shareResult = new ShareResult();
        shareResult.setResultCode(resultCode);
        shareResult.setEntity(mCurrShareEntity);
        return shareResult;
    }

    private void notifyShareResult(ShareResult shareResult) {
        for (WeakReference<ShareUIListener> listenerRef : mShareListeners.values()) {
            ShareUIListener listener = listenerRef.get();
            if (null != listener) {
                listener.onShareResult(mCurrShareEntity, shareResult);
            }
        }
    }

    /**
     * WXEntryActivity中调用该方法通过ShareManager进行分享结果中转（通知已注册的监听）
     */
    public void transferWXAPIOnRequest(BaseReq baseReq) {
        mWXListener.onReq(baseReq);
    }

    /**
     * WXEntryActivity中调用该方法通过ShareManager进行分享结果中转（通知已注册的监听）
     */
    public void transferWXAPIOnResponse(BaseResp baseResp) {
        mWXListener.onResp(baseResp);
    }

    /**
     * 注册业务层的分享监听
     *
     * @param listener ShareUIListener 实例
     */
    public void registerShareListener(ShareUIListener listener) {
        if (null != listener) {
            mShareListeners.put(listener.hashCode(), new WeakReference<>(listener));
        }
    }

    /**
     * 注销业务层的分享监听，在不需要监听时注销掉,尽量保证在同一个地方（对象）注册与注销（形成闭环）
     *
     * @param listener ShareUIListener 实例
     */
    public void unRegisterShareListener(ShareUIListener listener) {
        if (null != listener) {
            mShareListeners.remove(listener.hashCode(), listener);
        }
    }

    /**
     * 所有涉及到QQ/QQ控件分享的Activity，都必须调用此方法（否则收不到回调）
     */
    public void qqOnActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == mQQSharePerformer) {
            throw new RuntimeException("ShareManager initConfig 方法未调用！");
        }

        mQQSharePerformer.qqOnActivityResult(requestCode, resultCode, data, mUiListener);
    }

    public void share(Context context, ShareEntity entity) {
        if (null == entity) {
            return;
        }

        mCurrShareEntity = entity;

        int appTarget = entity.getAppTarget();
        switch (appTarget) {
            case ShareEntity.APP_QQ: {
                checkActivity(context);
                mQQSharePerformer.qqShare(context, entity, mUiListener);
                break;
            }

            case ShareEntity.APP_WX: {
                mWXSharePerformer.wxShare(entity);
                break;
            }
        }
    }

    private void checkActivity(Context context) {
        if (null == context || !(context instanceof Activity)) {
            throw new RuntimeException("QQ 相关分享需要传入 Activity 对象！！");
        }
    }
}
