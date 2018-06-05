package com.king.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.king.utils.pattern.XssHttpServletRequestWrapper;

import java.io.IOException;

/**
 *  XSS过滤
 * @author King chen
 * @date 2017年12月25日
 */

@WebFilter(filterName = "xssFilter", urlPatterns = "/*")
public class XssFilter implements Filter {

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		XssHttpServletRequestWrapper xssRequest = new XssHttpServletRequestWrapper(
				(HttpServletRequest) request);
		chain.doFilter(xssRequest, response);
	}

	@Override
	public void destroy() {
	}

}