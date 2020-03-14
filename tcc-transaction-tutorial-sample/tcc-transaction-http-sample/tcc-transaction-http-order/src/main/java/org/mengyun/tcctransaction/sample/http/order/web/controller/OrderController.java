package org.mengyun.tcctransaction.sample.http.order.web.controller;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.mengyun.tcctransaction.sample.http.order.domain.entity.Product;
import org.mengyun.tcctransaction.sample.http.order.domain.repository.ProductRepository;
import org.mengyun.tcctransaction.sample.http.order.domain.service.AccountServiceImpl;
import org.mengyun.tcctransaction.sample.http.order.domain.service.OrderServiceImpl;
import org.mengyun.tcctransaction.sample.http.order.service.PlaceOrderServiceImpl;
import org.mengyun.tcctransaction.sample.http.order.web.controller.vo.PlaceOrderRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.List;

/**
 * Created by changming.xie on 4/1/16.
 */
@Controller
@RequestMapping("")
public class OrderController {

    @Autowired
    PlaceOrderServiceImpl placeOrderService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    AccountServiceImpl accountService;

    @Autowired
    OrderServiceImpl orderService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView index() {
        ModelAndView mv = new ModelAndView("/index");
        return mv;
    }

    //显示商店中的所有商品
    @RequestMapping(value = "/user/{userId}/shop/{shopId}", method = RequestMethod.GET)
    public ModelAndView getProductsInShop(@PathVariable long userId,
                                          @PathVariable long shopId) {
        List<Product> products = productRepository.findByShopId(shopId);

        ModelAndView mv = new ModelAndView("/shop");

        mv.addObject("products", products);
        mv.addObject("userId", userId);
        mv.addObject("shopId", shopId);

        return mv;
    }

    //显示购买的商品细节，和自己的账户资金细节
    @RequestMapping(value = "/user/{userId}/shop/{shopId}/product/{productId}/confirm", method = RequestMethod.GET)
    public ModelAndView productDetail(@PathVariable long userId,
                                      @PathVariable long shopId,
                                      @PathVariable long productId) {

        ModelAndView mv = new ModelAndView("product_detail");

        mv.addObject("capitalAmount", accountService.getCapitalAccountByUserId(userId));
        mv.addObject("redPacketAmount", accountService.getRedPacketAccountByUserId(userId));

        mv.addObject("product", productRepository.findById(productId));

        mv.addObject("userId", userId);
        mv.addObject("shopId", shopId);

        return mv;
    }
    // 下单的主流程
    @RequestMapping(value = "/placeorder", method = RequestMethod.POST)
    public ModelAndView placeOrder(@RequestParam String redPacketPayAmount,
                                   @RequestParam long shopId,
                                   @RequestParam long payerUserId,
                                   @RequestParam long productId) {
        PlaceOrderRequest request = buildRequest(redPacketPayAmount, shopId, payerUserId, productId);
        // 1. 下单并支付订单
        String merchantOrderNo = placeOrderService.placeOrder(request.getPayerUserId(), request.getShopId(),
                request.getProductQuantities(), request.getRedPacketPayAmount());
        // 2. 返回下单结果
        ModelAndView mv = new ModelAndView("pay_success");
        // 3.  查询订单状态
        String status = orderService.getOrderStatusByMerchantOrderNo(merchantOrderNo);
        String payResultTip = null;
        if ("CONFIRMED".equals(status)) {
            payResultTip = "支付成功";
        } else if ("PAY_FAILED".equals(status)) {
            payResultTip = "支付失败";
        }
        //  4. 构建显示细节
        mv.addObject("payResult", payResultTip);
        mv.addObject("product", productRepository.findById(productId));
        mv.addObject("capitalAmount", accountService.getCapitalAccountByUserId(payerUserId));
        mv.addObject("redPacketAmount", accountService.getRedPacketAccountByUserId(payerUserId));
        return mv;
    }

    // 构建下单请求
    private PlaceOrderRequest buildRequest(String redPacketPayAmount, long shopId, long payerUserId, long productId) {
        BigDecimal redPacketPayAmountInBigDecimal = new BigDecimal(redPacketPayAmount);
        if (redPacketPayAmountInBigDecimal.compareTo(BigDecimal.ZERO) < 0)
            throw new InvalidParameterException("invalid red packet amount :" + redPacketPayAmount);

        PlaceOrderRequest request = new PlaceOrderRequest();

        request.setPayerUserId(payerUserId);

        request.setShopId(shopId);

        request.setRedPacketPayAmount(new BigDecimal(redPacketPayAmount));

        request.getProductQuantities().add(new ImmutablePair<Long, Integer>(productId, 1));

        return request;
    }
}
