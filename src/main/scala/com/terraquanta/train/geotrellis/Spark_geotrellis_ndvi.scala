package com.terraquanta.train.geotrellis

import geotrellis.raster.{ArrayMultibandTile, DoubleConstantNoDataCellType, MultibandTile, Raster, Tile}
import geotrellis.raster.io.geotiff.{GeoTiff, MultibandGeoTiff, SinglebandGeoTiff}
import geotrellis.raster.resample.Bilinear
import geotrellis.spark.{ContextRDD, MultibandTileLayerRDD, SpatialKey, TileLayerMetadata}
import geotrellis.spark.tiling.{FloatingLayoutScheme, ZoomedLayoutScheme}
import geotrellis.vector.ProjectedExtent
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD
import geotrellis.spark._

object Spark_geotrellis_ndvi {
  //val localCatalogPath = new java.io.File(new java.io.File(".").getCanonicalFile, "land-cover-data/catalog").getAbsolutePath

  def bandPath(b: String) = s"D:\\gdal\\geotrellis-landsat-tutorial-master\\Tif_band\\LAN_046029_${b}.tif"

  def main(args: Array[String]): Unit = {
    //读取两个tif文件,数据放到同一栅格
    val maskedPath = ""
    val rGeoTiff = SinglebandGeoTiff(bandPath("R"))
    val nirGeoTiff = SinglebandGeoTiff(bandPath("NIR"))
    val mb = ArrayMultibandTile(rGeoTiff.tile,nirGeoTiff.tile).convert(DoubleConstantNoDataCellType)
    val Tile_all = MultibandGeoTiff(mb, rGeoTiff.extent, rGeoTiff.crs).tile
    //spark读取为rdd
    val conf = new SparkConf().setMaster("local[5]").setAppName("geotrellis on spark calc ndvi")
    val sc = new SparkContext(conf)
    val rdd_band2: RDD[(ProjectedExtent, MultibandTile)] = sc.parallelize(List((ProjectedExtent(rGeoTiff.raster.extent, rGeoTiff.crs),Tile_all)))
    val (_, rasterMetaData) =
      TileLayerMetadata.fromRdd(rdd_band2, FloatingLayoutScheme(512))

    val tiled: RDD[(SpatialKey, MultibandTile)] =
      rdd_band2
        .tileToLayout(rasterMetaData.cellType, rasterMetaData.layout, Bilinear)
        .repartition(100)
    val rdd = MultibandTileLayerRDD(tiled, rasterMetaData)

    //计算ndvi
    val raster: Raster[Tile] =
      rdd
        .withContext { rdd =>
          rdd
            .mapValues { tile =>
              tile.convert(DoubleConstantNoDataCellType).combine(0, 1) { (r, nir) =>{if(r+nir==0.0) 0 else (nir - r) / (nir + r)}
              }
            }
            .reduceByKey(_.localMax(_))
        }
        .stitch
    //存文件
    GeoTiff(raster, rdd.metadata.crs).write("D:\\gdal\\geotrellis-landsat-tutorial-master\\Tif_band\\LAN_046029_NDVI.tif")
  }

}
