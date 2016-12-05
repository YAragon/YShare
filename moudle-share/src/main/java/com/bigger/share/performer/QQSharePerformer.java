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

package com.bigger.share.performer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.bigger.share.config.IQQShareConfig;
import com.bigger.share.entity.ShareEntity;
import com.bigger.share.utils.ShareUtil;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.util.ArrayList;

public class QQSharePerformer {

    private Tencent mTencent;
    private String mDefaultImgUrl;
    private String mTargetAppName;
    private String mQQAppId;

    public static QQSharePerformer newInstance(IQQShareConfig config) {
        if (null == config) {
            throw new RuntimeException("IQQShareConfig 不能为空 ！");
        }

        QQSharePerformer performer = new QQSharePerformer(ShareUtil.getAppContext(), config.qqAppId());
        performer.setDefaultImgUrl(config.qqDefaultImgUrl());
        performer.setTargetAppName(config.qqDefaultAppName());
        performer.setQqAppId(config.qqAppId());

        return performer;
    }

    private QQSharePerformer(Context context, String qqAppId) {
        mTencent = Tencent.createInstance(qqAppId, context.getApplicationContext());
    }

    private void setDefaultImgUrl(String imgUrl) {
        mDefaultImgUrl = imgUrl;
    }

    private void setTargetAppName(String appName) {
        mTargetAppName = appName;
    }

    private void setQqAppId(String appId) {
        mQQAppId = appId;
    }

    public void qqOnActivityResult(int requestCode, int resultCode, Intent data, IUiListener listener) {
        mTencent.onActivityResultData(requestCode, resultCode, data, listener);
    }

    public void qqShare(Context context, ShareEntity shareEntity, IUiListener listener) {
        Bundle params = new Bundle();
        if (shareEntity.getQqTarget() == shareEntity.QQ_ZONE) {//是否只分享到qq空间
            params.putInt(QQShare.SHARE_TO_QQ_EXT_INT, QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
        }
        //qq的图片分享，支持GIF图片
        int shareObjectType = shareEntity.getShareObjectType();
        if (shareObjectType == ShareEntity.ObjectType_Pic || shareObjectType == ShareEntity.ObjectType_Emoji) {
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, null != shareEntity.getFile() ? shareEntity.getFile().getAbsolutePath() : "");
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mTargetAppName);
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
            mTencent.shareToQQ((Activity) context, params, null);

        } else if (shareObjectType == ShareEntity.ObjectType_WEB) {
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, shareEntity.getTitle());
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, shareEntity.getDescription());
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, shareEntity.getUrl());

            ArrayList<String> imgList = new ArrayList<>();
            File file = shareEntity.getFile();
            if (null != file) {
                if (shareEntity.getQqTarget() == shareEntity.QQ_ZONE) {//是否只分享到qq空间
                    imgList.add(file.getAbsolutePath());
                    params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imgList);
                } else {
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, file.getAbsolutePath());
                }

            } else {
                String imgUrl = shareEntity.getImageUrl();
                imgUrl = TextUtils.isEmpty(imgUrl) ? mDefaultImgUrl : imgUrl;

                if (shareEntity.getQqTarget() == shareEntity.QQ_ZONE) {//是否只分享到qq空间
                    imgList.add(imgUrl);
                    params.putStringArrayList(QQShare.SHARE_TO_QQ_IMAGE_URL, imgList);
                } else {
                    params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, imgUrl);
                }
            }

            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mTargetAppName + mQQAppId);
            //qq分享标志
            if (shareEntity.getQqTarget() == shareEntity.QQ_ZONE) {//是否只分享到qq空间
                mTencent.shareToQzone((Activity) context, params, listener);
            } else {
                mTencent.shareToQQ((Activity) context, params, listener);
            }
        }
    }
}
