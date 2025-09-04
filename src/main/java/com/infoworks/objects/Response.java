package com.infoworks.objects;

public class Response extends Message {

    public static Response CreateErrorResponse(Throwable exp){
        Response newInstance = null;
        newInstance = new Response();
        newInstance.update(exp);
        return newInstance;
    }

    public Response update(Throwable exp){
        error = exp.getMessage();
        return this;
    }

    private Integer status = 404;
    private String error;
    private String message;

    public Response() {}

    public Integer getStatus() {
        return status;
    }

    public Response setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getError() {
        return error;
    }

    public Response setError(String error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Response setMessage(String message) {
        this.message = message;
        return this;
    }
}
