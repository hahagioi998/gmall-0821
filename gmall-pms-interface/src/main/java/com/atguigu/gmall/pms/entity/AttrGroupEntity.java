package com.atguigu.gmall.pms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * 属性分组
 * 
 * @author oono
 * @email andychao3210@gmail.com
 * @date 2021-01-18 19:26:02
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 分组id
	 */
	@TableId
	private Long id;
	/**
	 * 组名
	 */
	private String name;
	/**
	 * 排序
	 */
	private Integer sort;
	/**
	 * 组图标
	 */
	private String icon;
	/**
	 * 所属分类id
	 */
	private Long categoryId;
	/**
	 * 备注
	 */
	private String remark;

	//扩展的一个属性，用于让该pojo类复用于AttrGroupController中的queryAttrGroupsByCatId方法
	//但是必须声明：这个属性在数据库中没有对应的字段！否则报错，默认pojo的属性跟数据库表（上面@TableName中的那张表）
	//		中字段一一对应
	@TableField(exist = false)
	private List<AttrEntity> attrEntities;

}
