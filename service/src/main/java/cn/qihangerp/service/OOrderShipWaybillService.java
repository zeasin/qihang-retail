package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrderShipWaybill;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author qilip
* @description 针对表【o_ship_waybill(发货电子面单记录表（打单记录）)】的数据库操作Service
* @createDate 2024-07-28 18:29:53
*/
public interface OOrderShipWaybillService extends IService<OOrderShipWaybill> {
     PageResult<OOrderShipWaybill> queryPageTodayList(Long merchantId, PageQuery pageQuery);

     /**
      * 保存&更新电子面单取号信息
      * @param shipWaybill
      * @return
      */
     ResultVo<Integer> waybillUpdate(OOrderShipWaybill shipWaybill);
     OOrderShipWaybill getShipWaybillByOrderId(String orderId);
     List<OOrderShipWaybill> getListByOrderIds(Long shopId, String[] orderIds);
     List<OOrderShipWaybill> getListBySupplierShipOrderIds(String[] orderIds);
     ResultVo<Integer> printSuccess(Long shopId,String[] orderIds);
     ResultVo<Integer> supplierPrintSuccess(String supplierShipOrderId);

     /**
      * 电子面单发货并加入备货单(本地发货)
      * @param shopId
      * @param orderIds
      * @return
      */
     ResultVo<Integer> localShipOrderWaybillSendAndStocking(Long shopId, String[] orderIds);

     /**
      * 本地发货消息通知处理（加入备货单）
      * @param orderSn
      * @return
      */
     ResultVo<Long> localWaybillShipMessage(String orderSn);



//     ResultVo<Integer> pushSupplierShipSend(String supplierShipOrderId);
     ResultVo<OOrderShipWaybill> cancelWaybillCode(Long supplierShipOrderId, String orderNum);
     /**
      * 保存取号记录
      * @param
      * @param templateUrl
      * @param
      * @return
      */
     ResultVo<String> waybillCodeSave(String shipOrderId, String waybillOrderId, String waybillCode, String logisticsCode, String printData, String sign, String params, String templateUrl);

     /**
      * 自己取号发货并保存电子面单数据
      * @param orderNum
      * @param waybillCode
      * @param printData
      * @return
      */
     ResultVo<OOrderShipWaybill> ownerGetWaybillCodeAndSave(String orderNum, String waybillCode, String logisticsCode, String printData,String sign,String params,String templateUrl);

     /**
      * 取消取号
      * @param orderId oOrder表id
      * @return
      */
     ResultVo cancelWaybillCode(Long orderId);
}
