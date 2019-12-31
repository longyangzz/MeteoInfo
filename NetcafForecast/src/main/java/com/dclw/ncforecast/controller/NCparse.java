package com.dclw.ncforecast.controller;

import com.dclw.ncforecast.biz.ReadGribToSqlBIZ;
import com.dclw.ncforecast.common.Core;
import javafx.geometry.Point2D;
import org.meteoinfo.data.GridData;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.python.apache.xerces.xs.StringList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("NCparse")
public class NCparse {
    @Autowired
    private ReadGribToSqlBIZ readGribToSqlBIZ;

    @Value("${custom.forecastData}")
    private String forecastData;

    @Value("${custom.type}")
    private String diguiTypeValue;

    @Value("${custom.xyfblstep}")
    private int xyfblstep;

    @Value("${custom.data-featrue}")
    private String dataFeatrue;

    @Value("${custom.bound}")
    private String customBound;
    private List<Point2D> boundArr ;

    @Value("${custom.points}")
    private String customPoints;
    private List<Point2D> customPointsArr ;


    @RequestMapping("readGribToSql")
    public @ResponseBody String readGribToSql(){
        //！ 目录结构示意
        //！ ForecastData
        //！     20180707
        //！         ShortTerm
        //！             Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2
        //！             Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2
        //！         240Term
        //！     20180708
        //！         ShortTerm
        //! 遍历指定目录下的所有文件夹
        //! 然后对文件夹下的72小时的指定要素数据进行读取写出
        //! 获取下边的目录
        String log = "readGribToSql start" + "\r\n";


        List<File> files = Arrays.asList(new File( forecastData ).listFiles() );
        List arrfilesList = new ArrayList(files);
        Collections.sort(arrfilesList, new Comparator<File>(){
            @Override
            public int compare(File o1, File o2) {
                if(o1.isDirectory() && o2.isFile())
                    return -1;
                if(o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });


        List<String> subPathList = new ArrayList<>();
        if(arrfilesList == null)
        {
            log += "根目录下不存在子目录" + "\r\n";
            return log;
        }

        //!! 判断解析类型，是实时还是历史
        String isDigui = diguiTypeValue;
        int subfolderNum = 0;
        if(isDigui.equals("history"))
        {

        }else if(isDigui.equals("realtime"))
        {
            //！ 自定义文件夹目录
            arrfilesList.clear();
            arrfilesList.add(new File(forecastData + "\\" + Core.getTimeStr2()));
        }

        subfolderNum = arrfilesList.size();
        for (int i = 0; i != subfolderNum; ++i  ) {
            File file = (File)arrfilesList.get(i);
            if ( file.isDirectory() ) {
                subPathList.add( file.getPath() + "\\ShortTerm\\");
            }
        }

        //! 对subPathList目录下的ShortTerm目录下的所有文件进行解析入库处理
        if(subPathList.size() == 0)
        {
            log += "子目录为空" + "\r\n";
        }

        //! 解析为boundArr赋值
        int boundLength = customBound.split(";").length;
        boundArr = new ArrayList<Point2D>();
        for(int i = 0; i < boundLength; ++i)
        {
            String [] curPos = customBound.split(";")[i].split(",");
            if(curPos.length != 2)
            {
                continue;
            }
            boundArr.add(new Point2D(Double.parseDouble(curPos[0]), Double.parseDouble(curPos[1])) );
        }

        //！ 解析获取points序列
        int pointsLength = customPoints.split(";").length;
        customPointsArr = new ArrayList<Point2D>();
        for(int i = 0; i < pointsLength; ++i)
        {
            String [] curPos = customPoints.split(";")[i].split(",");
            if(curPos.length != 2)
            {
                continue;
            }
            customPointsArr.add(new Point2D(Double.parseDouble(curPos[0]), Double.parseDouble(curPos[1])) );
        }

        //! 遍历subPathList下的所有grib文件
        //！ 按bound解析还是按points解析
        for ( String gribFolder : subPathList ) {
            File[] gribFiles = new File( gribFolder ).listFiles();

            for ( File gribFile : gribFiles ) {
                if ( !gribFile.isDirectory() ) {
                    log += "解析文件开始" + gribFile.getPath()  + "\r\n";
                    List<String> cunRecoords;
                    //! 指定点坐标为空，则按bound解析
                    if(customPoints.isEmpty() ){
                        cunRecoords = OpenGribToSqlByBound(gribFile.getPath());
                    }else {
                        cunRecoords = OpenGribToSqlByPoints(gribFile.getPath());
                    }


                    if(cunRecoords.size() > 0){
                        log += "解析到有效数据在文件" + gribFile.getPath()  + "\r\n";
                    }
                    else
                    {
                        log +=  gribFile.getPath() + "中没有解析到有效数据" + "\r\n";
                    }

                    //！ 记录超过1000条分批插入
                    for ( String cunRecoord : cunRecoords ){
                        if(cunRecoord.equals("") || cunRecoord.isEmpty())
                        {
                            continue;
                        }

                        if( gribFile.getPath().contains("EDA10") && dataFeatrue.contains("EDA10"))
                        {
                            Integer lt = readGribToSqlBIZ.inserGribDataToSql(cunRecoord);
                        }

                        if(gribFile.getPath().contains("WWD") && dataFeatrue.contains("WWD"))
                        {
                            Integer lt = readGribToSqlBIZ.inserWWDGribDataToSql(cunRecoord);
                        }

                        log +=  gribFile.getPath() + "中的数据插入成功" + "\r\n";
                    }

                }
            }

        }


        return log;
    }

    //根据指定的文件名，以及bound值读取并判断类型，解析到数据库中
    public List<String> OpenGribToSqlByBound(String fileName)
    {
        List<String> srList = new ArrayList<String >();
        String stateValues = new String();


        String baseName = new File(fileName).getName();
        String [] nameList = baseName.split("_");
        //！ 从文件名称解析生成三个时间
        SimpleDateFormat dateInFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dateInFormat1 = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat dateOutFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        /**
        * 字符串转化为时间
        */
        Date date1=null;
        try {
            String genTime = nameList[4];
            date1=dateInFormat.parse(genTime);
            genTime = dateOutFormat.format(date1);

            String startForecastTime = nameList[8];
            date1=dateInFormat1.parse(startForecastTime);
            startForecastTime = dateOutFormat.format(date1);

            int addHour = Integer.parseInt(nameList[9].substring(0,3));
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(date1);
            rightNow.add(Calendar.HOUR,addHour);
            date1 = rightNow.getTime();
            String forecastTime = dateOutFormat.format(date1);

            // ！ EDA10判断数据文件类型
            if(fileName.endsWith("GRB2") && fileName.contains("EDA10") && dataFeatrue.contains("EDA10"))
            {
                //String path = forecastData + "\\20180707\\ShortTerm\\Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2";
                String path = fileName;
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(path);

                String uName = "u-component_of_wind_height_above_ground";
                GridData gridDataU = aDataInfo.getGridData(uName);

                String vName = "v-component_of_wind_height_above_ground";
                GridData gridDataV = aDataInfo.getGridData(vName);

                if(gridDataU.equals(null) || gridDataV.equals(null))
                {
                    aDataInfo.close();
                    return  srList;
                }

                //! 最后一条记录和其它记录有末尾是否加逗号的区别
                String fs = "";

                int xNum = gridDataV.xArray.length;//gridDataV.xArray.length;
                int yNUm = gridDataV.yArray.length;
                int tNum = (xNum / (xyfblstep+1) + 1 ) * (yNUm / (xyfblstep+1) + 1);

                // 遍历维度层
                int count = 0;
                for(int row = 0; row < yNUm; row += xyfblstep)
                {
                    for(int i = 0; i < xNum; i += xyfblstep)
                    {
                        //! 经纬度索引号按行拍
                        float lon = (float)( gridDataV.xArray[i]);
                        float lat = (float)( gridDataV.yArray[row]);
//                        lon = (float)(Math.round(lon*100))/100;
//                        lat = (float)(Math.round(lat*100))/100;

                        //! 判断点是否在删选范围内，不在则continue
                        Point2D point = new Point2D(lon, lat);
                        boolean isIN = Core.IsPtInPoly(point, boundArr);
                        if (!isIN) {
                            continue;
                        }

                        int index = xNum * row + i;
                        float uValue = (float)( gridDataU.data[row][i]);
                        float vValue = (float)( gridDataV.data[row][i]);

                        if (count == tNum - 1 || ((count+1)%1000 == 0) ) {
                            fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s)", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                            stateValues += fs;
                            srList.add(stateValues);

                            //! 清空
                            stateValues = "";
                        } else {
                            fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s),", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                            stateValues += fs;
                        }

                        count++;
                    }
                }
                aDataInfo.close();
            }

            if(fileName.endsWith("GRB2") && fileName.contains("WWD") && dataFeatrue.contains("WWD"))
            {
                //String path = forecastData + "\\20180707\\ShortTerm\\Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2";
                String path = fileName;
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(path);

                String uName = "u-component_of_wind_altitude_above_msl";
                GridData gridDataU = aDataInfo.getGridData(uName);

                if(gridDataU.equals(null))
                {
                    aDataInfo.close();
                    return  srList;
                }

                //! 最后一条记录和其它记录有末尾是否加逗号的区别
                String fs = "";

                int xNum = gridDataU.xArray.length;//gridDataV.xArray.length;
                int yNUm = gridDataU.yArray.length;
                int tNum = (xNum / (xyfblstep+1) + 1 ) * (yNUm / (xyfblstep+1) + 1);

                // 遍历维度层
                int count = 0;
                for(int row = 0; row < yNUm; row += xyfblstep)
                {
                    for(int i = 0; i < xNum; i += xyfblstep)
                    {
                        //! 经纬度索引号按行拍
                        float lon = (float)( gridDataU.xArray[i]);
                        float lat = (float)( gridDataU.yArray[row]);
//                        lon = (float)(Math.round(lon*100))/100;
//                        lat = (float)(Math.round(lat*100))/100;
                        //! 判断点是否在删选范围内，不在则continue
                        Point2D point = new Point2D(lon, lat);
                        boolean isIN = Core.IsPtInPoly(point, boundArr);
                        if (!isIN) {
                            continue;
                        }

                        int index = xNum * row + i;
                        float uValue = (float)( gridDataU.data[row][i]);

                        if (count == tNum - 1 || ((count+1)%1000 == 0) ) {
                            fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s)", genTime, startForecastTime, forecastTime, uValue, 0.0, lon, lat, Integer.toString(index));
                            stateValues += fs;
                            srList.add(stateValues);

                            //! 清空
                            stateValues = "";
                        } else {
                            fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s),", genTime, startForecastTime, forecastTime, uValue, 0.0, lon, lat, Integer.toString(index));
                            stateValues += fs;
                        }

                        count++;
                    }
                }
                aDataInfo.close();
            }


