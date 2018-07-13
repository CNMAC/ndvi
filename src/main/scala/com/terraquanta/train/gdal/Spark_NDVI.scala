package com.terraquanta.train.gdal

import com.terraquanta.train.util.TifOperation
import org.apache.spark.{SparkConf, SparkContext}

object Spark_NDVI {

  def calc_ndvi(a:String, b:String): Float ={
    if (a(0)=="1"){
      var nir = a.slice(1,a.length).toFloat
      var red = b.slice(1,b.length).toFloat
      (nir - red) / (nir + red)
    }else{
      var red = a.slice(1,a.length).toFloat
      var nir = b.slice(1,b.length).toFloat
      (nir - red) / (nir + red)
    }
  }

  def main(args: Array[String]): Unit = {

    if(args.length !=3){
      System.err.println("Usage: Spark_NDVI <NIRTifPath> <RTifPath> <OutFilePath> ")
      System.exit(1)
    }
    val conf = new SparkConf().setMaster("local[5]").setAppName("spark tif ndvi")
    val sc = new SparkContext(conf)
    //读取NIR.tif文件生成计算需要的数据
    val Array(nir_path,r_path,out_path) = args
    //"D:\\下载\\Tif_band\\LAN_046029_NIR.tif"
    //"D:\\下载\\Tif_band\\LAN_046029_R.tif"
    val tif_nir = TifOperation.ReadTifToArray(nir_path)
    val nir_width = tif_nir.getWidth
    val nir_height = tif_nir.getHeight
    val nir_values = tif_nir.getTifValus
    //读取R.tif文件生成计算需要的数据
    val tif_red = TifOperation.ReadTifToArray(r_path)
    val red_width = tif_red.getWidth
    val red_height = tif_red.getHeight
    val red_values = tif_red.getTifValus
    //判断是否存在异常
    if (nir_width != red_width && nir_height != red_height){
      System.err.println("ERROR: The size of the Tif images is different!")
      System.exit(1)
    }
    //分布式计算准备，如下计算在driver容器内执行，适量调大driver资源
    val nir_mark = for(x <- 0 to nir_values.length - 1 ) yield (x,"1"+nir_values(x).toString)
    val red_mark = for(x <- 0 to red_values.length - 1 ) yield (x,"2"+red_values(x).toString)
    val nir_rdd = sc.parallelize(nir_mark)
    val red_rdd = sc.parallelize(red_mark)

    //进行计算NDVI,生成结果数据
    val rdd_merge = nir_rdd.union(red_rdd).repartition(50).reduceByKey({case (x, y) => calc_ndvi(x,y).toString}).sortByKey().collect()
    val result_arr = for(x <- rdd_merge) yield x._2.toFloat

    //数据写入tif文件
    TifOperation.SaveResultInTif(out_path,nir_width,nir_height,result_arr)
    println("任务完成！")
  }

}
