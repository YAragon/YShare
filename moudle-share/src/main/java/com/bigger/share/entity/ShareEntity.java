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

package com.bigger.share.entity;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;

import java.io.File;
import java.io.Serializable;

public class ShareEntity implements Serializable {
    /*
    ------------------------------------------说明-----------------------------------------------
    目前只设计了qq及微信相关的封装，如果需要其他类型(如微博、Facebook等)，需要拓展设计,
    例如ShareEntity可以再细分为各个平台对应的更为具体的ShareEntity，但总的思路是不变的.

    注意：setAppTarget + setQqTarget/setWxTarget 两个方法确定一个具体平台的分享，如下图所示

    |- setAppTarget(int appTarget)
                             |-APP_QQ
                             |    |-setQqTarget(int qqTarget)
                             |                          |-QQ_DEFAULT
                             |                          |-QQ_ZONE
                             |-APP_WX
                                  |-setWxTarget(int wxTarget)
                                                        |-WX_CHAT
                                                        |-WX_MOMENT
    ---------------------------------------------------------------------------------------------
    */

    public static final int WX_CHAT = SendMessageToWX.Req.WXSceneSession;
    public static final int WX_MOMENT = SendMessageToWX.Req.WXSceneTimeline;

    public static final int QQ_ZONE = 1;
    public static final int QQ_DEFAULT = 2;

    public static final int ObjectType_Pic = 1;
    public static final int ObjectType_WEB = 2;
    public static final int ObjectType_Emoji = 3;
    public static final int ObjectType_WEB_WX_URL = 4;//微信分享时icon为网络图片

    public static final int APP_QQ = 1;
    public static final int APP_WX = 2;

    private String title = "";
    private String description = "";
    private String imageUrl = "";

    /**
     * <p>微信分享展示的地方</p>
     * <p>可取值: {@link #WX_MOMENT} (朋友圈),  或者{@link #WX_CHAT} (微信好友)</p>
     */
    private int wxTarget = WX_MOMENT;

    /**
     * <p>qq分享展示的地方</p>
     * <p>可取值: {@link #QQ_DEFAULT} (默认QQ分享),  或者{@link #QQ_ZONE} (qq空间)</p>
     */
    private int qqTarget = QQ_DEFAULT;

    /**
     * <p>分享的类型</p>
     * <p>可取值：</p>
     * <p>{@link #ObjectType_Pic}：图片</p>
     * <p>{@link #ObjectType_WEB}：网页</p>
     * <p>{@link #ObjectType_Emoji}：gif图</p>
     * <p>{@link #ObjectType_WEB_WX_URL}：微信网页分享且icon为网络图片</p>
     */
    private int shareObjectType;

    /**
     * <p>分享的app</p>
     * <p>可取值</p>
     * <p>{@link #APP_QQ}：qq</p>
     * <p>{@link #APP_WX}：微信</p>
     */
    private int appTarget;

    private File file = null;

    private ShareEntity() {
    }

    public void setWxTarget(int wxTarget) {
        this.wxTarget = wxTarget;
    }

    public void setQqTarget(int qqTarget) {
        this.qqTarget = qqTarget;
    }

    public void setAppTarget(int appTarget) {
        this.appTarget = appTarget;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setShareObjectType(int shareObjectType) {
        this.shareObjectType = shareObjectType;
    }

    public int getWxTarget() {
        return wxTarget;
    }

    public int getAppTarget() {
        return appTarget;
    }

    public int getQqTarget() {
        return qqTarget;
    }

    public File getFile() {
        return file;
    }

    public String getUrl() {
        return url;
    }

    private String url = null;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {

        return imageUrl;
    }

    public int getShareObjectType() {
        return shareObjectType;
    }

    public static class Builder {
        private int wxTarget = WX_MOMENT;
        private int qqTarget = QQ_DEFAULT;
        private int appTarget;
        private File file = null;
        private String url = null;
        private String title = null;
        private String description = null;
        private String imageUrl;
        private int shareObjectType;

        public Builder setWxTarget(int wxTarget) {
            this.wxTarget = wxTarget;
            return this;
        }

        public Builder setQqTarget(int qqTarget) {
            this.qqTarget = qqTarget;
            return this;
        }

        public Builder setAppTarget(int appTarget) {
            this.appTarget = appTarget;
            return this;
        }

        public Builder setFile(File file) {
            this.file = file;
            return this;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder setShareObjectType(int shareObjectType) {
            this.shareObjectType = shareObjectType;
            return this;
        }

        public ShareEntity build() {
            ShareEntity entity = new ShareEntity();
            entity.setWxTarget(wxTarget);
            entity.setQqTarget(qqTarget);
            entity.setAppTarget(appTarget);
            entity.setFile(file);
            entity.setUrl(url);
            entity.setTitle(title);
            entity.setDescription(description);
            entity.setImageUrl(imageUrl);
            entity.setShareObjectType(shareObjectType);
            return entity;
        }
    }
}
