/*
 * Copyright (C) 2014 ChatWing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chatwing.whitelabel.views;

import android.content.Context;

import com.chatwing.whitelabel.Constants;
import com.chatwing.whitelabel.R;
import com.chatwing.whitelabel.modules.ForApplication;
import com.github.kevinsawicki.http.HttpRequest;

import javax.inject.Inject;

/**
 * Created by nguyenthanhhuy on 4/17/14.
 */
public class ErrorMessageView extends QuickMessageView {
    @Inject
    ErrorMessageView(@ForApplication Context context) {
        super(context);
    }

    public void show(Exception exc) {
        show(exc, null);
    }

    public void show(Exception exc, String detailMessage) {
        if (exc instanceof HttpRequest.HttpRequestException) {
            show(mContext.getString(R.string.error_network_connection));
            return;
        }
        //In here we only have very general exceptions such as ApiException
        //User must not see anything too technical
        if (Constants.DEBUG) {
            show(exc.getLocalizedMessage());
        } else {
            show(detailMessage != null
                    ? detailMessage
                    : mContext.getString(R.string.error_unknown));
        }
    }
}
