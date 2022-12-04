package com.cb.gulimall.ware.config;

import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;


/**
 * @ClassName DataSourceConfig
 * @Description
 * @Author JingXu
 * @Date 2022/12/4 17:53
 */
@Configuration
public class SeataDataSourceProxyConfig {


    @Bean
	public DataSource dataSource(DataSourceProperties dataSourceProperties){
		HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		if(StringUtils.hasText(dataSourceProperties.getName())){
			dataSource.setPoolName(dataSourceProperties.getName());
		}
		return new DataSourceProxy(dataSource);
	}
}
