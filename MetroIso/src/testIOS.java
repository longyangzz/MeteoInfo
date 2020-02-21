import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.mapdata.MapDataManage;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.geoprocess.analysis.InterpolationMethods;
import org.meteoinfo.geoprocess.analysis.InterpolationSetting;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.legend.LegendType;
import org.meteoinfo.map.MapView;
import org.meteoinfo.shape.ShapeTypes;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.awt.*;
import java.util.Random;

public class testIOS {
    public static void main(String [] args){
        System.out.println("Hello world");

        MapView mapView = new MapView();
        mapView.setBackground(new Color(255, 255, 255, 0));
        mapView.setBounds(0, 0, 200, 200);

        try{
            VectorLayer clipLayer = MapDataManage.readMapFile_ShapeFile("D:\\4555\\beijing.shp"); //剪切图层，就是生成等值面的形状范围，
            //使用shp最方便了，这儿有一个坑，就是你是用的shp可以通过他自带的软件加载显示才行，否则程序会异常，至于为啥有的shp不能加载，我也没搞清楚
            //反正这个坑让我趟过去了。

            //! 模拟站点数据
            StationData stationData = new StationData();

            Random rand = new Random();

            for(int i=0;i<100;i++)
            {
                double value = rand.nextInt(50) + 1;
                stationData.addData("st"+i, 110+0.1*i, 33+0.1*i, value); //站点名称，经度，维度，值
            }

            //！ 设置插值参数
            GridDataSetting gridDataSetting = new GridDataSetting();
            gridDataSetting.dataExtent = clipLayer.getExtent();
            stationData.projInfo = clipLayer.getProjInfo();
            gridDataSetting.xNum = 1000;// 格点点数
            gridDataSetting.yNum = 1000;// 格点点数

            InterpolationSetting interSet = new InterpolationSetting();

            interSet.setGridDataSetting(gridDataSetting);
            interSet.setInterpolationMethod(InterpolationMethods.IDW_Radius);
            interSet.setRadius(5);
            interSet.setMinPointNum(1);
            GridData gridData = stationData.interpolateData(interSet);

            LegendScheme legendScheme = LegendManage.createLegendSchemeFromGridData(gridData, LegendType.UniqueValue, ShapeTypes.Polygon);


            //！ 插值生成图层
            VectorLayer contourLayer = DrawMeteoData.createShadedLayer(gridData, legendScheme, "ContourLayer", "Data", true);
            VectorLayer lastLayer = contourLayer.clip(clipLayer);
            mapView.addLayer(lastLayer);

            //！ 导出为图片
            mapView.exportToPicture("D:\\4555\\ddd.png"); //地图导出为图片
            //地图导出为kml
            lastLayer.saveAsKMLFile("D:\\4555\\ddd.kml");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
