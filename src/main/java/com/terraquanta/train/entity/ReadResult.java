package com.terraquanta.train.entity;

public class ReadResult {
    private Integer Width;
    private Integer Height;
    private float[] TifValus;

    public ReadResult(float[] res, Integer width, Integer height){
        this.Width = width;
        this.Height = height;
        this.TifValus = res;
    }


    public Integer getWidth() {
        return Width;
    }

    public void setWidth(Integer width) {
        Width = width;
    }

    public Integer getHeight() {
        return Height;

    }

    public void setHeight(Integer height) {
        Height = height;
    }

    public float[] getTifValus() {
        return TifValus;
    }

    public void setTifValus(float[] tifValus) {
        TifValus = tifValus;
    }
}
