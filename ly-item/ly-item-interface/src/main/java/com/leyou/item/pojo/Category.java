package com.leyou.item.pojo;

import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;


/**
 * @author kann.tian
 * @time 2019/3/7
 * @feature: 商品分类对应的实体
 */
@Table(name="tb_category")
@Data
public class Category implements Serializable {
	@Id
	@KeySql(useGeneratedKeys = true)
	private Long id;
	private String name;
	private Long parentId;
	private Boolean isParent;
	private Integer sort;
//	private Integer sortOrder;
//	private Date created;
//	private Date updated;


}