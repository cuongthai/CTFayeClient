package com.chatwing.whitelabel.services;

import com.chatwing.whitelabel.modules.ChatWingModule;
import com.chatwing.whitelabel.modules.ExtendChatWingModule;

import java.util.Arrays;
import java.util.List;

/**
 * Created by steve on 01/03/2015.
 */
public abstract class ExtendBaseIntentService extends BaseIntentService {
    public ExtendBaseIntentService(String name) {
        super(name);
    }

    @Override
    protected List<Object> getModules() {
        return Arrays.asList(new ChatWingModule(this), new ExtendChatWingModule(this));
    }
}
