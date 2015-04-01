package com.chatwing.whitelabel.pojos.params;

import com.chatwingsdk.pojos.params.Params;
import com.google.gson.annotations.SerializedName;

/**
 * Created by steve on 25/07/2014.
 */
public class FlagMessageParams extends Params {
    private String id;

    public FlagMessageParams(String messageID) {
        id = messageID;
    }
}
