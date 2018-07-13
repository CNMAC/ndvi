package com.terraquanta.train.geotrellis

import geotrellis.raster._
import geotrellis.raster.io.geotiff.{MultibandGeoTiff, SinglebandGeoTiff}
import geotrellis.raster.render.ColorMap

object Geotrellis_simple_ndvi {

  //计算ndvi
  def calculate_ndvi (r: Double, ir: Double) : Double = {
    if (isData(r) && isData(ir)) {
      (ir - r) / (ir + r)
    } else {
      Double.NaN
    }
  }
  def bandPath(b: String) = s"D:\\gdal\\geotrellis-landsat-tutorial-master\\Tif_band\\LAN_046029_${b}.tif"

  def main(args: Array[String]): Unit = {
    val Array(maskedPath,maskedPath2) = args
//    val maskedPath = "D:\\gdal\\geotrellis-landsat-tutorial-master\\Tif_band\\LAN_046029_NDVI.png"
//    val maskedPath2 = "D:\\gdal\\geotrellis-landsat-tutorial-master\\Tif_band\\LAN_046029_NDVI.tif"
    val rGeoTiff = SinglebandGeoTiff(bandPath("R"))
    val nirGeoTiff = SinglebandGeoTiff(bandPath("NIR"))
    val mb = ArrayMultibandTile(rGeoTiff.tile,nirGeoTiff.tile).convert(DoubleConstantNoDataCellType)
    val tile = MultibandGeoTiff(mb, rGeoTiff.extent, rGeoTiff.crs).tile.convert(DoubleConstantNoDataCellType)

    val ndvi_res =tile.combineDouble(0,1){(r: Double, ir: Double) => calculate_ndvi(r, ir)}
    val ndviTiff = MultibandTile(ndvi_res)
    val colorMap = ColorMap.fromStringDouble("0:ffffe5ff;0.1:f7fcb9ff;0.2:d9f0a3ff;0.3:addd8eff;0.4:78c679ff;0.5:41ab5dff;0.6:238443ff;0.7:006837ff;1:004529ff").get
    //生成png图片
    ndvi_res.renderPng(colorMap).write(maskedPath)
    //生成tif
    SinglebandGeoTiff(ndvi_res,rGeoTiff.extent, rGeoTiff.crs).write(maskedPath2)
  }
}
