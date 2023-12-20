package com.lkj.lkjapigateway.filter;

import com.lkj.lkjapiclientsdk.utils.SignUtils;
import com.lkj.apicommon.entity.InterfaceInfo;
import com.lkj.apicommon.entity.User;
import com.lkj.apicommon.vo.UserInterfaceInfoMessage;
import com.lkj.apicommon.service.ApiBackendService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.lkj.apicommon.constant.RabbitmqConstant.EXCHANGE_INTERFACE_CONSISTENT;
import static com.lkj.apicommon.constant.RabbitmqConstant.ROUTING_KEY_INTERFACE_CONSISTENT;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.lkjapigateway
 * @Project：LKJAPI-Gateway
 * @name：CustomGlobalFilter
 * @Date：2023/12/11 15:05
 * @Filename：CustomGlobalFilter
 */
@Component
@Slf4j
public class CustomGlobalFilter implements GatewayFilter, Ordered {


    @DubboReference
    private ApiBackendService apiBackendService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    public static final String INTERFACE_HOST ="http://localhost:8091";

    /**
     * 全局过滤
     * @param exchange 路由交换机，所有的请求的信息，响应的信息，响应体，请求体都能从这里拿到
     * @param chain 责任链模式，因为我们的所有过滤器都是按照从上到下的顺序依次执行，形成了一个链条，所以这里用了一个chain，如果当前过滤器对请求进行了过滤后发现可以放行，就要去调用责任链中的next方法，相当于直接找到下一个过滤器，这里称filter。
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.用户发送请求到API网关,配置方法已经实现
        //2.请求日志
        ServerHttpRequest request = exchange.getRequest();
        //拿到响应对象
        ServerHttpResponse response = exchange.getResponse();

        String id = request.getId();
        String path =INTERFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        MultiValueMap<String, String> queryParams = request.getQueryParams();
        String hostString = request.getLocalAddress().getHostString();
        log.info("请求唯一标识：" + id);
        log.info("请求方法：" + method);
        log.info("请求路径：" + path);
        log.info("请求参数：" + queryParams);
        log.info("请求来源地址：" + hostString);

        //3.黑白名单（可做可不做）
//        if (!IP_WHITE_LIST.contains(hostString)){
//            //设置相应状态码为403 Forbidden（禁止访问）
//            response.setStatusCode(HttpStatus.FORBIDDEN);
//            //返回处理完成的响应
//            return response.setComplete();
//        }
        //4.用户鉴权（判断ak，sk是否合法）
        HttpHeaders headers = request.getHeaders();

        String accessKey = headers.getFirst("accessKey");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");
        String sign = headers.getFirst("sign");
        String body = headers.getFirst("body");
        //实际情况根据数据库查询是否已分配给用户
        //根据accessKey获取secretKey，判断accessKey是否合法
        User invokeUser = null;
        try {
           //调用内部服务，根据访问密钥获取用户信息
            invokeUser = apiBackendService.getInvokeUser(accessKey);
        } catch (RuntimeException e) {
           log.error("远程调用获取调用接口用户的信息失败",e);
        }
        if (invokeUser == null){
            return handleNoAuth(response);
        }
        //防重放，使用redis存储请求的唯一标识，随机时间，并定时淘汰，那使用什么redis结构来实现嗯？
        //既然是单个数据，这样用string结构实现即可
        // 设置随机数，并设置过期时间为5分钟
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(nonce, "1", 5, TimeUnit.MINUTES);
        if (success ==null){
            log.error("随机数存储失败!!!!");
            return handleNoAuth(response);
        }

        //实际情况是从数据库中查出 secretKey
        //从获取到的用户信息获取用户的密钥
        String secretKey = invokeUser.getSecretKey();
        //使用获取到的密钥对请求体进行签名
        String serverSign = SignUtils.genSign(body, secretKey);
        //检查请求中的签名是否为空，或者是否与服务器生成的签名不一致
        if (sign == null || !sign.equals(serverSign)){
            log.error("签名校验失败!!!!");
            //如果签名为空或者签名不一致，返回处理未授权的响应
            return handleNoAuth(response);
        }
        //5.远程调用判断接口是否存在以及获取调用接口信息
        // 从数据库中查询模拟接口是否存在，以及请求方法是否匹配（还可以校验请求参数）
        //初始化一个 InterfaceInfo 对象，用于存储查询结果
        InterfaceInfo interfaceInfo = null;
        try {
            //尝试从内部接口信息服务获取指定路径和方法的接口信息
             interfaceInfo = apiBackendService.getInterFaceInfo(path, method);
        } catch (Exception e) {
            log.error("getInterfaceInfo error",e);
        }
        if (interfaceInfo == null){
            log.error("接口不存在！！！！");
            return handleNoAuth(response);
        }
        //6.判断接口是否还有调用次数，并且统计接口调用，
        // 将二者转化成原子性操作(backend本地服务的本地事务实现)，解决二者数据一致性问题
        boolean result = false;
        try {
            result = apiBackendService.invokeCount(invokeUser.getId(), interfaceInfo.getId());
        } catch (Exception e) {
            log.error("统计接口出现问题或者用户恶意调用不存在的接口");
            e.printStackTrace();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return response.setComplete();
        }
        if (!result){
            log.error("接口剩余次数不足");
            return handleNoAuth(response);
        }
        //6.发起接口调用，网关路由实现
        Mono<Void> filter = chain.filter(exchange);

        return handleResponser(exchange,chain,interfaceInfo.getId(),invokeUser.getId());
    }

    /**
     * 对返回参数进行装饰，装饰者设计模式的作用是在原本的类的基础上对其能里进行增强，
     * 这样哪怕它是异步的，当最后执行完这个方法时，装饰器也能做额外的事情，这就是装饰者利用装饰增强原有方法的处理能力。
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponser(ServerWebExchange exchange, GatewayFilterChain chain,long interfaceInfoId,long userId){
        try {
            //获取原始的相应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            //获取数据缓冲工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            //获取相应的状态码
            HttpStatus statusCode = originalResponse.getStatusCode();
            //判断状态码是否为200 ok
            if(statusCode == HttpStatus.OK){
                //创建一个装饰后的响应对象（开始穿装备，增强能力）
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                    //重写writeWith方法，用于处理响应体的数据
                    //这段方法就是只要当我们的模拟接口调用完成之后，等它返回结果，
                    //就会调用writeWith方法，我们就能根据响应结果做一些自己的处理
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        //判断响应体是否时Flux类型
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            //返回一个处理后的响应体
                            //（这里就理解为它在拼接字符串，它把缓冲区的数据取出来，一点一点拼接好）
                            return super.writeWith(fluxBody.map(dataBuffer -> {
                                //读取响应体的内容并转换为字节数组
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                //7.获取响应结果，打上响应日志
                                // 构建日志
                                log.info("接口调用响应状态码：" + originalResponse.getStatusCode());
                                //responseBody
                                String responseBody= new String(content, StandardCharsets.UTF_8);
                                //8.接口调用失败，利用消息队列实现接口统计数据的回滚；
                                // 因为消息队列的可靠性所以我们选择消息队列而不是远程调用来实现
                                if (!(originalResponse.getStatusCode() == HttpStatus.OK)){
                                    log.error("接口异常调用-响应体:" + responseBody);
                                    UserInterfaceInfoMessage vo = new UserInterfaceInfoMessage(userId,interfaceInfoId);
                                    rabbitTemplate.convertAndSend(EXCHANGE_INTERFACE_CONSISTENT, ROUTING_KEY_INTERFACE_CONSISTENT,vo);
                                }
                                //将处理后的内容重新包装成DataBuffer并返回
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                //对于200 ok的请求，将装饰后的响应对象传递给下一个过滤器，并继续处理（设置response对象为装饰过的）
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            //对于非200 ok 的请求，直接返回，进行降级处理
            return chain.filter(exchange);//降级处理返回数据
        }catch (Exception e){
            //处理异常情况，记录错误日志
            log.error("网关处理响应异常" + e);
            return chain.filter(exchange);
        }

    }

    /**
     * 处理无权限调用异常
     * @param response
     * @return
     */
    private Mono<Void> handleNoAuth(ServerHttpResponse response) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            return response.setComplete();
    }

    private Mono<Void> handleInvokeError(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.setComplete();
    }
    @Override
        public int getOrder() {
            return -2;
        }
    }

