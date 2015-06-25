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


import com.chatwing.whitelabel.tasks.CallbackTask;

/**
 * Author: Huy Nguyen
 * Date: 5/26/13
 * Time: 11:35 PM
 */
public class TaskFinishedEvent {

    public static TaskFinishedEvent succeedEvent(CallbackTask task,
                                                 Object result) {
        TaskFinishedEvent event = new TaskFinishedEvent();
        event.status = Status.SUCCEED;
        event.task = task;
        event.result = result;
        return event;
    }

    public static TaskFinishedEvent failedEvent(CallbackTask task,
                                                Exception exception) {
        TaskFinishedEvent event = new TaskFinishedEvent();
        event.status = Status.FAILED;
        event.task = task;
        event.exception = exception;
        return event;
    }

    public enum Status {
        SUCCEED,
        FAILED
    }

    private TaskFinishedEvent() {
    }

    private Status status;
    private CallbackTask task;
    private Object result;
    private Exception exception;

    public Status getStatus() {
        return status;
    }

    public CallbackTask getTask() {
        return task;
    }

    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
}
