package com.chatwing.whitelabel.pojos;

import java.io.Serializable;

/**
 * Created by steve on 05/03/2015.
 */
public class Emoticon implements Serializable{
    private String symbol;
    private String image;
    private int width;
    private int height;

    public String getSymbol() {
        return symbol;
    }

    public String getImage() {
        return image;
    }
}