            return  srList;

        } catch (ParseException e) {
            return  srList;
        }

    }

    //根据指定的文件名，以及bound值读取并判断类型，解析到数据库中
    public List<String> OpenGribToSqlByPoints(String fileName)
    {
        List<String> srList = new ArrayList<String >();
        String stateValues = new String();


        String baseName = new File(fileName).getName();
        String [] nameList = baseName.split("_");
        //！ 从文件名称解析生成三个时间
        SimpleDateFormat dateInFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat dateInFormat1 = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat dateOutFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        /**
         * 字符串转化为时间
         */
        Date date1=null;
        try {
            String genTime = nameList[4];
            date1=dateInFormat.parse(genTime);
            genTime = dateOutFormat.format(date1);

            String startForecastTime = nameList[8];
            date1=dateInFormat1.parse(startForecastTime);
            startForecastTime = dateOutFormat.format(date1);

            int addHour = Integer.parseInt(nameList[9].substring(0,3));
            Calendar rightNow = Calendar.getInstance();
            rightNow.setTime(date1);
            rightNow.add(Calendar.HOUR,addHour);
            date1 = rightNow.getTime();
            String forecastTime = dateOutFormat.format(date1);

            // ！ EDA10判断数据文件类型
            if(fileName.endsWith("GRB2") && fileName.contains("EDA10") && dataFeatrue.contains("EDA10"))
            {
                //String path = forecastData + "\\20180707\\ShortTerm\\Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2";
                String path = fileName;
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(path);

                String uName = "u-component_of_wind_height_above_ground";
                GridData gridDataU = aDataInfo.getGridData(uName);

                String vName = "v-component_of_wind_height_above_ground";
                GridData gridDataV = aDataInfo.getGridData(vName);

                if(gridDataU.equals(null) || gridDataV.equals(null))
                {
                    aDataInfo.close();
                    return  srList;
                }

                //! 最后一条记录和其它记录有末尾是否加逗号的区别
                String fs = "";

                int xNum = gridDataV.xArray.length;//gridDataV.xArray.length;
                int yNUm = gridDataV.yArray.length;
                int tNum = (xNum / (xyfblstep+1) + 1 ) * (yNUm / (xyfblstep+1) + 1);

                int count = 0;
                //！ 遍历点个数，根据点坐标，获取行列号，从行列号中取出相应数值写出，超过1000个则
                //! 重新组装
                int ptNums = customPointsArr.size();
                for(int pi = 0; pi < ptNums; ++pi) {
                    //! 经纬度索引号按行拍
                    float lon = (float)customPointsArr.get(pi).getX();
                    float lat = (float)customPointsArr.get(pi).getY();

                    //! 获取行列号值
                    int row = (int) ((lon - gridDataU.xArray[0]) / gridDataU.getXDelt());
                    int col = (int) ((lat - gridDataU.yArray[0]) / gridDataU.getYDelt());

                    int index = xNum * row + col;
                    float uValue = (float)( gridDataU.toStation(lon, lat));
                    float vValue = (float)( gridDataV.toStation(lon, lat));

                    if (count == ptNums - 1 || ((count+1)%1000 == 0) ) {
                        fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s)", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                        stateValues += fs;
                        srList.add(stateValues);

                        //! 清空
                        stateValues = "";
                    } else {
                        fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s),", genTime, startForecastTime, forecastTime, uValue, vValue, lon, lat, Integer.toString(index));
                        stateValues += fs;
                    }

                    count++;
                }
                aDataInfo.close();
            }

            if(fileName.endsWith("GRB2") && fileName.contains("WWD") && dataFeatrue.contains("WWD"))
            {
                //String path = forecastData + "\\20180707\\ShortTerm\\Z_NWGD_C_BEHK_20180707060054_P_RFFC_SPCC-EDA10_201807070800_00301.GRB2";
                String path = fileName;
                MeteoDataInfo aDataInfo = new MeteoDataInfo();
                aDataInfo.openNetCDFData(path);

                String uName = "u-component_of_wind_altitude_above_msl";
                GridData gridDataU = aDataInfo.getGridData(uName);

                if(gridDataU.equals(null))
                {
                    aDataInfo.close();
                    return  srList;
                }

                //! 最后一条记录和其它记录有末尾是否加逗号的区别
                String fs = "";

                int xNum = gridDataU.xArray.length;//gridDataV.xArray.length;
                int yNUm = gridDataU.yArray.length;
                int tNum = (xNum / (xyfblstep+1) + 1 ) * (yNUm / (xyfblstep+1) + 1);

                // 遍历维度层
                int count = 0;
                //！ 遍历点个数，根据点坐标，获取行列号，从行列号中取出相应数值写出，超过1000个则
                //! 重新组装
                int ptNums = customPointsArr.size();
                for(int pi = 0; pi < ptNums; ++pi) {
                    //! 经纬度索引号按行拍
                    float lon = (float)customPointsArr.get(pi).getX();
                    float lat = (float)customPointsArr.get(pi).getY();

                    //! 获取行列号值
                    int row = (int) ((lon - gridDataU.xArray[0]) / gridDataU.getXDelt());
                    int col = (int) ((lat - gridDataU.yArray[0]) / gridDataU.getYDelt());

                    int index = xNum * row + col;
                    float uValue = (float)( gridDataU.toStation(lon, lat));

                    if (count == ptNums - 1 || ((count+1)%1000 == 0) ) {
                        fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s)", genTime, startForecastTime, forecastTime, uValue, 0.0, lon, lat, Integer.toString(index));
                        stateValues += fs;
                        srList.add(stateValues);

                        //! 清空
                        stateValues = "";
                    } else {
                        fs = String.format("('%s', '%s', '%s', %.2f, %.2f, %.2f, %.2f, %s),", genTime, startForecastTime, forecastTime, uValue, 0.0, lon, lat, Integer.toString(index));
                        stateValues += fs;
                    }

                    count++;
                }
                aDataInfo.close();
            }


            return  srList;

        } catch (ParseException e) {
            return  srList;
        }

    }

}
