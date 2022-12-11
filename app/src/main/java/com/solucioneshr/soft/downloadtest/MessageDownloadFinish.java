package com.solucioneshr.soft.downloadtest;

public class MessageDownloadFinish {
    private String status = "";
    private String msg = "";


    public MessageDownloadFinish(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }


    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
