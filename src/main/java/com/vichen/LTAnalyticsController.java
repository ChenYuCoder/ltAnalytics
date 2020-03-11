package com.vichen;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/bb/")
public class LTAnalyticsController {
    @Autowired
    LTAnalyticsConfig bbConfig;

    Logger logger = LoggerFactory.getLogger(LTAnalyticsController.class);

    @GetMapping("a")
    public String analytics(String path) {
        JSONArray data;
        try {
            data = read(path);
        } catch (Exception e) {
            return e.getMessage();
        }

        return analyticsData(data).toJSONString();
    }

    private JSONObject analyticsData(JSONArray data) {
        List<Object> dataList = data.stream().filter((item) -> {
            JSONObject jsonItem = (JSONObject) item;
            if (!"新增".equals(jsonItem.getString("是否续订"))) {
                return false;
            }
            if (!"高清".equals(jsonItem.getString("产品类型"))) {
                return false;
            }
            if (!bbConfig.getAnalyticsProvince().contains(jsonItem.getString("省分"))) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());

        JSONObject result = new JSONObject();
        for (Object item :
                dataList) {
            JSONObject jsonItem = (JSONObject) item;
            String province = jsonItem.getString("省分");
            JSONObject provinceResult;
            if (result.containsKey(province)) {
                provinceResult = result.getJSONObject(province);
            } else {
                provinceResult = new JSONObject();
                result.put(province, provinceResult);
            }

            provinceResult.put("总订购", provinceResult.getIntValue("总订购") + 1);

            if ("内蒙华为".equals(province)) {
                if (bbConfig.getHuaweiInsideRecommendList().contains(jsonItem.getString("产品ID"))) {
                    provinceResult.put("内部订购", provinceResult.getIntValue("内部订购") + 1);
                } else {
                    provinceResult.put("活动页", provinceResult.getIntValue("活动页") + 1);
                }
            }


            String appId = jsonItem.getString("APP_ID");
            if (bbConfig.getRecommendIdList().contains(appId)) {
                JSONObject appIdResult = provinceResult.getJSONObject("appId");
                if (appIdResult == null) {
                    appIdResult = new JSONObject();
                    provinceResult.put("appId", appIdResult);
                }
                appIdResult.put(appId, appIdResult.getIntValue(appId) + 1);
            }

            String triggerValue = jsonItem.getString("触发项");
            if (bbConfig.getTriggerList().contains(triggerValue)) {
                provinceResult.put(triggerValue, provinceResult.getIntValue(triggerValue) + 1);
            }

            if (appId != null && appId.startsWith(bbConfig.getConventionIdPrefix())) {
//                provinceResult.put("常规内容", provinceResult.getIntValue("常规内容") + 1);

                if (StringUtils.isEmpty(jsonItem.getString("触发项"))) {
                    provinceResult.put("常规内容触发项空", provinceResult.getIntValue("常规内容触发项空") + 1);
                }

                String channel = jsonItem.getString("频道");

                if (channel == null || channel.equals("0.0")) {
                    JSONObject triggerResult = provinceResult.getJSONObject("空频道");

                    if (triggerResult == null) {
                        triggerResult = new JSONObject();
                        provinceResult.put("空频道", triggerResult);
                    }
                    triggerResult.put(triggerValue, triggerResult.getIntValue(triggerValue) + 1);
                } else {
                    provinceResult.put(channel, provinceResult.getIntValue(channel) + 1);
                }

            }


        }

        return result;
    }

    /**
     * @param path
     * @return
     * @throws Exception
     */

    private JSONArray read(String path) throws Exception {
        JSONArray data = new JSONArray();
        try (InputStream fis = new FileInputStream(path)) {
            Workbook workbook = null;
            if (path.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (path.endsWith(".xls") || path.endsWith(".et")) {
                workbook = new HSSFWorkbook(fis);
            }
            if (workbook == null) {
                throw new RuntimeException("exl文件存在问题，文件路径为：" + path);
            }
            //创建二维数组,储存excel行列数据

            //遍历工作簿中的sheet
            Sheet sheet = workbook.getSheetAt(0);
            //当前sheet页面为空,继续遍历
            if (sheet == null) {
                throw new RuntimeException("exl不存在标签页");
            }
            // 对于每个sheet，读取其中的每一行

            Row headerRow = sheet.getRow(0);
            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) {
                    continue;
                }
                JSONObject rolObject = new JSONObject();
                // 遍历每一行的每一列
                for (int columnNum = 0; columnNum < headerRow.getLastCellNum(); columnNum++) {
                    Cell cell = row.getCell(columnNum);

                    Object value;
                    if (cell == null) {
                        value = null;
                    } else if (!CellType.STRING.equals(cell.getCellType())) {
                        value = cell.getNumericCellValue();
                    } else {
                        value = cell.getStringCellValue();
                    }
                    rolObject.put(headerRow.getCell(columnNum).getStringCellValue(), value);
                }
                data.add(rolObject);
            }

        } catch (FileNotFoundException e) {
            logger.error("has error", e);
            throw new RuntimeException("文件路径存在问题，文件路径为：" + path);
        } catch (Exception e1) {
            logger.error("has error", e1);
            throw new RuntimeException("内部发生异常：" + path);
        }
        return data;
    }
}
