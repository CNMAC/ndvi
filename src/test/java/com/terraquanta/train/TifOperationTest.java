package com.terraquanta.train;

import com.terraquanta.train.entity.ReadResult;
import com.terraquanta.train.util.TifOperation;
import org.junit.Assert;
import org.junit.Test;

public class TifOperationTest {
    @Test
    public void ReadTifToArrayTest(){
        ReadResult tifRes = TifOperation.ReadTifToArray("D:\\下载\\Tif_band\\LAN_046029_NIR.tif");
        Assert.assertNotNull(tifRes);
    }

    //......
}
