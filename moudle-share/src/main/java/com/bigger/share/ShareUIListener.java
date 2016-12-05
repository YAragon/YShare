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


import com.bigger.share.entity.ShareEntity;
import com.bigger.share.entity.ShareResult;

/**
 * <p>Description: ShareUIListener，ShareUIListener的实例对象无需被WeakReference包装（因为在ShareManager中被包装过了，多重WeakReference时，对象实例容易被gc回收） </p>
 * @author: Aragon.Wu
 * @date: 2016-12-02
 * @vserion: 1.0
 */
public interface ShareUIListener {
    /**
     * 注意：该回调结果在UI线程执行，不能执行耗时操作！
     * @param entity 当次分享的ShareEntity
     * @param result 分享结果
     */
    void onShareResult(ShareEntity entity, ShareResult result);
}
