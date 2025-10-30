// package com.szschoolmanager.auth.config;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.redis.core.StringRedisTemplate;
// import org.springframework.data.redis.core.script.RedisScript;
// import org.springframework.mock.web.MockFilterChain;
// import org.springframework.mock.web.MockHttpServletRequest;
// import org.springframework.mock.web.MockHttpServletResponse;

// import java.util.Collections;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.*;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// class RateLimitFilterTest {

//     @Mock
//     private StringRedisTemplate redisTemplate;

//     @Mock
//     private RedisScript<Long> rateLimitScript;

//     @InjectMocks
//     private RateLimitFilter rateLimitFilter;

//     @Test
//     void shouldAllowRequestWhenUnderLimit() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setRequestURI("/api/v1/auth/login");
//         request.setRemoteAddr("192.168.1.1");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain filterChain = new MockFilterChain();

//         when(redisTemplate.execute(
//             any(RedisScript.class),
//             anyList(),
//             anyString(),
//             anyString()
//         )).thenReturn(3L); // Under limit

//         rateLimitFilter.doFilterInternal(request, response, filterChain);

//         assertEquals(200, response.getStatus());
//         assertNotNull(filterChain.getRequest());
//     }

//     @Test
//     void shouldRejectRequestWhenOverLimit() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setRequestURI("/api/v1/auth/login");
//         request.setRemoteAddr("192.168.1.1");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain filterChain = new MockFilterChain();

//         when(redisTemplate.execute(
//             any(RedisScript.class),
//             anyList(),
//             anyString(),
//             anyString()
//         )).thenReturn(6L); // Over limit (5)

//         rateLimitFilter.doFilterInternal(request, response, filterChain);

//         assertEquals(429, response.getStatus());
//         assertTrue(response.getContentAsString().contains("Too many requests"));
//     }

//     @Test
//     void shouldFailClosedWhenRedisUnavailable() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setRequestURI("/api/v1/auth/login");
//         request.setRemoteAddr("192.168.1.1");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain filterChain = new MockFilterChain();

//         when(redisTemplate.execute(
//             any(RedisScript.class),
//             anyList(),
//             anyString(),
//             anyString()
//         )).thenThrow(new org.springframework.data.redis.RedisConnectionFailureException("Connection refused"));

//         rateLimitFilter.doFilterInternal(request, response, filterChain);

//         assertEquals(503, response.getStatus());
//         assertTrue(response.getContentAsString().contains("Service temporarily unavailable"));
//     }

//     @Test
//     void shouldExtractClientIpFromXForwardedFor() throws Exception {
//         MockHttpServletRequest request = new MockHttpServletRequest();
//         request.setRequestURI("/api/v1/auth/login");
//         request.addHeader("X-Forwarded-For", "203.0.113.1, 198.51.100.1");
//         request.setRemoteAddr("192.168.1.1");

//         MockHttpServletResponse response = new MockHttpServletResponse();
//         MockFilterChain filterChain = new MockFilterChain();

//         when(redisTemplate.execute(
//             any(RedisScript.class),
//             eq(Collections.singletonList("rl:203.0.113.1:/api/v1/auth/login")),
//             anyString(),
//             anyString()
//         )).thenReturn(1L);

//         rateLimitFilter.doFilterInternal(request, response, filterChain);

//         verify(redisTemplate).execute(
//             any(RedisScript.class),
//             eq(Collections.singletonList("rl:203.0.113.1:/api/v1/auth/login")),
//             anyString(),
//             anyString()
//         );
//     }
// }