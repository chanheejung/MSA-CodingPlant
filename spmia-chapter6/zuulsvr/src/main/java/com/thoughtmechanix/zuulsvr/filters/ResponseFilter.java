package com.thoughtmechanix.zuulsvr.filters;


import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseFilter extends ZuulFilter{
    private static final int  FILTER_ORDER=1;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(ResponseFilter.class);
    
    @Autowired
    FilterUtils filterUtils;

    @Override
    public String filterType() {
        /** 사후 필터로 지정 */
        return FilterUtils.POST_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        /** 필터 활성화 여부 */
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        /** 해당 타입의 다른 필터와 비교해 실행해야하는 순서 */
        return SHOULD_FILTER; // 상수 1
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        logger.debug("Adding the correlation id to the outbound headers. {}", filterUtils.getCorrelationId());
        /** 원래 HTTP 요청에서 전달된 상관관계 ID를 가져와 응답에 삽입한다. */
        ctx.getResponse().addHeader(FilterUtils.CORRELATION_ID, filterUtils.getCorrelationId());
        /** 처음부터 끝까지 주울에 들어오고 나가는 요청 항목을 보여 주기 위해 나가는 요청 URI를 기록*/
        logger.debug("Completing outgoing request for {}.", ctx.getRequest().getRequestURI());

        return null;
    }
}
