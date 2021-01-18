package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CommentEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品评价
 * 
 * @author oono
 * @email andychao3210@gmail.com
 * @date 2021-01-18 19:26:02
 */
@Mapper
public interface CommentMapper extends BaseMapper<CommentEntity> {
	
}
