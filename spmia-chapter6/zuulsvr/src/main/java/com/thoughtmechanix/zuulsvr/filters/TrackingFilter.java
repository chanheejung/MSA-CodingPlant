package com.thoughtmechanix.zuulsvr.filters;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TrackingFilter extends ZuulFilter{
    private static final int      FILTER_ORDER =  1;
    private static final boolean  SHOULD_FILTER=true;
    private static final Logger logger = LoggerFactory.getLogger(TrackingFilter.class);

    @Autowired
    FilterUtils filterUtils;

    @Override
    public String filterType()
    {
        /** 사전 필터로 지정 */
        return FilterUtils.PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        /** 해당 타입의 다른 필터와 비교해 실행해야하는 순서 */
        return FILTER_ORDER; // 상수 1
    }

    public boolean shouldFilter() {
        /** 필터 활성화 여부 */
        return SHOULD_FILTER;
    }

    private boolean isCorrelationIdPresent(){
      if (filterUtils.getCorrelationId() !=null){
          return true;
      }

      return false;
    }

    /** 상관 관계 ID 랜덤수 생성 */
    private String generateCorrelationId(){
        return java.util.UUID.randomUUID().toString();
    }

    public Object run() {

        /** HEADER에 상관관계ID 존재 유무 확인 */
        if (isCorrelationIdPresent()) {
            /** 상관관계ID가 있다면, 아무것도 하지 않음 */
           logger.debug("tmx-correlation-id found in tracking filter: {}. ", filterUtils.getCorrelationId());
        }
        else{
            /** 상관관계ID가 없다면, 상관관계ID 생성 */
            filterUtils.setCorrelationId(generateCorrelationId());
            logger.debug("tmx-correlation-id generated in tracking filter: {}.", filterUtils.getCorrelationId());
        }

        RequestContext ctx = RequestContext.getCurrentContext();
        logger.debug("Processing incoming request for {}.",  ctx.getRequest().getRequestURI());
        return null;
    }
}