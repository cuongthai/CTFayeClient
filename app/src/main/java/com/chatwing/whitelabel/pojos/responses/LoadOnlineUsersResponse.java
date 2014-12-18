package com.chatwing.whitelabel.pojos.responses;

import com.chatwing.whitelabel.pojos.OnlineUser;
import com.chatwingsdk.pojos.responses.BaseResponse;

import java.util.List;

/**
 * Author: Huy Nguyen
 * Date: 7/25/13
 * Time: 3:09 PM
 */
public class LoadOnlineUsersResponse extends BaseResponse {
    private Data data;

    public Data getData() {
        return data;
    }

    public static class Data {
        private int count;
        private List<OnlineUser> list;

        public int getCount() {
            return count;
        }

        public List<OnlineUser> getList() {
            return list;
        }
    }
}
