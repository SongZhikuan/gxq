package com.hengyunsoft.platform.developer.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.hengyunsoft.platform.developer.BaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * @BeforeClass 全局只会执行一次，而且是第一个运行
 * @Before 在测试方法运行之前运行
 * @Test 测试方法
 * @After 在测试方法运行之后允许
 * @AfterClass 全局只会执行一次，而且是最后一个运行
 * @Ignore 忽略此方法
 * <p>
 * MockMvc讲解
 * https://www.cnblogs.com/xd03122049/p/6001457.html
 */
public class ModuleUseJnlImplTest extends BaseTest {

    @Before
    public void before() throws Exception {
        /*
         * 测试方法 6部曲
         * 1，针对每一个具体Controller层类，创建一个测试类，继承BaseTest
         * 2，对Controller层中每个public方法，增加一个测试方法（testXxx）
         * 3，构造模拟uri，并且 配置好参数，请求头等信息
         * 4，模拟执行请求
         * 5，对执行结果进行断言
         * 6，打印一些返回值 [可选]
         * */
    }

    @After
    public void after() throws Exception {
    }


    /**
     * 接口的参数使用 @RequestBody 的，用这种方式
     *
     * @throws Exception
     */
    @Test
    public void testGetJnl() throws Exception {
        //MockMvcRequestBuilders.get|post|put|delete:构造一个请求.根据接口定义调用不用的方法
        //根据uri模板和uri变量值得到一个GET请求方式的MockHttpServletRequestBuilder；如get("/user/{id}", 1L)；
        String urlTemplate = "/moduleUse/getJnl";
        MockHttpServletRequestBuilder mockHsrBuilder = MockMvcRequestBuilders.post(urlTemplate);
        mockHsrBuilder.accept(MediaType.APPLICATION_JSON_VALUE);
        mockHsrBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockHsrBuilder.header("token", token);
        JSONObject reqPar = new JSONObject();
        reqPar.put("appId", "testAppId");
        reqPar.put("moduleId", "testModuleId");
        reqPar.put("startTime", "2018-04-18 19:17:08");
        reqPar.put("endTime", "2018-04-18 19:17:08");
        reqPar.put("timeScale", "hour");
        mockHsrBuilder.content(reqPar.toString());

        //mockMvc.perform执行一个请求；
        //resultAction是用来模拟客户端请求
        ResultActions resultActions = mvc.perform(mockHsrBuilder);
        //ResultActions.andExpect添加执行完成后的断言
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); //断言返回请求是否响应200
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)); //验证响应contentType
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.errcode").value(0)); //使用Json path验证JSON 请参考http://goessner.net/articles/JsonPath/
        //ResultActions.andDo添加一个结果处理器，表示要对结果做点什么事情，比如此处使用MockMvcResultHandlers.print()输出整个响应结果信息。

        //ResultActions.andReturn表示执行完成后返回相应的结果。
        MvcResult mvcResult = resultActions.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        System.out.println("result = " + result);

        //.andExpect(MockMvcResultMatchers.content().string("365"));  //测试接口返回内容
    }


    /**
     * 接口的参数使用 @RequestBody 的，用这种方式
     *
     * @throws Exception
     */
    @Test
    public void testSave() throws Exception {
        //MockMvcRequestBuilders.get|post|put|delete:构造一个请求.根据接口定义调用不用的方法
        //根据uri模板和uri变量值得到一个GET请求方式的MockHttpServletRequestBuilder；如get("/user/{id}", 1L)；
        String urlTemplate = "/moduleUse/getJnl";
        MockHttpServletRequestBuilder mockHsrBuilder = MockMvcRequestBuilders.post(urlTemplate);
        mockHsrBuilder.accept(MediaType.APPLICATION_JSON_VALUE);
        mockHsrBuilder.contentType(MediaType.APPLICATION_JSON_VALUE);
        mockHsrBuilder.header("token", token);
        JSONObject reqPar = new JSONObject();
        reqPar.put("appId", "testAppId");
        reqPar.put("moduleId", "testModuleId");
        reqPar.put("eotalNum", "1");
        reqPar.put("errorNum", "0");
        mockHsrBuilder.content(reqPar.toString());

        //mockMvc.perform执行一个请求；
        //resultAction是用来模拟客户端请求
        ResultActions resultActions = mvc.perform(mockHsrBuilder);
        //ResultActions.andExpect添加执行完成后的断言
        resultActions.andExpect(MockMvcResultMatchers.status().isOk()); //断言返回请求是否响应200
        resultActions.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)); //验证响应contentType
        resultActions.andExpect(MockMvcResultMatchers.jsonPath("$.errcode").value(0)); //使用Json path验证JSON 请参考http://goessner.net/articles/JsonPath/
        //ResultActions.andDo添加一个结果处理器，表示要对结果做点什么事情，比如此处使用MockMvcResultHandlers.print()输出整个响应结果信息。

        //ResultActions.andReturn表示执行完成后返回相应的结果。
        MvcResult mvcResult = resultActions.andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        System.out.println("result = " + result);

        //.andExpect(MockMvcResultMatchers.content().string("365"));  //测试接口返回内容
    }
}
