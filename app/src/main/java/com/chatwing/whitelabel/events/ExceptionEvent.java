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

package com.chatwing.whitelabel.events;

/**
 * Author: Huy Nguyen
 * Date: 7/4/13
 * Time: 2:44 PM
 * <p/>
 * An abstract event that contains an exception if any.
 * By convention, when an instance of this event does not have an exception,
 * it's a success event.
 */
public abstract class ExceptionEvent {
    private Exception exception;

    protected ExceptionEvent() {
    }

    protected ExceptionEvent(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
