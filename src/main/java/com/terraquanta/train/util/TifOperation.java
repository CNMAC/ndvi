package com.terraquanta.train.util;

import com.terraquanta.train.entity.ReadResult;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconstConstants;
import scala.Int;

public class TifOperation {
    /**
     * 读取Tif文件
     * @param tifFileName if文件路径
     * @return ReadResult
     */
    public static ReadResult ReadTifToArray(String tifFileName){
        //Register all known configured GDAL drivers.
        gdal.AllRegister();
        //路径包含中文加上
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES");

        Dataset ds = gdal.Open(tifFileName, gdalconstConstants.GA_ReadOnly);
        if (ds == null){
            return null;
        }
        //获取第一个频段数据，仅包含一个频段
        Band tif_band = ds.GetRasterBand(1);
        Integer width = tif_band.getXSize();
        Integer height = tif_band.getYSize();
        float[] res_data = new float[width * height];
        tif_band.ReadRaster(0,0,tif_band.getXSize(),tif_band.getYSize(),res_data);
        tif_band.delete();
        ds.delete();
        //返回需要数据
        return new ReadResult(res_data,width,height);
    }

    /**
     * 导出新的tif图
     * @param savePath 保存路径
     * @param width 宽
     * @param height 高
     * @param res_data NDVI计算结果
     */
    public static void SaveResultInTif(String savePath,Integer width,Integer height,float [] res_data){

        Driver getDriver = gdal.GetDriverByName("GTiff");
        String[] option=null;
        //绘制栅格
        Dataset dataSet = getDriver.Create(savePath, width, height, 1, gdalconstConstants.GDT_Float32, option);
        Band writeBand = dataSet.GetRasterBand(1);
        //写入数据
        writeBand.WriteRaster(0, 0, width, height, res_data);
        dataSet.FlushCache();
        //关闭io
        dataSet.delete();
        writeBand.delete();
    }
}
