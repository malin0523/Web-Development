package com.example.demo.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	@Autowired
	private TokenProvider tokenProvider;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException{
		try {
			// ��û���� ��ū ��������
			String token = parseBearerToken(request);
			log.info("Filter is running...");
			// ��ū �˻��ϱ�. JWT�̹Ƿ� �ΰ� ������ ��û���� �ʰ��� ���� ����
			if(token!=null&&!token.equalsIgnoreCase("null")) {
				// userId ��������. ������ ��� ���� ó���ȴ�.
				String userId = tokenProvider.validateAndGetUserId(token);
				log.info("Authenticated user ID : " + userId);
				// ���� �Ϸ�. SecurityContextHolder�� ����ؾ� ������ ����ڶ�� �����Ѵ�.
				AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						userId,	// ������ ������� ����. ���ڿ��� �ƴϾ �ƹ����̳� ���� �� �ִ�. ���� UserDetails��� ������Ʈ�� �ִµ� �츮�� ���� �ʾҴ�.
						null,	
						AuthorityUtils.NO_AUTHORITIES
					);
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
				securityContext.setAuthentication(authentication);
				SecurityContextHolder.setContext(securityContext);
			}
		} catch(Exception ex) {
			logger.error("Could not set user authentication in security context", ex);
		}
		filterChain.doFilter(request, response);
	}
	
	private String parseBearerToken(HttpServletRequest request) {
		// Http ��û�� ����� �Ľ��� Bearer ��ū�� �����Ѵ�.
		String bearerToken = request.getHeader("Authorization");
		
		if(StringUtils.hasText(bearerToken)&&bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}