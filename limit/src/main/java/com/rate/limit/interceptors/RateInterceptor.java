package com.rate.limit.interceptors;

import com.rate.limit.service.RateLimitResult;
import com.rate.limit.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class RateInterceptor implements HandlerInterceptor {
    private RateLimiterService rateLimiterService;

    public RateInterceptor(RateLimiterService rateLimiterService)
    {
        this.rateLimiterService=rateLimiterService;
    }    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) {
        String ip=request.getRemoteAddr();
        RateLimitResult result=rateLimiterService.checkRateLimit(ip);
        if(!result.isAllowed()){
            response.setHeader("X-RateLimit-Limit",
                    String.valueOf(result.getLimit()));

            response.setHeader("X-RateLimit-Remaining",
                    String.valueOf(result.getRemaining()));
            response.setStatus(HttpServletResponse.SC_UPGRADE_REQUIRED);
        }
        return true;
    }

}
