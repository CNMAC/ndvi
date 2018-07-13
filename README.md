# ndvi

### 实现了三种方式计算ndvi

1. 基于java gdal读取tif文件，使用spark rdd进行运算
```
Spark_NDVI.scala
```
2. 使用geotrellis框架单机处理
```
Geotrellis_simple_ndvi.scala
```
3. 使用geotrellis框架在Spark上处理
```
Spark_geotrellis_ndvi.scala
```
