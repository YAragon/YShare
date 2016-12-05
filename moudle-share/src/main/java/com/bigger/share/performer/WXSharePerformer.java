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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.bigger.share.config.IWXShareConfig;
import com.bigger.share.entity.ShareEntity;
import com.bigger.share.utils.ShareUtil;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class WXSharePerformer {
    private static final int WX_MAX = 120;
    private IWXAPI mWXApi;

    private int mDefaultThumbDrawableId;

    private WXSharePerformer(Context context, String wxAppId) {
        mWXApi = WXAPIFactory.createWXAPI(context, wxAppId, true);
        mWXApi.registerApp(wxAppId);
    }

    public static WXSharePerformer newInstance(IWXShareConfig config) {
        if (null == config) {
            throw new RuntimeException("IWXShareConfig 不能为空 ！");
        }

        WXSharePerformer performer = new WXSharePerformer(ShareUtil.getAppContext(), config.wxAppId());
        performer.setDefaultThumbDrawableId(config.wxDefaultThumbDrawableId());

        return performer;
    }

    public void wxShare(ShareEntity shareEntity) {
        int shareObjectType = shareEntity.getShareObjectType();
        if (shareObjectType == ShareEntity.ObjectType_Pic) {
            WXSharePic(shareEntity.getFile(), shareEntity.getWxTarget());
        } else if (shareObjectType == ShareEntity.ObjectType_WEB) {
            shareNetImgWithThumbLocal(shareEntity);
        } else if (shareObjectType == ShareEntity.ObjectType_WEB_WX_URL) {
            shareNetImgWithThumbUrl(shareEntity);
        } else if (shareObjectType == ShareEntity.ObjectType_Emoji) {
            WXShareEmoji(shareEntity.getFile(), shareEntity.getWxTarget());
        }
    }

    private void setDefaultThumbDrawableId(int drawableId) {
        this.mDefaultThumbDrawableId = drawableId;
    }

    /**
     * gif图片分享，注意：微信暂时不支持朋友圈分享gif
     */
    private void WXShareEmoji(File file, int target) {
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        WXEmojiObject fileObj = new WXEmojiObject();
        fileObj.emojiPath = file.getAbsolutePath();
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = fileObj;
        //设置缩略图
        int thumbWidth = 100;
        if (bmp != null) {
            float rate = bmp.getWidth() / 100f;
            int thumbHeight = (int) (bmp.getWidth() / rate);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true);
            msg.thumbData = bmpToByteArray(thumbBmp, true);
            bmp.recycle();
        }

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = new Date().getTime() + "";
        req.message = msg;
        req.scene = target;

        mWXApi.sendReq(req);
    }

    /**
     * @param file
     * @param target
     */
    private void WXSharePic(File file, int target) {
        Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bmp == null)
            return;
        WXImageObject imgObj = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        //设置缩略图,宽度固定100
        int thumbWidth = 100;
        float rate = bmp.getWidth() / 100f;
        int thumbHeight = (int) (bmp.getWidth() / rate);
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true);
        msg.thumbData = bmpToByteArray(thumbBmp, true);
        bmp.recycle();

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = new Date().getTime() + "";
        req.message = msg;
        req.scene = target;
        mWXApi.sendReq(req);
    }

    /**
     * 通过url获取适合微信分享的位图。注意：微信分享时的icon图标在发送给微信时不能过大， 以120*120以下为宜（当然
     * 也可以直接设置icon的长宽）
     */
    private Bitmap getBitMap4WXApi(Bitmap bmp, boolean recycle) {
        Bitmap scaledBitmap;
        if (bmp == null) {
            scaledBitmap = null;
        } else {
            int bmpWidth = bmp.getWidth();
            int bmpHeight = bmp.getHeight();


            if (bmpWidth < WX_MAX && bmpHeight < WX_MAX) {
                //当图片长宽均小于WX_MAX大小时，不进行强制设置，否则图片失真，微信分享失败
                scaledBitmap = Bitmap.createScaledBitmap(bmp, bmpHeight,
                        bmpHeight, true);
            } else {
                float scaleWitdth = ((float) WX_MAX) / bmpWidth;
                float scaleHeight = ((float) WX_MAX) / bmpHeight;

                float chooseScale = scaleWitdth > scaleHeight ? scaleHeight
                        : scaleWitdth;

                int chooseWidth = (int) (chooseScale * bmpWidth);
                int chooseHeight = (int) (chooseScale * bmpHeight);

                if (chooseWidth > WX_MAX || chooseHeight > WX_MAX) {
                    chooseWidth = WX_MAX;
                    chooseHeight = WX_MAX;
                }

                scaledBitmap = Bitmap.createScaledBitmap(bmp, chooseWidth,
                        chooseHeight, true);
            }

            if (recycle) {
                bmp.recycle();
            }
        }
        return scaledBitmap;
    }

    private Bitmap getBitMap4WXApi(String url, boolean fixed, boolean recycle) {

        Bitmap scaledBitmap = null;

        try {
            Bitmap bmp = BitmapFactory.decodeStream(new URL(url).openStream());
            if (bmp == null) {
                return null;
            }
            if (!fixed) {
                scaledBitmap = getBitMap4WXApi(bmp, recycle);
            } else {
                scaledBitmap = Bitmap.createScaledBitmap(bmp, WX_MAX, WX_MAX, true);
            }

        } catch (MalformedURLException e) {
            scaledBitmap = null;
            e.printStackTrace();
        } catch (IOException e) {
            scaledBitmap = null;
            e.printStackTrace();
        }
        return scaledBitmap;
    }

    /**
     * 分享网络图片（缩略图为本地图片）
     *
     * @param shareEntity ShareEntity 实例
     */
    private void shareNetImgWithThumbLocal(ShareEntity shareEntity) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareEntity.getUrl();

        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareEntity.getTitle();
        msg.description = shareEntity.getDescription();

        //缩略图
        File icon = shareEntity.getFile();
        if (icon != null) {
            Bitmap thumb = BitmapFactory.decodeFile(icon.getAbsolutePath());
            msg.thumbData = bmpToByteArray(thumb, true);
        } else {
            Bitmap thumb = BitmapFactory.decodeResource(ShareUtil.getAppContext().getResources(), mDefaultThumbDrawableId);
            msg.thumbData = bmpToByteArray(thumb, true);
        }
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = new Date().getTime() + "";
        req.message = msg;
        req.scene = shareEntity.getWxTarget();
        mWXApi.sendReq(req);
    }

    /**
     * 分享网络图片（缩略图为网络图片，需要下载成本地图片处理后再分享）
     *
     * @param shareEntity ShareEntity 实例
     */
    private void shareNetImgWithThumbUrl(ShareEntity shareEntity) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = shareEntity.getUrl();

        final WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = shareEntity.getTitle();
        msg.description = shareEntity.getDescription();
        final SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = new Date().getTime() + "";
        req.message = msg;
        req.scene = shareEntity.getWxTarget();
        final String iconUrl = shareEntity.getImageUrl();
        new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = null;
                if (TextUtils.isEmpty(iconUrl)) {
                    Bitmap thumb = BitmapFactory.decodeResource(ShareUtil.getAppContext().getResources(), mDefaultThumbDrawableId);
                    bitmap = getBitMap4WXApi(thumb, false);//thumb 不回收
                } else {
                    bitmap = getBitMap4WXApi(iconUrl, false, false);//thumb 不回收
                }

                if (null != bitmap) {
                    msg.thumbData = bmpToByteArray(bitmap, true);//thumb 必须回收
                }
                mWXApi.sendReq(req);
            }
        }.start();
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        if (bmp.isRecycled()) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
