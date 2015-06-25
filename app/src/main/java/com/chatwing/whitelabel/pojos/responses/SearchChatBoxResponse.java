package com.chatwing.whitelabel.pojos.responses;


import com.chatwing.whitelabel.pojos.LightWeightChatBox;

import java.util.ArrayList;


/**
 * Created by nguyenthanhhuy on 10/26/13.
 */
public class SearchChatBoxResponse extends BaseResponse {
    private ArrayList<LightWeightChatBox> data;

    public ArrayList<LightWeightChatBox> getData() {
        return data;
    }
}
