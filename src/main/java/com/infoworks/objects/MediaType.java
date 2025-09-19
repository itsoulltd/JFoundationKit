package com.infoworks.objects;

import java.nio.charset.Charset;

public class MediaType {

    public static String Key = "Content-Type";
    public static MediaType PLAIN_TEXT = new MediaType("text/plain");
    public static MediaType JSON = new MediaType("application/json");
    public static MediaType XML = new MediaType("application/xml");
    public static MediaType HTML = new MediaType("text/html");
    public static MediaType CSV = new MediaType("text/csv");
    public static MediaType PDF = new MediaType("application/pdf", null);
    public static MediaType ZIPPED = new MediaType("application/zip", null);
    public static MediaType BINARY_OCTET_STREAM = new MediaType("application/octet-stream", null);
    public static MediaType JPEG = new MediaType("image/jpeg", null);
    public static MediaType PNG = new MediaType("image/png", null);
    public static MediaType MP4 = new MediaType("video/mp4", null);

    private String typeValue;
    private String charset;
    MediaType(String type){
        this(type, "UTF-8");
    }

    MediaType(String type, String charset){
        this.typeValue = type;
        this.charset = charset;
    }

    /**
     * @return formatted string like "this.typeValue; charset=this.charset"
     * e.g. "application/json; charset=UTF-8"
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(this.typeValue);
        if (this.charset != null && !this.charset.isEmpty())
            builder.append(String.format("; charset=%s", this.charset));
        return builder.toString();
    }

    public String key() {return Key;}
    public String value() {return typeValue;}
    public Charset charset(){
        return (this.charset == null || this.charset.isEmpty())
                ? null
                : Charset.forName(this.charset);
    }

}
