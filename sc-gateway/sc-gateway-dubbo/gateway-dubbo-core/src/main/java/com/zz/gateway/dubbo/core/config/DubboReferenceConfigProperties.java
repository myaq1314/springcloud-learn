package com.zz.gateway.dubbo.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.Ordered;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "dubbo.config")
public class DubboReferenceConfigProperties {

	private String charset = "UTF-8";

	private String server;

	private int filterOrder = Ordered.LOWEST_PRECEDENCE;

	private Map<String, DubboReferenceConfig> dubboRefer = new HashMap<String, DubboReferenceConfig>();

	private RedisHttpSessionConfig session = new RedisHttpSessionConfig();

	public RedisHttpSessionConfig getSession() {
		return session;
	}

	public void setSession(RedisHttpSessionConfig session) {
		this.session = session;
	}

	@Value("${includUrlPatterns:#{null}}")
	private String[] includUrlPatterns;

	@Value("${excludUrlPatterns:#{null}}")
	private String[] excludUrlPatterns;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String[] getIncludUrlPatterns() {
		return includUrlPatterns;
	}

	public void setIncludUrlPatterns(String[] includUrlPatterns) {
		this.includUrlPatterns = includUrlPatterns;
	}

	public String[] getExcludUrlPatterns() {
		return excludUrlPatterns;
	}

	public void setExcludUrlPatterns(String[] excludUrlPatterns) {
		this.excludUrlPatterns = excludUrlPatterns;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getFilterOrder() {
		return filterOrder;
	}

	public void setFilterOrder(int filterOrder) {
		this.filterOrder = filterOrder;
	}

	public Map<String, DubboReferenceConfig> getDubboRefer() {
		return dubboRefer;
	}

	public void setDubboRefer(Map<String, DubboReferenceConfig> dubboRefer) {
		this.dubboRefer = dubboRefer;
	}

	public class RedisHttpSessionConfig {
		private CookieConfig cookie = new CookieConfig();

		public CookieConfig getCookie() {
			return cookie;
		}

		public void setCookie(CookieConfig cookie) {
			this.cookie = cookie;
		}

		public class CookieConfig {
			private String domain;
			private String name;
			private String path;
			private boolean enable;

			public String getDomain() {
				return domain;
			}

			public void setDomain(String domain) {
				this.domain = domain;
			}

			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}

			public String getPath() {
				return path;
			}

			public void setPath(String path) {
				this.path = path;
			}

			public boolean isEnable() {
				return enable;
			}

			public void setEnable(boolean enable) {
				this.enable = enable;
			}

		}

	}

	public class CorsConfig{
		/**
		 * 是否开启cors
		 */
		private boolean enable=true;
		/**
		 * 允许跨域访问的来源
		 */
		@Value("${allowedOrigins:#{null}}")
		private List<String> allowedOrigins;
		/**
		 * 允许请求的方法
		 */
		@Value("${allowedMethods:#{null}}")
		private List<String> allowedMethods;
		/**
		 * 允许额外的头
		 */
		@Value("${allowedHeaders:#{null}}")
		private List<String> allowedHeaders;
		/**
		 * 执行跨域校验的path
		 */
		private String path="/**";
		/**
		 * 缓存多少秒
		 */
		private int maxAge=600;
		
		public int getMaxAge() {
			return maxAge;
		}
		public void setMaxAge(int maxAge) {
			this.maxAge = maxAge;
		}
		public boolean isEnable() {
			return enable;
		}
		public void setEnable(boolean enable) {
			this.enable = enable;
		}
		public List<String> getAllowedOrigins() {
			return allowedOrigins;
		}
		public void setAllowedOrigins(List<String> allowedOrigins) {
			this.allowedOrigins = allowedOrigins;
		}
		public List<String> getAllowedMethods() {
			return allowedMethods;
		}
		public void setAllowedMethods(List<String> allowedMethods) {
			this.allowedMethods = allowedMethods;
		}
		public List<String> getAllowedHeaders() {
			return allowedHeaders;
		}
		public void setAllowedHeaders(List<String> allowedHeaders) {
			this.allowedHeaders = allowedHeaders;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}
		
	}
}
